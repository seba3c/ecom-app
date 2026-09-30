from app.core.config import Settings
from app.services.auth_service import (
    hash_password,
    issue_token,
    token_username,
    verify_password,
)


def test_bcrypt_passwords():
    encoded = hash_password("userpass")
    assert encoded.startswith("$2")
    assert verify_password("userpass", encoded)
    assert not verify_password("wrong", encoded)


def test_jwt_validation_and_expiration():
    settings = Settings(jwt_secret="test-secret-with-at-least-thirty-two-bytes")
    token = issue_token("user", settings)
    assert token_username(token, settings) == "user"
    assert (
        token_username(
            token,
            Settings(jwt_secret="different-secret-with-at-least-thirty-two-bytes"),
        )
        is None
    )
    assert token_username("garbage", settings) is None
    expired = issue_token(
        "user", Settings(jwt_secret=settings.jwt_secret, jwt_expiration_seconds=-1)
    )
    assert token_username(expired, settings) is None
