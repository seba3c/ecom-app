from decimal import Decimal

import pytest

from app.models.order import Order, OrderItem, Payment
from app.repositories.orders import OrderRepository
from tests.repositories.factories import (
    make_address,
    make_product,
    make_user,
)


@pytest.mark.anyio
async def test_create_persists_order_items_and_payment(db_session):
    user = await make_user(db_session)
    address = await make_address(db_session, user)
    product = await make_product(db_session)
    order = Order(
        user_id=user.id,
        address_id=address.id,
        payment=Payment(method="CARD", pg_name="Stripe"),
        status="PENDING",
        total_amount=Decimal("180.00"),
        items=[
            OrderItem(
                product=product,
                quantity=2,
                price=product.price,
                discount=product.discount,
            )
        ],
    )

    created = await OrderRepository(db_session).create(order)

    assert created.id is not None
    assert created.order_date is not None
    assert created.payment.id is not None
    assert created.items[0].id is not None
    assert created.items[0].product_id == product.id
    assert (await db_session.get(Order, created.id)).total_amount == 180
