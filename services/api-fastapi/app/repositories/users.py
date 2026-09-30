from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.models.user import User, UserRole


class UserRepository:
    def __init__(self, session: AsyncSession):
        self.session = session

    async def by_username(self, username: str) -> User | None:
        return await self.session.scalar(select(User).where(User.username == username))

    async def by_email(self, email: str) -> User | None:
        return await self.session.scalar(select(User).where(User.email == email))

    async def by_id(self, user_id: int) -> User | None:
        return await self.session.get(User, user_id)

    async def create(
        self, username: str, email: str, password_hash: str, roles: set[str]
    ) -> User:
        user = User(username=username, email=email, password_hash=password_hash)
        user.roles = [UserRole(role=role) for role in sorted(roles)]
        self.session.add(user)
        await self.session.commit()
        await self.session.refresh(user)
        return user
