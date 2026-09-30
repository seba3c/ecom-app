import time
from datetime import datetime, timezone

from fastapi import APIRouter

from app.api.dependencies import get_settings
from app.schemas.health import HealthResponse

START_TIME = time.time()

router = APIRouter(tags=["health"])


@router.get("", response_model=HealthResponse)
async def health_check():
    settings = get_settings()
    return HealthResponse(
        status="ok",
        timestamp=datetime.now(timezone.utc),
        version=settings.version,
        environment=settings.environment,
        uptime_seconds=time.time() - START_TIME,
    )
