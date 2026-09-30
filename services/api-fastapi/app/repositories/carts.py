from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.models.cart import Cart
from app.repositories.pagination import paginate

SORTABLE = {"id": Cart.id, "totalPrice": Cart.total_price, "user": Cart.user_id}


class CartRepository:
    def __init__(self, session: AsyncSession):
        self.session = session

    async def get(self, user_id: int, lock: bool = False) -> Cart | None:
        query = select(Cart).where(Cart.user_id == user_id)
        if lock:
            query = query.with_for_update()
        return await self.session.scalar(query)

    async def page(
        self, page_number: int, page_size: int, sort_by: str, sort_order: str
    ) -> dict:
        return await paginate(
            self.session,
            select(Cart),
            page_number,
            page_size,
            SORTABLE[sort_by],
            sort_order,
        )
