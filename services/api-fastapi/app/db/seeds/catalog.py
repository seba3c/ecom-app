"""Load the shared development catalog without changing existing records."""

import asyncio
import json
from datetime import UTC, datetime
from decimal import Decimal
from pathlib import Path

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config import Settings
from app.db.session import create_async_engine_instance, create_async_session_maker
from app.db.seeds.users import seed_users
from app.models.category import Category
from app.models.product import Product
from app.models.user import User

CATALOG_PATH = Path("../../tools/seed/catalog.json")


async def seed_catalog(session: AsyncSession, path: Path = CATALOG_PATH) -> None:
    catalog = json.loads(path.read_text())
    categories = {
        category.name: category
        for category in (await session.scalars(select(Category))).all()
    }
    product_names = set((await session.scalars(select(Product.name))).all())
    sellers = {
        user.username: user
        for user in (
            await session.scalars(
                select(User).where(User.username.in_(("seller2", "seller3")))
            )
        ).all()
    }
    if set(sellers) != {"seller2", "seller3"}:
        raise RuntimeError("Seed sellers are missing")

    for entry in catalog["categories"]:
        category = categories.get(entry["name"])
        if category is None:
            now = datetime.now(UTC).replace(tzinfo=None)
            category = Category(name=entry["name"], created_at=now, updated_at=now)
            session.add(category)
            categories[category.name] = category
        for item in entry["products"]:
            if item["name"] in product_names:
                continue
            seller = sellers.get(item["seller"])
            if seller is None:
                raise ValueError(f"Unknown seed seller: {item['seller']}")
            session.add(
                Product(
                    name=item["name"],
                    description=item["description"],
                    quantity=item["quantity"],
                    price=Decimal(item["price"]),
                    discount=Decimal(item["discount"]),
                    category=category,
                    seller=seller,
                )
            )
            product_names.add(item["name"])
    await session.commit()


async def main() -> None:
    settings = Settings()
    if settings.environment != "development":
        raise RuntimeError("Catalog seeding is only available in development")
    engine = create_async_engine_instance(settings)
    try:
        session_maker = create_async_session_maker(engine)
        async with session_maker() as session:
            await seed_users(session)
            await seed_catalog(session)
    finally:
        await engine.dispose()


if __name__ == "__main__":
    asyncio.run(main())
