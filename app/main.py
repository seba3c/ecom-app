import logging
from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi_pagination import add_pagination

from app.api.dependencies import get_settings
from app.api.v1.router import register_routers
from app.core.exceptions import register_exception_handlers
from app.core.logging import configure_logging
from app.core.middleware import register_middleware
from app.db.session import create_async_engine_instance, create_async_session_maker
from app.db.seeds.users import seed_users


@asynccontextmanager
async def lifespan(app: FastAPI):
    settings = get_settings()
    if (
        settings.environment == "production"
        and settings.jwt_secret
        == "development-only-secret-change-in-production-0123456789abcdef"
    ):
        raise RuntimeError("JWT_SECRET must be configured in production")
    configure_logging(settings)
    logger = logging.getLogger(__name__)

    logger.info("Starting %s app...", app.title)
    logger.info(
        f"Environment: {settings.environment}, "
        f"Debug: {settings.debug}, "
        f"Version: {settings.version}"
    )

    engine = create_async_engine_instance(settings)
    session_maker = create_async_session_maker(engine)

    app.state.engine = engine
    app.state.session_maker = session_maker

    if settings.environment == "development":
        async with session_maker() as session:
            await seed_users(session)

    yield

    logger.info("Shutting down %s app...", app.title)
    await engine.dispose()


def create_app() -> FastAPI:
    app = FastAPI(title="FastAPI ecom", lifespan=lifespan)
    register_middleware(app)
    register_routers(app)
    add_pagination(app)
    register_exception_handlers(app)
    return app


app = create_app()
