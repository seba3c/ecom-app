from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.models.address import Address


class AddressRepository:
    def __init__(self, session: AsyncSession):
        self.session = session

    async def get(self, address_id: int, user_id: int | None = None) -> Address | None:
        query = select(Address).where(Address.id == address_id)
        if user_id is not None:
            query = query.where(Address.user_id == user_id)
        return await self.session.scalar(query)

    async def list(self, user_id: int) -> list[Address]:
        return list(
            (
                await self.session.scalars(
                    select(Address)
                    .where(Address.user_id == user_id)
                    .order_by(Address.id)
                )
            ).all()
        )
