import pytest

from app.repositories.carts import CartRepository
from tests.repositories.factories import make_cart, make_product, make_user


@pytest.mark.anyio
async def test_get_by_user_with_items_and_lock(db_session):
    user = await make_user(db_session)
    other = await make_user(db_session, "other")
    product = await make_product(db_session)
    cart = await make_cart(db_session, user, product, quantity=2)
    await make_cart(db_session, other)
    repository = CartRepository(db_session)

    found = await repository.get(user.id)
    assert found.id == cart.id
    assert found.total_price == 180
    assert [(item.product_id, item.quantity) for item in found.items] == [
        (product.id, 2)
    ]
    assert (await repository.get(user.id, lock=True)).id == cart.id
    assert await repository.get(999) is None


@pytest.mark.anyio
async def test_page_sorts_and_counts_carts(db_session):
    first_user = await make_user(db_session)
    second_user = await make_user(db_session, "other")
    product = await make_product(db_session)
    await make_cart(db_session, first_user)
    expensive = await make_cart(db_session, second_user, product, quantity=2)
    repository = CartRepository(db_session)

    page = await repository.page(0, 1, "totalPrice", "desc")

    assert [cart.id for cart in page["content"]] == [expensive.id]
    assert page["total_elements"] == 2
    assert page["total_pages"] == 2
    assert page["last_page"] is False
