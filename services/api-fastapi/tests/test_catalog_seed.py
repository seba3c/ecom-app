from decimal import Decimal

import pytest
from sqlalchemy import select

from app.db.seeds.catalog import seed_catalog
from app.db.seeds.users import seed_users
from app.models.category import Category
from app.models.product import Product
from app.models.user import User


@pytest.mark.anyio
async def test_catalog_seed_is_repeatable_and_preserves_existing_products(db_session):
    await seed_users(db_session)
    await seed_catalog(db_session)

    categories = (await db_session.scalars(select(Category))).all()
    products = (await db_session.scalars(select(Product))).all()
    sellers = (
        await db_session.scalars(
            select(User).where(User.username.in_(("seller2", "seller3")))
        )
    ).all()
    assert len(categories) == 10
    assert len(products) == 100
    assert len(sellers) == 2
    assert sum(item.seller.username == "seller2" for item in products) == 50
    assert sum(item.seller.username == "seller3" for item in products) == 50
    assert sum(item.category.name == "Electronics" for item in products) == 10

    products[0].price = Decimal("321.00")
    original_name = products[0].name
    await db_session.commit()
    await seed_catalog(db_session)

    assert len((await db_session.scalars(select(Category))).all()) == 10
    assert len((await db_session.scalars(select(Product))).all()) == 100
    preserved = await db_session.scalar(
        select(Product).where(Product.name == original_name)
    )
    assert preserved.price == Decimal("321.00")
