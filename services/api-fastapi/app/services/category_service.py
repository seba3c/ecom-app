from app.core.exceptions import CategoryNotFoundError
from app.repositories.categories import CategoryRepository
from app.schemas.category import Category, CategoryCreate, CategoryUpdate
from app.schemas.common import PaginationParams


class CategoryService:
    def __init__(self, repository: CategoryRepository) -> None:
        self.repository = repository

    async def list_categories(self, params: PaginationParams | None = None):
        if params is None:
            params = PaginationParams()
        return await self.repository.paginated_list(params)

    async def get_category(self, category_id: int) -> Category:
        category = await self.repository.get(category_id)
        if category is None:
            raise CategoryNotFoundError()
        return category

    async def create_category(self, category_create: CategoryCreate) -> Category:
        return await self.repository.create(category_create)

    async def update_category(
        self, category_id: int, category_update: CategoryUpdate
    ) -> Category:
        category = await self.repository.update(category_id, category_update)
        if category is None:
            raise CategoryNotFoundError()
        return category

    async def delete_category(self, category_id: int) -> None:
        deleted = await self.repository.delete(category_id)
        if not deleted:
            raise CategoryNotFoundError()
