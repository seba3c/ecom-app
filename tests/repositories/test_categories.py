import pytest

from app.schemas.category import CategoryCreate, CategoryUpdate
from app.schemas.common import PaginationParams


@pytest.mark.anyio
async def test_list_empty(repository):
    categories = await repository.list()
    assert categories == []


@pytest.mark.anyio
async def test_list_paginated(repository):
    create1 = CategoryCreate(name="Category 1")
    create2 = CategoryCreate(name="Category 2")
    create3 = CategoryCreate(name="Category 3")
    await repository.create(create1)
    await repository.create(create2)
    await repository.create(create3)

    params = PaginationParams(limit=1, offset=1)
    result = await repository.paginated_list(params)
    assert result.total == 3
    assert len(result.items) == 1
    assert result.items[0].name == "Category 2"


@pytest.mark.anyio
async def test_list_paginated_with_default_params(repository):
    create1 = CategoryCreate(name="Category 1")
    create2 = CategoryCreate(name="Category 2")
    await repository.create(create1)
    await repository.create(create2)

    result = await repository.paginated_list()
    assert result.total == 2
    assert len(result.items) == 2
    assert result.limit == 50
    assert result.offset == 0


@pytest.mark.anyio
async def test_create_category(repository):
    create = CategoryCreate(name="Test Category")
    category = await repository.create(create)
    assert category.name == "Test Category"
    assert category.id is not None


@pytest.mark.anyio
async def test_create_category_duplicate(repository):
    from app.core.exceptions import CategoryDuplicatedError

    create = CategoryCreate(name="Duplicate")
    await repository.create(create)

    with pytest.raises(CategoryDuplicatedError) as exc_info:
        await repository.create(create)
    assert exc_info.value.detail == "Category with this name already exists."


@pytest.mark.anyio
async def test_get_category(repository):
    create = CategoryCreate(name="Test Category")
    category = await repository.create(create)

    fetched = await repository.get(category.id)
    assert fetched is not None
    assert fetched.name == "Test Category"
    assert fetched.id == category.id


@pytest.mark.anyio
async def test_get_category_not_found(repository):
    fetched = await repository.get(1)
    assert fetched is None


@pytest.mark.anyio
async def test_update_category(repository):
    create = CategoryCreate(name="Original")
    category = await repository.create(create)

    update = CategoryUpdate(name="Updated")
    updated = await repository.update(category.id, update)
    assert updated is not None
    assert updated.name == "Updated"
    assert updated.id == category.id


@pytest.mark.anyio
async def test_update_category_not_found(repository):
    update = CategoryUpdate(name="Updated")
    result = await repository.update(1, update)
    assert result is None


@pytest.mark.anyio
async def test_delete_category(repository):
    create = CategoryCreate(name="To Delete")
    category = await repository.create(create)

    deleted = await repository.delete(category.id)
    assert deleted is True
    assert await repository.get(category.id) is None


@pytest.mark.anyio
async def test_delete_category_not_found(repository):
    deleted = await repository.delete(1)
    assert deleted is False


@pytest.mark.anyio
async def test_page_sorts_and_counts_categories(repository):
    await repository.create(CategoryCreate(name="Electronics"))
    await repository.create(CategoryCreate(name="Books"))

    page = await repository.page(0, 1, "name", "desc")

    assert [item.name for item in page["content"]] == ["Electronics"]
    assert page["total_elements"] == 2
    assert page["total_pages"] == 2
    assert page["last_page"] is False


@pytest.mark.anyio
async def test_bulk_create_persists_all_categories(repository):
    await repository.bulk_create(
        [CategoryCreate(name="Books"), CategoryCreate(name="Electronics")]
    )

    assert {item.name for item in await repository.list()} == {
        "Books",
        "Electronics",
    }


@pytest.mark.anyio
async def test_update_duplicate_rolls_back(repository):
    first = await repository.create(CategoryCreate(name="Books"))
    second = await repository.create(CategoryCreate(name="Electronics"))
    second_id = second.id

    from app.core.exceptions import CategoryDuplicatedError

    with pytest.raises(CategoryDuplicatedError):
        await repository.update(second_id, CategoryUpdate(name=first.name))

    assert (await repository.get(second_id)).name == "Electronics"
