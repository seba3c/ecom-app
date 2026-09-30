from typing import Literal

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    app_name: str = "FastAPI ecommerce app"
    debug: bool = False
    version: str = "0.1"
    environment: Literal["development", "staging", "production"] = "development"
    log_level: Literal["DEBUG", "INFO", "WARNING", "ERROR", "CRITICAL"] = "INFO"
    database_url: str = ""
    jwt_secret: str = "development-only-secret-change-in-production-0123456789abcdef"
    jwt_expiration_seconds: int = 86400
    jwt_cookie_name: str = "ecommerce-app"

    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8")
