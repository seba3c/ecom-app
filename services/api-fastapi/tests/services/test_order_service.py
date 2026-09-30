from types import SimpleNamespace
from unittest.mock import AsyncMock

import pytest

from app.core.exceptions import APIError
from app.schemas.order import OrderInput
from app.services.order_service import OrderService
from tests.services.factories import (
    address,
    cart,
    cart_item,
    order_date,
    product,
    user,
)


@pytest.fixture
def service():
    instance = OrderService(SimpleNamespace(session=AsyncMock(), create=AsyncMock()))
    instance.carts.get = AsyncMock()
    instance.addresses.get = AsyncMock()
    instance.products.get = AsyncMock()
    return instance


def order_input(address_id=3):
    return OrderInput(
        address_id=address_id,
        payment_method="CARD",
        pg_name="Stripe",
        pg_payment_id="pay_1",
        pg_status="paid",
        pg_response="ok",
    )


@pytest.mark.anyio
async def test_place_order_rejects_missing_cart(service):
    service.carts.get.return_value = None

    with pytest.raises(APIError, match="Cart not found for user"):
        await service.place_order(user(), order_input())

    service.carts.get.assert_awaited_once_with(1, lock=True)
    service.repository.create.assert_not_awaited()


@pytest.mark.anyio
async def test_place_order_rejects_empty_cart(service):
    service.carts.get.return_value = cart()

    with pytest.raises(APIError, match="Cart is empty"):
        await service.place_order(user(), order_input())

    service.addresses.get.assert_not_awaited()
    service.repository.create.assert_not_awaited()


@pytest.mark.anyio
async def test_place_order_rejects_address_not_owned_by_user(service):
    existing = cart([cart_item()])
    service.carts.get.return_value = existing
    service.addresses.get.return_value = None

    with pytest.raises(APIError, match="Address with id: 3 not found") as exc:
        await service.place_order(user(), order_input())

    assert exc.value.status_code == 404
    service.addresses.get.assert_awaited_once_with(3, 1)
    service.products.get.assert_not_awaited()
    service.repository.create.assert_not_awaited()
    assert len(existing.items) == 1


@pytest.mark.anyio
async def test_place_order_rejects_insufficient_stock_without_saving(service):
    existing_product = product(quantity=1)
    existing = cart([cart_item(existing_product, quantity=2)])
    existing.total_price = 180
    service.carts.get.return_value = existing
    service.addresses.get.return_value = address()
    service.products.get.return_value = existing_product

    with pytest.raises(APIError, match="Insufficient stock"):
        await service.place_order(user(), order_input())

    service.products.get.assert_awaited_once_with(existing_product.id, lock=True)
    service.repository.create.assert_not_awaited()
    assert existing_product.quantity == 1
    assert len(existing.items) == 1
    assert existing.total_price == 180


@pytest.mark.anyio
async def test_place_order_saves_payment_items_and_clears_cart(service):
    existing_product = product()
    existing = cart([cart_item(existing_product, quantity=2)])
    existing.total_price = 180
    service.carts.get.return_value = existing
    service.addresses.get.return_value = address()
    service.products.get.return_value = existing_product

    async def save(order):
        order.id = 6
        order.order_date = order_date()
        order.payment.id = 7
        order.items[0].id = 8
        return order

    service.repository.create.side_effect = save

    detail = await service.place_order(user(), order_input())

    assert detail.id == 6
    assert detail.email == "buyer@example.com"
    assert detail.status == "PENDING"
    assert detail.shipping_address_id == 3
    assert detail.total_amount == 180
    assert detail.payment.method == "CARD"
    assert detail.payment.pg_name == "Stripe"
    assert detail.items[0].quantity == 2
    assert detail.items[0].product.id == existing_product.id
    assert existing_product.quantity == 3
    assert existing.items == []
    assert existing.total_price == 0
    service.repository.create.assert_awaited_once()
