import logging
import time

from app.schemas.category import CategoryCreatedPayload

logger = logging.getLogger("app")


def notify_category_created(category: CategoryCreatedPayload) -> None:
    """Mock a background task that logs before and after a simulated delay."""
    logger.info(
        "Starting background task for category: %s (id=%s)", category.name, category.id
    )
    time.sleep(3)
    logger.info(
        "Finished background task for category: %s (id=%s)", category.name, category.id
    )
