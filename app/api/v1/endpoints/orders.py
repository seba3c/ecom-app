from fastapi import APIRouter, Depends, status

from app.api.dependencies import current_user, get_order_repository
from app.models.user import User
from app.repositories.orders import OrderRepository
from app.schemas.order import OrderDetail, OrderInput
from app.services.order_service import OrderService

router = APIRouter(prefix="/orders", tags=["orders"])


@router.post("", response_model=OrderDetail, status_code=status.HTTP_201_CREATED)
async def place_order(
    data: OrderInput,
    user: User = Depends(current_user),
    repository: OrderRepository = Depends(get_order_repository),
):
    return await OrderService(repository).place_order(user, data)
