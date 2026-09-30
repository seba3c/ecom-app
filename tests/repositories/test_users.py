import pytest

from app.repositories.users import UserRepository


@pytest.mark.anyio
async def test_create_persists_user_and_sorted_roles(db_session):
    repository = UserRepository(db_session)
    user = await repository.create(
        "buyer", "buyer@example.com", "hashed", {"ROLE_USER", "ROLE_SELLER"}
    )

    assert user.id is not None
    assert user.password_hash == "hashed"
    assert [role.role for role in user.roles] == ["ROLE_SELLER", "ROLE_USER"]
    assert (await repository.by_username("buyer")).id == user.id
    assert (await repository.by_email("buyer@example.com")).id == user.id
    assert (await repository.by_id(user.id)).id == user.id


@pytest.mark.anyio
async def test_lookups_return_none_for_missing_user(db_session):
    repository = UserRepository(db_session)
    assert await repository.by_username("missing") is None
    assert await repository.by_email("missing@example.com") is None
    assert await repository.by_id(999) is None
