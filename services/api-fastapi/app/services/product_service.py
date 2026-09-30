from sqlalchemy.exc import IntegrityError

from app.core.exceptions import APIError, not_found
from app.models.product import Product
from app.repositories.categories import CategoryRepository
from app.repositories.products import ProductRepository
from app.schemas.product import ProductDetail, ProductInput


class ProductService:
    def __init__(self, repository: ProductRepository):
        self.repository = repository
        self.session = repository.session
        self.categories = CategoryRepository(self.session)

    async def create_product(self, category_id: int, data: ProductInput) -> Product:
        category = await self.categories.get(category_id)
        if category is None:
            raise not_found("Category", category_id)
        if await self.repository.by_name(data.name):
            raise APIError(400, f"Product with the name {data.name} already exists")
        product = Product(**data.model_dump(), category=category)
        self.session.add(product)
        await self.session.commit()
        await self.session.refresh(product)
        return product

    async def update_product(self, product_id: int, data: ProductInput) -> Product:
        product = await self.repository.get(product_id)
        if product is None:
            raise not_found("Product", product_id)
        for key, value in data.model_dump().items():
            setattr(product, key, value)
        try:
            await self.session.commit()
        except IntegrityError as exc:
            await self.session.rollback()
            raise APIError(
                400, f"Product with the name {data.name} already exists"
            ) from exc
        return product

    async def delete_product(self, product_id: int) -> ProductDetail:
        product = await self.repository.get(product_id)
        if product is None:
            raise not_found("Product", product_id)
        detail = ProductDetail.model_validate(product)
        await self.session.delete(product)
        await self.session.commit()
        return detail
