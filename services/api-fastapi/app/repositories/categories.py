from __future__ import annotations

from fastapi_pagination.ext.sqlalchemy import apaginate
from sqlalchemy import select
from sqlalchemy.exc import IntegrityError
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions import CategoryDuplicatedError
from app.models.category import Category
from app.schemas.category import (
    CategoryCreate,
    CategoryUpdate,
    CategoryBulkCreate,
    CategoryOrNone,
)
from app.schemas.common import PaginationParams
from app.repositories.pagination import paginate

SORTABLE = {
    "id": Category.id,
    "name": Category.name,
    "createdAt": Category.created_at,
    "updatedAt": Category.updated_at,
}


class CategoryRepository:
    def __init__(self, session: AsyncSession) -> None:
        self._session = session

    async def page(
        self, page_number: int, page_size: int, sort_by: str, sort_order: str
    ) -> dict:
        return await paginate(
            self._session,
            select(Category),
            page_number,
            page_size,
            SORTABLE[sort_by],
            sort_order,
        )

    async def list(self) -> list[Category]:
        result = await self._session.execute(select(Category))
        return list(result.scalars().all())

    async def paginated_list(self, params: PaginationParams | None = None):
        if params is None:
            params = PaginationParams()
        return await apaginate(self._session, select(Category), params)

    async def get(self, category_id: int) -> CategoryOrNone:
        result = await self._session.execute(
            select(Category).where(Category.id == category_id)
        )
        return result.scalar_one_or_none()

    async def create(self, category_create: CategoryCreate) -> Category:
        category = Category(name=category_create.name)
        self._session.add(category)
        try:
            await self._session.commit()
        except IntegrityError as exc:
            await self._session.rollback()
            raise CategoryDuplicatedError() from exc
        await self._session.refresh(category)
        return category

    async def bulk_create(self, creates: CategoryBulkCreate) -> None:
        categories = [Category(name=c.name) for c in creates]
        self._session.add_all(categories)
        await self._session.commit()

    async def update(
        self, category_id: int, category_update: CategoryUpdate
    ) -> CategoryOrNone:
        category = await self.get(category_id)
        if not category:
            return None
        if category_update.name is not None:
            category.name = category_update.name
        try:
            await self._session.commit()
        except IntegrityError as exc:
            await self._session.rollback()
            raise CategoryDuplicatedError() from exc
        await self._session.refresh(category)
        return category

    async def delete(self, category_id: int) -> bool:
        category = await self.get(category_id)
        if not category:
            return False
        await self._session.delete(category)
        await self._session.commit()
        return True
