from pydantic import EmailStr, Field, field_validator

from app.schemas.base import APIModel


class SigninRequest(APIModel):
    username: str
    password: str


class SignupRequest(APIModel):
    username: str = Field(min_length=3, max_length=20)
    email: EmailStr = Field(max_length=50)
    password: str = Field(min_length=8, max_length=40)
    roles: set[str] | None = None

    @field_validator("username", "password")
    @classmethod
    def nonblank(cls, value: str) -> str:
        if not value.strip():
            raise ValueError("must not be blank")
        return value


class Message(APIModel):
    message: str


class UserInfo(APIModel):
    id: int
    username: str
    roles: list[str]
    jwt_token: str | None
