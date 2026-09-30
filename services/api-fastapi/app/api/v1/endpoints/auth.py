from fastapi import APIRouter, Depends, Response
from fastapi.responses import JSONResponse, PlainTextResponse

from app.api.dependencies import get_settings, get_user_repository, optional_user
from app.core.config import Settings
from app.core.exceptions import APIError
from app.models.user import User
from app.repositories.users import UserRepository
from app.schemas.auth import Message, SigninRequest, SignupRequest, UserInfo
from app.services.auth_service import issue_token
from app.services.user_service import signin, signup

router = APIRouter(prefix="/auth", tags=["auth"])


def user_info(user: User, token: str | None) -> UserInfo:
    return UserInfo(
        id=user.id,
        username=user.username,
        roles=sorted(role.role for role in user.roles),
        jwt_token=token,
    )


@router.post("/signin", response_model=UserInfo)
async def sign_in(
    request: SigninRequest,
    response: Response,
    repository: UserRepository = Depends(get_user_repository),
    settings: Settings = Depends(get_settings),
) -> UserInfo | JSONResponse:
    user = await signin(repository, request.username, request.password)
    if user is None:
        return JSONResponse(
            status_code=401, content={"message": "Bad credentials", "status": False}
        )
    token = issue_token(user.username, settings)
    response.set_cookie(
        settings.jwt_cookie_name,
        token,
        max_age=settings.jwt_expiration_seconds,
        path="/api",
        httponly=True,
    )
    return user_info(user, token)


@router.post("/signup", response_model=Message)
async def sign_up(
    request: SignupRequest, repository: UserRepository = Depends(get_user_repository)
) -> Message:
    await signup(repository, request)
    return Message(message="User registered successfully!")


@router.get("/username", response_class=PlainTextResponse)
async def username(user: User | None = Depends(optional_user)) -> str:
    return user.username if user else ""


@router.get("/user", response_model=UserInfo | Message)
async def user_details(
    user: User | None = Depends(optional_user),
) -> UserInfo | Message:
    return user_info(user, None) if user else Message(message="No user details found")


@router.post("/signout", response_model=Message)
async def sign_out(
    response: Response,
    user: User | None = Depends(optional_user),
    settings: Settings = Depends(get_settings),
) -> Message:
    if user is None:
        raise APIError(400, "No user signed in")
    response.delete_cookie(settings.jwt_cookie_name, path="/api")
    return Message(message="User signed out successfully!")
