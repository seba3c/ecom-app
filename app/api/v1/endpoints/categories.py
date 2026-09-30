import logging

from fastapi import APIRouter, BackgroundTasks, Depends, Query, status

from app.api.dependencies import (
    admin_user,
    get_category_repository,
)
from app.core.exceptions import APIError, CategoryDuplicatedError, not_found
from app.models.user import User
from app.repositories.categories import CategoryRepository, SORTABLE
from app.schemas.category import (
    Category,
    CategoryCreate,
    CategoryUpdate,
    CategoryStream,
    CategoryDetail,
    CategoryInput,
    CategoryCreatedPayload,
)
from app.schemas.common import Page, PaginationParams
from app.services.category_service import CategoryService
from app.services.pagination import validate_pagination
from app.tasks.category_tasks import notify_category_created

router = APIRouter(tags=["categories"])


logger = logging.getLogger(__name__)


@router.post(
    "/admin/categories",
    response_model=CategoryDetail,
    status_code=status.HTTP_201_CREATED,
)
async def create_category(
    category_create: CategoryInput,
    background_tasks: BackgroundTasks,
    repository: CategoryRepository = Depends(get_category_repository),
    _admin: User = Depends(admin_user),
):
    service = CategoryService(repository)
    try:
        category = await service.create_category(
            CategoryCreate(name=category_create.name)
        )
    except CategoryDuplicatedError as exc:
        raise APIError(
            400, f"Category with the name {category_create.name} already exists"
        ) from exc
    background_tasks.add_task(
        notify_category_created,
        CategoryCreatedPayload(id=category.id, name=category.name),
    )
    return category


@router.get("/public/categories", response_model=Page[CategoryDetail])
async def list_categories(
    page_number: int = Query(0, alias="pageNumber"),
    page_size: int = Query(50, alias="pageSize"),
    sort_by: str = Query("id", alias="sortBy"),
    sort_order: str = Query("asc", alias="sortOrder"),
    repository: CategoryRepository = Depends(get_category_repository),
):
    validate_pagination(page_number, page_size, sort_by, SORTABLE)
    return Page[CategoryDetail].model_validate(
        await repository.page(page_number, page_size, sort_by, sort_order)
    )


@router.get("/public/categories/stream")
async def stream_categories(
    repository: CategoryRepository = Depends(get_category_repository),
) -> CategoryStream:
    service = CategoryService(repository)
    params = PaginationParams()
    while True:
        results = await service.list_categories(params)
        if not results.items:
            break
        for item in results.items:
            yield Category.model_validate(item)
        params = PaginationParams(
            limit=params.limit, offset=params.offset + params.limit
        )


@router.get("/admin/categories/{category_id}", response_model=Category)
async def get_category(
    category_id: int,
    repository: CategoryRepository = Depends(get_category_repository),
    _admin: User = Depends(admin_user),
):
    service = CategoryService(repository)
    return await service.get_category(category_id)


@router.put("/admin/categories/{category_id}", response_model=CategoryDetail)
async def update_category(
    category_id: int,
    category_update: CategoryInput,
    repository: CategoryRepository = Depends(get_category_repository),
    _admin: User = Depends(admin_user),
):
    service = CategoryService(repository)
    if await repository.get(category_id) is None:
        raise not_found("Category", category_id)
    try:
        return await service.update_category(
            category_id, CategoryUpdate(name=category_update.name)
        )
    except CategoryDuplicatedError as exc:
        raise APIError(
            400, f"Category with the name {category_update.name} already exists"
        ) from exc


@router.delete("/admin/categories/{category_id}", response_model=CategoryDetail)
async def delete_category(
    category_id: int,
    repository: CategoryRepository = Depends(get_category_repository),
    _admin: User = Depends(admin_user),
):
    service = CategoryService(repository)
    category = await repository.get(category_id)
    if category is None:
        raise not_found("Category", category_id)
    detail = CategoryDetail.model_validate(category)
    await service.delete_category(category_id)
    return detail
