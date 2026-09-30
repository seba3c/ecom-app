from sqlalchemy.ext.asyncio import AsyncSession

from app.models.user import UserRole
from app.repositories.users import UserRepository
from app.services.auth_service import hash_password

SEED_USERS = (
    ("user", "user@ecommapp.com", "userpass", {"ROLE_USER"}),
    ("seller", "seller@ecommapp.com", "sellerpass", {"ROLE_SELLER"}),
    (
        "admin",
        "admin@ecommapp.com",
        "adminpass",
        {"ROLE_USER", "ROLE_SELLER", "ROLE_ADMIN"},
    ),
)


async def seed_users(session: AsyncSession) -> None:
    repository = UserRepository(session)
    for username, email, password, roles in SEED_USERS:
        user = await repository.by_username(username)
        if user is None:
            await repository.create(username, email, hash_password(password), roles)
        elif {role.role for role in user.roles} != roles:
            user.roles = [role for role in user.roles if role.role in roles]
            user.roles.extend(
                UserRole(role=role)
                for role in sorted(roles - {item.role for item in user.roles})
            )
            await session.commit()
