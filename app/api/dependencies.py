from functools import lru_cache
from typing import Any, AsyncGenerator

from fastapi import Depends, Request
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config import Settings
from app.repositories.categories import CategoryRepository
from app.repositories.users import UserRepository
from app.repositories.products import ProductRepository
from app.repositories.addresses import AddressRepository
from app.repositories.carts import CartRepository
from app.repositories.orders import OrderRepository
from app.models.user import User
from app.services.auth_service import token_username
from app.core.exceptions import APIError, AuthenticationRequired


@lru_cache
def get_settings() -> Settings:
    return Settings()


async def get_db_session(
    request: Request,
) -> AsyncGenerator[Any, Any]:
    session_maker = request.app.state.session_maker
    async with session_maker() as session:
        yield session


def get_category_repository(
    session: AsyncSession = Depends(get_db_session),
) -> CategoryRepository:
    return CategoryRepository(session)


def get_user_repository(
    session: AsyncSession = Depends(get_db_session),
) -> UserRepository:
    return UserRepository(session)


def get_product_repository(
    session: AsyncSession = Depends(get_db_session),
) -> ProductRepository:
    return ProductRepository(session)


def get_address_repository(
    session: AsyncSession = Depends(get_db_session),
) -> AddressRepository:
    return AddressRepository(session)


def get_cart_repository(
    session: AsyncSession = Depends(get_db_session),
) -> CartRepository:
    return CartRepository(session)


def get_order_repository(
    session: AsyncSession = Depends(get_db_session),
) -> OrderRepository:
    return OrderRepository(session)


async def optional_user(
    request: Request,
    repository: UserRepository = Depends(get_user_repository),
    settings: Settings = Depends(get_settings),
) -> User | None:
    token = request.cookies.get(settings.jwt_cookie_name)
    if token is None:
        authorization = request.headers.get("Authorization", "")
        if authorization.startswith("Bearer "):
            token = authorization[7:]
    if not token:
        return None
    username = token_username(token, settings)
    return await repository.by_username(username) if username else None


async def current_user(user: User | None = Depends(optional_user)) -> User:
    if user is None:
        raise AuthenticationRequired()
    return user


async def admin_user(user: User = Depends(current_user)) -> User:
    if "ROLE_ADMIN" not in {role.role for role in user.roles}:
        raise APIError(403, "Forbidden")
    return user
