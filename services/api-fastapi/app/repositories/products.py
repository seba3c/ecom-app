from sqlalchemy import or_, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.models.product import Product
from app.repositories.pagination import paginate

SORTABLE = {
    "id": Product.id,
    "name": Product.name,
    "description": Product.description,
    "price": Product.price,
    "quantity": Product.quantity,
    "discount": Product.discount,
    "category": Product.category_id,
}


class ProductRepository:
    def __init__(self, session: AsyncSession):
        self.session = session

    async def get(self, product_id: int, lock: bool = False) -> Product | None:
        query = select(Product).where(Product.id == product_id)
        if lock:
            query = query.with_for_update()
        return await self.session.scalar(query)

    async def by_name(self, name: str) -> Product | None:
        return await self.session.scalar(select(Product).where(Product.name == name))

    async def page(
        self,
        page_number: int,
        page_size: int,
        sort_by: str,
        sort_order: str,
        *,
        keyword: str | None = None,
        category_id: int | None = None,
    ) -> dict:
        query = select(Product)
        if keyword is not None:
            pattern = f"%{keyword}%"
            query = query.where(
                or_(Product.name.ilike(pattern), Product.description.ilike(pattern))
            )
        if category_id is not None:
            query = query.where(Product.category_id == category_id)
        return await paginate(
            self.session, query, page_number, page_size, SORTABLE[sort_by], sort_order
        )
