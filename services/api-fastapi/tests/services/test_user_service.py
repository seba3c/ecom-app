from types import SimpleNamespace
from unittest.mock import AsyncMock

import pytest
from sqlalchemy.exc import IntegrityError

from app.core.exceptions import APIError, FieldValidationError
from app.schemas.auth import SignupRequest
from app.services import user_service
from tests.services.factories import user


@pytest.fixture
def repository():
    return SimpleNamespace(
        by_username=AsyncMock(return_value=None),
        by_email=AsyncMock(return_value=None),
        create=AsyncMock(),
        session=SimpleNamespace(rollback=AsyncMock()),
    )


def signup_request(roles=None):
    return SignupRequest(
        username="buyer",
        email="buyer@example.com",
        password="password1",
        roles=roles,
    )


@pytest.mark.anyio
async def test_signup_hashes_password_and_defaults_to_user_role(
    repository, monkeypatch
):
    monkeypatch.setattr(
        user_service, "hash_password", lambda password: f"hash:{password}"
    )

    await user_service.signup(repository, signup_request())

    repository.create.assert_awaited_once_with(
        "buyer", "buyer@example.com", "hash:password1", {"ROLE_USER"}
    )


@pytest.mark.anyio
async def test_signup_maps_explicit_roles(repository, monkeypatch):
    monkeypatch.setattr(user_service, "hash_password", lambda password: "hashed")

    await user_service.signup(repository, signup_request({"seller", "user"}))

    assert repository.create.await_args.args[3] == {"ROLE_SELLER", "ROLE_USER"}


@pytest.mark.anyio
async def test_signup_rejects_existing_username_before_email_lookup(repository):
    repository.by_username.return_value = user()

    with pytest.raises(APIError, match="Username is already taken"):
        await user_service.signup(repository, signup_request())

    repository.by_email.assert_not_awaited()
    repository.create.assert_not_awaited()


@pytest.mark.anyio
async def test_signup_rejects_existing_email(repository):
    repository.by_email.return_value = user()

    with pytest.raises(APIError, match="Email is already taken"):
        await user_service.signup(repository, signup_request())

    repository.create.assert_not_awaited()


@pytest.mark.anyio
async def test_signup_rejects_unsupported_role(repository):
    with pytest.raises(FieldValidationError) as exc:
        await user_service.signup(repository, signup_request({"admin"}))

    assert exc.value.errors == {"roles": "Each role must be one of: user, seller"}
    repository.create.assert_not_awaited()


@pytest.mark.anyio
@pytest.mark.parametrize(
    ("username_now_exists", "message"),
    [(True, "Username is already taken"), (False, "Email is already taken")],
)
async def test_signup_integrity_race_rolls_back_and_reports_conflict(
    repository, monkeypatch, username_now_exists, message
):
    monkeypatch.setattr(user_service, "hash_password", lambda password: "hashed")
    repository.create.side_effect = IntegrityError("INSERT", {}, Exception())
    repository.by_username.side_effect = [None, user() if username_now_exists else None]

    with pytest.raises(APIError, match=message):
        await user_service.signup(repository, signup_request())

    repository.session.rollback.assert_awaited_once()


@pytest.mark.anyio
async def test_signin_returns_user_only_with_valid_password(repository, monkeypatch):
    existing = user()
    repository.by_username.return_value = existing
    monkeypatch.setattr(
        user_service,
        "verify_password",
        lambda password, hashed: password == "correct" and hashed == "hash",
    )

    assert await user_service.signin(repository, "buyer", "correct") is existing
    assert await user_service.signin(repository, "buyer", "wrong") is None


@pytest.mark.anyio
async def test_signin_missing_user_does_not_check_password(repository, monkeypatch):
    def unexpected(*args):
        raise AssertionError("password verification should not run")

    monkeypatch.setattr(user_service, "verify_password", unexpected)
    assert await user_service.signin(repository, "missing", "wrong") is None
