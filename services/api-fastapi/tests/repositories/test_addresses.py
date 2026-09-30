import pytest

from app.repositories.addresses import AddressRepository
from tests.repositories.factories import make_address, make_user


@pytest.mark.anyio
async def test_get_and_list_only_return_owners_addresses(db_session):
    owner = await make_user(db_session)
    other = await make_user(db_session, "other")
    first = await make_address(db_session, owner)
    second = await make_address(db_session, owner, "Barcelona")
    await make_address(db_session, other)
    repository = AddressRepository(db_session)

    assert [item.id for item in await repository.list(owner.id)] == [
        first.id,
        second.id,
    ]
    assert (await repository.get(first.id, owner.id)).id == first.id
    assert await repository.get(first.id, other.id) is None
    assert (await repository.get(first.id)).id == first.id
    assert await repository.get(999, owner.id) is None


@pytest.mark.anyio
async def test_list_empty_for_user_without_addresses(db_session):
    user = await make_user(db_session)
    assert await AddressRepository(db_session).list(user.id) == []
