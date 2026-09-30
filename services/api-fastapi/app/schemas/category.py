from collections.abc import AsyncIterable
from datetime import datetime

from pydantic import Field, field_validator

from app.schemas.base import APIModel, BaseModel


class CategoryCreate(BaseModel):
    name: str = Field(..., min_length=2, max_length=255)


class CategoryUpdate(BaseModel):
    name: str | None = Field(None, min_length=2, max_length=255)


class Category(BaseModel):
    id: int
    name: str = Field(..., min_length=2, max_length=255)
    created_at: datetime
    updated_at: datetime


CategoryBulkCreate = list[CategoryCreate]

CategoryOrNone = Category | None

CategoryStream = AsyncIterable[Category]


class CategoryInput(APIModel):
    name: str = Field(min_length=2)

    @field_validator("name")
    @classmethod
    def nonblank(cls, value: str) -> str:
        if not value.strip():
            raise ValueError("Category name must not be blank")
        return value


class CategoryDetail(APIModel):
    id: int
    name: str


class CategoryCreatedPayload(BaseModel):
    id: int
    name: str
