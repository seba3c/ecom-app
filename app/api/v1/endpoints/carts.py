from fastapi import APIRouter, Depends, Query, status
from fastapi.responses import JSONResponse

from app.api.dependencies import admin_user, current_user, get_cart_repository
from app.models.user import User
from app.repositories.carts import CartRepository, SORTABLE
from app.schemas.cart import CartDetail
from app.schemas.common import Page
from app.services.cart_service import CartService, cart_detail
from app.services.pagination import validate_pagination

router = APIRouter(tags=["carts"])


@router.get("/my_cart", response_model=CartDetail)
async def get_cart(
    user: User = Depends(current_user),
    repository: CartRepository = Depends(get_cart_repository),
):
    return cart_detail(await CartService(repository).cart(user.id, create=True))


@router.post(
    "/my_cart/{product_id}/quantity/{quantity}",
    response_model=CartDetail,
    status_code=status.HTTP_201_CREATED,
)
async def add_product(
    product_id: int,
    quantity: int,
    user: User = Depends(current_user),
    repository: CartRepository = Depends(get_cart_repository),
):
    if quantity < 1:
        return JSONResponse(
            status_code=400, content={"quantity": "must be greater than or equal to 1"}
        )
    return await CartService(repository).change_cart(
        user.id, product_id, quantity, "add"
    )


@router.put("/my_cart/{product_id}/quantity/{quantity}", response_model=CartDetail)
async def update_quantity(
    product_id: int,
    quantity: int,
    user: User = Depends(current_user),
    repository: CartRepository = Depends(get_cart_repository),
):
    if quantity < 1:
        return JSONResponse(
            status_code=400, content={"quantity": "must be greater than or equal to 1"}
        )
    return await CartService(repository).change_cart(
        user.id, product_id, quantity, "update"
    )


@router.delete("/my_cart/{product_id}", response_model=CartDetail)
async def remove_product(
    product_id: int,
    user: User = Depends(current_user),
    repository: CartRepository = Depends(get_cart_repository),
):
    return await CartService(repository).change_cart(
        user.id, product_id, None, "remove"
    )


@router.get("/admin/carts", response_model=Page[CartDetail])
async def all_carts(
    page_number: int = Query(0, alias="pageNumber"),
    page_size: int = Query(50, alias="pageSize"),
    sort_by: str = Query("id", alias="sortBy"),
    sort_order: str = Query("asc", alias="sortOrder"),
    repository: CartRepository = Depends(get_cart_repository),
    _admin: User = Depends(admin_user),
):
    validate_pagination(page_number, page_size, sort_by, SORTABLE)
    page = await repository.page(page_number, page_size, sort_by, sort_order)
    page["content"] = [cart_detail(cart) for cart in page["content"]]
    return Page[CartDetail].model_validate(page)
