from decimal import Decimal

from pydantic import Field, field_validator

from app.schemas.base import APIModel
from app.schemas.category import CategoryDetail
from app.schemas.common import Money


class ProductInput(APIModel):
    name: str = Field(min_length=5)
    description: str = Field(min_length=1)
    quantity: int = Field(ge=0)
    price: Decimal = Field(ge=0)
    discount: Decimal = Field(ge=0)

    @field_validator("name")
    @classmethod
    def name_nonblank(cls, value: str) -> str:
        if not value.strip():
            raise ValueError("Product name must not be blank")
        return value

    @field_validator("description")
    @classmethod
    def description_nonblank(cls, value: str) -> str:
        if not value.strip():
            raise ValueError("Product description must not be blank")
        return value


class ProductDetail(APIModel):
    id: int
    name: str
    description: str
    quantity: int
    price: Money
    discount: Money
    category: CategoryDetail
