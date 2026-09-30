from datetime import datetime

from pydantic import Field, field_validator

from app.schemas.base import APIModel
from app.schemas.common import Money
from app.schemas.product import ProductDetail


class OrderItemDetail(APIModel):
    id: int
    quantity: int
    price: Money
    discount: Money
    product: ProductDetail


class PaymentDetail(APIModel):
    id: int
    method: str
    pg_name: str
    pg_payment_id: str | None
    pg_status: str | None
    pg_response: str | None


class OrderInput(APIModel):
    address_id: int
    payment_method: str = Field(min_length=4)
    pg_name: str = Field(min_length=1)
    pg_payment_id: str | None = None
    pg_status: str | None = None
    pg_response: str | None = None

    @field_validator("payment_method", "pg_name")
    @classmethod
    def nonblank(cls, value: str) -> str:
        if not value.strip():
            raise ValueError("must not be blank")
        return value


class OrderDetail(APIModel):
    id: int
    email: str
    items: list[OrderItemDetail]
    order_date: datetime
    total_amount: Money
    status: str
    shipping_address_id: int
    payment: PaymentDetail
