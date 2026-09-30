from pydantic import Field, field_validator

from app.schemas.base import APIModel


class AddressInput(APIModel):
    street_line1: str = Field(min_length=1)
    street_line2: str | None = None
    city: str = Field(min_length=3)
    state: str = Field(min_length=3)
    country: str = Field(min_length=2)
    zip_code: str = Field(min_length=1)

    @field_validator("street_line1", "city", "state", "country", "zip_code")
    @classmethod
    def nonblank(cls, value: str) -> str:
        if not value.strip():
            raise ValueError("must not be blank")
        return value


class AddressDetail(AddressInput):
    id: int


class AddressList(APIModel):
    content: list[AddressDetail]
