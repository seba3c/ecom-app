from types import SimpleNamespace
from unittest.mock import AsyncMock, Mock

import pytest

from app.core.exceptions import APIError
from app.services.address_service import AddressService
from tests.services.factories import address, address_input


@pytest.fixture
def service():
    repository = SimpleNamespace(get=AsyncMock(), session=AsyncMock())
    repository.session.add = Mock()
    return AddressService(repository)


@pytest.mark.anyio
async def test_address_returns_only_owners_address(service):
    existing = address()
    service.repository.get.return_value = existing

    assert await service.address(1, existing.id) is existing
    service.repository.get.assert_awaited_once_with(existing.id, 1)


@pytest.mark.anyio
async def test_address_missing_for_user_raises_not_found(service):
    service.repository.get.return_value = None

    with pytest.raises(APIError, match="Address with id: 3 not found") as exc:
        await service.address(2, 3)

    assert exc.value.status_code == 404


@pytest.mark.anyio
async def test_create_address_sets_owner_and_commits(service):
    data = address_input()

    created = await service.create_address(1, data)

    assert created.user_id == 1
    assert created.city == "Madrid"
    service.session.add.assert_called_once_with(created)
    service.session.commit.assert_awaited_once()
    service.session.refresh.assert_awaited_once_with(created)


@pytest.mark.anyio
async def test_update_address_changes_fields_and_commits(service):
    existing = address()
    service.repository.get.return_value = existing

    updated = await service.update_address(1, existing.id, address_input("Barcelona"))

    assert updated is existing
    assert updated.city == "Barcelona"
    service.repository.get.assert_awaited_once_with(existing.id, 1)
    service.session.commit.assert_awaited_once()


@pytest.mark.anyio
async def test_delete_address_removes_owned_record(service):
    existing = address()
    service.repository.get.return_value = existing

    await service.delete_address(1, existing.id)

    service.session.delete.assert_awaited_once_with(existing)
    service.session.commit.assert_awaited_once()


@pytest.mark.anyio
async def test_update_and_delete_missing_address_do_not_write(service):
    service.repository.get.return_value = None

    with pytest.raises(APIError):
        await service.update_address(1, 999, address_input())
    with pytest.raises(APIError):
        await service.delete_address(1, 999)

    service.session.commit.assert_not_awaited()
    service.session.delete.assert_not_awaited()
