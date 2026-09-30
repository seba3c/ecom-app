from datetime import datetime

from app.schemas.base import BaseModel


class HealthResponse(BaseModel):
    status: str
    timestamp: datetime
    version: str
    environment: str
    uptime_seconds: float
