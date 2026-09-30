import pytest
import os
from httpx import AsyncClient, ASGITransport
from sqlalchemy.ext.asyncio import create_async_engine, async_sessionmaker
from sqlalchemy.pool import StaticPool

from app.api.dependencies import get_db_session
from app.db.base import Base
import app.models  # noqa: F401
from app.db.seeds.users import seed_users
from app.main import app
from app.repositories.categories import CategoryRepository
from app.schemas.category import CategoryCreate


@pytest.fixture
def anyio_backend():
    return "asyncio"


@pytest.fixture
async def db_engine():
    url = os.getenv("TEST_DATABASE_URL", "sqlite+aiosqlite:///:memory:")
    engine = create_async_engine(
        url, poolclass=StaticPool if url.startswith("sqlite") else None
    )
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
    yield engine
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.drop_all)
    await engine.dispose()


@pytest.fixture
async def db_session(db_engine):
    session_maker = async_sessionmaker(db_engine, expire_on_commit=False)
    async with session_maker() as session:
        yield session


@pytest.fixture
async def repository(db_session):
    return CategoryRepository(db_session)


@pytest.fixture
async def category_factory(repository):
    async def _make(name: str = "Test Category"):
        return await repository.create(CategoryCreate(name=name))

    return _make


@pytest.fixture
async def client(db_engine):
    session_maker = async_sessionmaker(db_engine, expire_on_commit=False)
    async with session_maker() as session:
        await seed_users(session)

    async def override_db_session():
        async with session_maker() as session:
            yield session

    app.dependency_overrides[get_db_session] = override_db_session
    async with AsyncClient(
        transport=ASGITransport(app=app), base_url="http://test"
    ) as ac:
        yield ac
    app.dependency_overrides.clear()
