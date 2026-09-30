from fastapi import APIRouter, Depends, Query, status

from app.api.dependencies import admin_user, get_product_repository
from app.core.exceptions import not_found
from app.models.user import User
from app.repositories.categories import CategoryRepository
from app.repositories.products import ProductRepository, SORTABLE
from app.schemas.common import Page
from app.schemas.product import ProductDetail, ProductInput
from app.services.pagination import validate_pagination
from app.services.product_service import ProductService

router = APIRouter(tags=["products"])


@router.post(
    "/admin/categories/{category_id}/products",
    response_model=ProductDetail,
    status_code=status.HTTP_201_CREATED,
)
async def create_product(
    category_id: int,
    data: ProductInput,
    repository: ProductRepository = Depends(get_product_repository),
    _admin: User = Depends(admin_user),
):
    return await ProductService(repository).create_product(category_id, data)


@router.put("/admin/products/{product_id}", response_model=ProductDetail)
async def update_product(
    product_id: int,
    data: ProductInput,
    repository: ProductRepository = Depends(get_product_repository),
    _admin: User = Depends(admin_user),
):
    return await ProductService(repository).update_product(product_id, data)


@router.delete("/admin/products/{product_id}", response_model=ProductDetail)
async def delete_product(
    product_id: int,
    repository: ProductRepository = Depends(get_product_repository),
    _admin: User = Depends(admin_user),
):
    return await ProductService(repository).delete_product(product_id)


async def product_page(
    repository: ProductRepository,
    page_number: int,
    page_size: int,
    sort_by: str,
    sort_order: str,
    *,
    keyword: str | None = None,
    category_id: int | None = None,
) -> Page[ProductDetail]:
    validate_pagination(page_number, page_size, sort_by, SORTABLE)
    if (
        category_id is not None
        and await CategoryRepository(repository.session).get(category_id) is None
    ):
        raise not_found("Category", category_id)
    page = await repository.page(
        page_number,
        page_size,
        sort_by,
        sort_order,
        keyword=keyword,
        category_id=category_id,
    )
    return Page[ProductDetail].model_validate(page)


@router.get("/public/products", response_model=Page[ProductDetail])
async def all_products(
    page_number: int = Query(0, alias="pageNumber"),
    page_size: int = Query(50, alias="pageSize"),
    sort_by: str = Query("id", alias="sortBy"),
    sort_order: str = Query("asc", alias="sortOrder"),
    repository: ProductRepository = Depends(get_product_repository),
):
    return await product_page(repository, page_number, page_size, sort_by, sort_order)


@router.get("/public/products/keyword/{keyword}", response_model=Page[ProductDetail])
async def search_products(
    keyword: str,
    page_number: int = Query(0, alias="pageNumber"),
    page_size: int = Query(50, alias="pageSize"),
    sort_by: str = Query("id", alias="sortBy"),
    sort_order: str = Query("asc", alias="sortOrder"),
    repository: ProductRepository = Depends(get_product_repository),
):
    return await product_page(
        repository, page_number, page_size, sort_by, sort_order, keyword=keyword
    )


@router.get(
    "/public/categories/{category_id}/products", response_model=Page[ProductDetail]
)
async def category_products(
    category_id: int,
    page_number: int = Query(0, alias="pageNumber"),
    page_size: int = Query(50, alias="pageSize"),
    sort_by: str = Query("id", alias="sortBy"),
    sort_order: str = Query("asc", alias="sortOrder"),
    repository: ProductRepository = Depends(get_product_repository),
):
    return await product_page(
        repository, page_number, page_size, sort_by, sort_order, category_id=category_id
    )


@router.get("/public/products/{product_id}", response_model=ProductDetail)
async def get_product(
    product_id: int, repository: ProductRepository = Depends(get_product_repository)
):
    product = await repository.get(product_id)
    if product is None:
        raise not_found("Product", product_id)
    return product
