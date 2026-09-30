import asyncio
import itertools
import logging

from app.core.config import Settings
from app.db.session import create_async_engine_instance, create_async_session_maker
from app.repositories.categories import CategoryRepository
from app.schemas.category import CategoryCreate

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

TARGET_COUNT = 1_000

# fmt: off
BASE_CATEGORIES = [
    "Electronics", "Phones", "Computers", "Tablets", "Cameras",
    "Audio", "TVs", "Gaming", "Software", "Accessories",
    "Clothing", "Men", "Women", "Kids", "Shoes",
    "Jewelry", "Watches", "Bags", "Beauty", "Skincare",
    "Makeup", "Hair", "Fragrance", "Health", "Wellness",
    "Vitamins", "Home", "Kitchen", "Furniture", "Bedding",
    "Bath", "Decor", "Lighting", "Garden", "Tools",
    "Automotive", "Tires", "Parts", "Sports", "Fitness",
    "Outdoors", "Camping", "Cycling", "Running", "Yoga",
    "Toys", "Games", "Puzzles", "Baby", "Food",
    "Grocery", "Beverages", "Snacks", "Coffee", "Wine",
    "Pets", "Dogs", "Cats", "Office", "School",
    "Art", "Crafts", "Books", "Fiction", "Music",
    "Movies", "Party", "Gifts", "Industrial", "Scientific",
    "Lab", "Test", "Safety", "Cleaning", "Storage",
    "Hardware", "Paint", "Plumbing", "Electrical", "Building",
    "Action", "Dolls", "Educational", "Strollers", "Diapers",
    "Dental", "Medical", "Organic", "Fish", "Birds",
    "Instruments", "Seasonal", "Holiday", "Material", "Handling",
    "Packaging", "Janitorial", "Measurement", "Nonfiction", "Comics",
]
# fmt: on


def generate_category_names(count: int) -> list[str]:
    names: list[str] = []
    for a, b in itertools.combinations(BASE_CATEGORIES, 2):
        names.append(f"{a} & {b}")
        if len(names) >= count:
            break
    return names


async def seed_categories() -> None:
    settings = Settings()
    engine = create_async_engine_instance(settings)
    session_maker = create_async_session_maker(engine)

    async with session_maker() as session:
        repository = CategoryRepository(session)

        names = generate_category_names(TARGET_COUNT)
        creates = [CategoryCreate(name=name) for name in names]
        await repository.bulk_create(creates)
        logger.info("Seeded %d categories.", len(creates))

    await engine.dispose()


if __name__ == "__main__":
    asyncio.run(seed_categories())
