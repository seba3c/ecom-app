from datetime import datetime, timezone
from unittest.mock import AsyncMock

import pytest

from app.schemas.category import Category, CategoryCreate, CategoryUpdate
from app.schemas.common import PaginationParams
from app.services.category_service import CategoryService


def _category(id: int, name: str) -> Category:
    now = datetime.now(timezone.utc)
    return Category(id=id, name=name, created_at=now, updated_at=now)


@pytest.fixture
def service():
    mock_repo = AsyncMock()
    return CategoryService(repository=mock_repo)


@pytest.mark.anyio
async def test_list_categories(service):
    mock_result = AsyncMock()
    mock_result.items = [_category(id=1, name="Category 1")]
    mock_result.total = 1
    service.repository.paginated_list.return_value = mock_result
    params = PaginationParams(limit=50, offset=0)
    result = await service.list_categories(params)
    assert result == mock_result
    service.repository.paginated_list.assert_awaited_once_with(params)


@pytest.mark.anyio
async def test_list_categories_with_default_params(service):
    mock_result = AsyncMock()
    mock_result.items = [_category(id=1, name="Category 1")]
    mock_result.total = 1
    service.repository.paginated_list.return_value = mock_result
    result = await service.list_categories()
    assert result == mock_result
    service.repository.paginated_list.assert_awaited_once()
    call_args = service.repository.paginated_list.call_args[0][0]
    assert call_args.limit == 50
    assert call_args.offset == 0


@pytest.mark.anyio
async def test_get_category(service):
    category_id = 1
    mock_category = _category(id=category_id, name="Category 1")
    service.repository.get.return_value = mock_category
    result = await service.get_category(category_id)
    assert result == mock_category
    service.repository.get.assert_awaited_once_with(category_id)


@pytest.mark.anyio
async def test_get_category_not_found(service):
    from app.core.exceptions import CategoryNotFoundError

    category_id = 1
    service.repository.get.return_value = None
    with pytest.raises(CategoryNotFoundError) as exc_info:
        await service.get_category(category_id)
    assert exc_info.value.detail == "Category not found"


@pytest.mark.anyio
async def test_create_category(service):
    create_data = CategoryCreate(name="New Category")
    mock_category = _category(id=1, name="New Category")
    service.repository.create.return_value = mock_category
    result = await service.create_category(create_data)
    assert result == mock_category
    service.repository.create.assert_awaited_once_with(create_data)


@pytest.mark.anyio
async def test_create_category_duplicate(service):
    from app.core.exceptions import CategoryDuplicatedError

    create_data = CategoryCreate(name="Duplicate")
    service.repository.create.side_effect = CategoryDuplicatedError()

    with pytest.raises(CategoryDuplicatedError) as exc_info:
        await service.create_category(create_data)
    assert exc_info.value.detail == "Category with this name already exists."


@pytest.mark.anyio
async def test_update_category(service):
    category_id = 1
    update_data = CategoryUpdate(name="Updated")
    mock_category = _category(id=category_id, name="Updated")
    service.repository.update.return_value = mock_category
    result = await service.update_category(category_id, update_data)
    assert result == mock_category
    service.repository.update.assert_awaited_once_with(category_id, update_data)


@pytest.mark.anyio
async def test_update_category_not_found(service):
    from app.core.exceptions import CategoryNotFoundError

    category_id = 1
    update_data = CategoryUpdate(name="Updated")
    service.repository.update.return_value = None
    with pytest.raises(CategoryNotFoundError) as exc_info:
        await service.update_category(category_id, update_data)
    assert exc_info.value.detail == "Category not found"


@pytest.mark.anyio
async def test_delete_category(service):
    category_id = 1
    service.repository.delete.return_value = True
    result = await service.delete_category(category_id)
    assert result is None
    service.repository.delete.assert_awaited_once_with(category_id)


@pytest.mark.anyio
async def test_delete_category_not_found(service):
    from app.core.exceptions import CategoryNotFoundError

    category_id = 1
    service.repository.delete.return_value = False
    with pytest.raises(CategoryNotFoundError) as exc_info:
        await service.delete_category(category_id)
    assert exc_info.value.detail == "Category not found"
