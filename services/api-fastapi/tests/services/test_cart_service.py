from decimal import Decimal
from types import SimpleNamespace
from unittest.mock import AsyncMock, Mock

import pytest

from app.core.exceptions import APIError
from app.services.cart_service import CartService, cart_detail
from tests.services.factories import cart, cart_item, product


@pytest.fixture
def service():
    repository = SimpleNamespace(get=AsyncMock(), session=AsyncMock())
    repository.session.add = Mock()
    instance = CartService(repository)
    instance.products.get = AsyncMock()
    return instance


@pytest.mark.anyio
async def test_cart_returns_existing_cart_without_writes(service):
    existing = cart()
    service.repository.get.return_value = existing

    assert await service.cart(1) is existing
    service.session.commit.assert_not_awaited()


@pytest.mark.anyio
async def test_cart_missing_without_create_raises(service):
    service.repository.get.return_value = None

    with pytest.raises(APIError, match="Cart not found"):
        await service.cart(1)


@pytest.mark.anyio
async def test_cart_create_initializes_empty_cart(service):
    service.repository.get.return_value = None

    created = await service.cart(1, create=True)

    assert created.user_id == 1
    assert created.items == []
    assert created.total_price == 0
    service.session.add.assert_called_once_with(created)
    service.session.commit.assert_awaited_once()
    service.session.refresh.assert_awaited_once_with(created)


@pytest.mark.anyio
async def test_add_item_captures_price_and_discount(service):
    existing = cart()
    service.repository.get.return_value = existing
    service.products.get.return_value = product()

    # In a real session, flush assigns the new cart item's ID before serialization.
    async def commit():
        existing.items[0].id = 5

    service.session.commit.side_effect = commit
    detail = await service.change_cart(1, 2, 2, "add")

    assert detail.total_price == 180
    assert detail.cart_items[0].quantity == 2
    assert detail.cart_items[0].price == 100
    assert detail.cart_items[0].discount == 10
    service.session.commit.assert_awaited_once()


@pytest.mark.anyio
async def test_update_item_recalculates_total(service):
    item = cart_item()
    existing = cart([item])
    service.repository.get.return_value = existing
    service.products.get.return_value = item.product

    detail = await service.change_cart(1, item.product_id, 3, "update")

    assert item.quantity == 3
    assert detail.total_price == 270
    service.session.commit.assert_awaited_once()


@pytest.mark.anyio
async def test_remove_item_clears_total(service):
    item = cart_item()
    existing = cart([item])
    existing.total_price = Decimal("180")
    service.repository.get.return_value = existing
    service.products.get.return_value = item.product

    detail = await service.change_cart(1, item.product_id, None, "remove")

    assert detail.cart_items == []
    assert detail.total_price == 0
    service.session.commit.assert_awaited_once()


@pytest.mark.anyio
@pytest.mark.parametrize(
    ("action", "items", "quantity", "expected"),
    [
        ("add", True, 1, "already in cart"),
        ("update", False, 1, "not found in cart"),
        ("add", False, 6, "Insufficient stock"),
    ],
)
async def test_change_cart_rejects_invalid_changes(
    service, action, items, quantity, expected
):
    existing_product = product()
    existing = cart([cart_item(existing_product)] if items else [])
    service.repository.get.return_value = existing
    service.products.get.return_value = existing_product

    with pytest.raises(APIError, match=expected):
        await service.change_cart(1, existing_product.id, quantity, action)

    service.session.commit.assert_not_awaited()


@pytest.mark.anyio
async def test_change_cart_missing_product_raises_without_commit(service):
    service.repository.get.return_value = cart()
    service.products.get.return_value = None

    with pytest.raises(APIError, match="Product with id: 9 not found"):
        await service.change_cart(1, 9, 1, "add")

    service.session.commit.assert_not_awaited()


def test_cart_detail_serializes_items_and_total():
    item = cart_item()
    existing = cart([item])
    existing.total_price = Decimal("180")

    detail = cart_detail(existing)

    assert detail.id == 4
    assert detail.total_price == 180
    assert detail.cart_items[0].product.id == 2
