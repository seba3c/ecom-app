from sqlalchemy.exc import IntegrityError

from app.core.exceptions import APIError, FieldValidationError
from app.models.user import User
from app.repositories.users import UserRepository
from app.schemas.auth import SignupRequest
from app.services.auth_service import hash_password, verify_password


async def signup(repository: UserRepository, request: SignupRequest) -> None:
    if await repository.by_username(request.username):
        raise APIError(400, "Error: Username is already taken!")
    if await repository.by_email(request.email):
        raise APIError(400, "Error: Email is already taken!")
    roles = request.roles or {"user"}
    if not roles <= {"user", "seller"}:
        raise FieldValidationError({"roles": "Each role must be one of: user, seller"})
    try:
        await repository.create(
            request.username,
            request.email,
            hash_password(request.password),
            {f"ROLE_{role.upper()}" for role in roles},
        )
    except IntegrityError as exc:
        await repository.session.rollback()
        if await repository.by_username(request.username):
            raise APIError(400, "Error: Username is already taken!") from exc
        raise APIError(400, "Error: Email is already taken!") from exc


async def signin(
    repository: UserRepository, username: str, password: str
) -> User | None:
    user = await repository.by_username(username)
    if user is None or not verify_password(password, user.password_hash):
        return None
    return user
