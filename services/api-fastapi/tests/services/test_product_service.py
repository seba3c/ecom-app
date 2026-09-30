from types import SimpleNamespace
from unittest.mock import AsyncMock, Mock

import pytest
from sqlalchemy.exc import IntegrityError

from app.core.exceptions import APIError
from app.services.product_service import ProductService
from tests.services.factories import category, product, product_input


@pytest.fixture
def service():
    repository = SimpleNamespace(
        get=AsyncMock(), by_name=AsyncMock(), session=AsyncMock()
    )
    repository.session.add = Mock()
    return ProductService(repository)


@pytest.mark.anyio
async def test_create_product_persists_category_and_data(service):
    existing_category = category()
    service.categories.get = AsyncMock(return_value=existing_category)
    service.repository.by_name.return_value = None

    created = await service.create_product(1, product_input())

    assert created.category is existing_category
    assert created.name == "Laptop Pro"
    service.session.add.assert_called_once_with(created)
    service.session.commit.assert_awaited_once()
    service.session.refresh.assert_awaited_once_with(created)


@pytest.mark.anyio
async def test_create_product_rejects_missing_category(service):
    service.categories.get = AsyncMock(return_value=None)

    with pytest.raises(APIError, match="Category with id: 9 not found"):
        await service.create_product(9, product_input())

    service.repository.by_name.assert_not_awaited()
    service.session.commit.assert_not_awaited()


@pytest.mark.anyio
async def test_create_product_rejects_duplicate_name(service):
    service.categories.get = AsyncMock(return_value=category())
    service.repository.by_name.return_value = product()

    with pytest.raises(APIError, match="already exists"):
        await service.create_product(1, product_input())

    service.session.commit.assert_not_awaited()


@pytest.mark.anyio
async def test_update_product_changes_fields(service):
    existing = product()
    service.repository.get.return_value = existing

    updated = await service.update_product(existing.id, product_input("Laptop Ultra"))

    assert updated is existing
    assert updated.name == "Laptop Ultra"
    service.session.commit.assert_awaited_once()


@pytest.mark.anyio
async def test_update_duplicate_name_rolls_back(service):
    service.repository.get.return_value = product()
    service.session.commit.side_effect = IntegrityError("UPDATE", {}, Exception())

    with pytest.raises(APIError, match="already exists"):
        await service.update_product(2, product_input("Laptop Ultra"))

    service.session.rollback.assert_awaited_once()


@pytest.mark.anyio
async def test_update_and_delete_missing_product_do_not_write(service):
    service.repository.get.return_value = None

    with pytest.raises(APIError, match="Product with id: 9 not found"):
        await service.update_product(9, product_input())
    with pytest.raises(APIError, match="Product with id: 9 not found"):
        await service.delete_product(9)

    service.session.commit.assert_not_awaited()


@pytest.mark.anyio
async def test_delete_product_returns_detail_and_commits(service):
    existing = product()
    service.repository.get.return_value = existing

    detail = await service.delete_product(existing.id)

    assert detail.id == existing.id
    assert detail.category.id == 1
    service.session.delete.assert_awaited_once_with(existing)
    service.session.commit.assert_awaited_once()
