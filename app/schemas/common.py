from decimal import Decimal
from typing import Annotated, Generic, TypeVar

from fastapi_pagination import LimitOffsetPage, LimitOffsetParams
from pydantic import PlainSerializer

from app.schemas.base import APIModel

PaginatedResponse = LimitOffsetPage
PaginationParams = LimitOffsetParams
Money = Annotated[Decimal, PlainSerializer(float, return_type=float, when_used="json")]
T = TypeVar("T")


class Page(APIModel, Generic[T]):
    content: list[T]
    page_number: int
    page_size: int
    total_elements: int
    total_pages: int
    last_page: bool
