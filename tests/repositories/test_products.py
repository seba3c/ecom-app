import pytest

from app.repositories.products import ProductRepository
from tests.repositories.factories import make_category, make_product


@pytest.mark.anyio
async def test_get_by_name_and_missing_product(db_session):
    product = await make_product(db_session)
    repository = ProductRepository(db_session)

    assert (await repository.get(product.id)).id == product.id
    assert (await repository.get(product.id, lock=True)).id == product.id
    assert (await repository.by_name(product.name)).id == product.id
    assert await repository.get(999) is None
    assert await repository.by_name("Missing") is None


@pytest.mark.anyio
async def test_page_filters_keyword_and_category_before_counting(db_session):
    electronics = await make_category(db_session)
    books = await make_category(db_session, "Books")
    await make_product(db_session, electronics, "Laptop Pro")
    match = await make_product(
        db_session, electronics, "Phone Plus", description="Pocket DEVICE"
    )
    await make_product(db_session, books, "Device Guide")
    repository = ProductRepository(db_session)

    page = await repository.page(
        0, 1, "name", "desc", keyword="device", category_id=electronics.id
    )

    assert [item.id for item in page["content"]] == [match.id]
    assert page["total_elements"] == 1
    assert page["total_pages"] == 1
    assert page["last_page"] is True


@pytest.mark.anyio
async def test_page_applies_offset_and_sort_order(db_session):
    category = await make_category(db_session)
    await make_product(db_session, category, "Laptop Pro")
    second = await make_product(db_session, category, "Phone Plus")
    repository = ProductRepository(db_session)

    page = await repository.page(1, 1, "name", "asc")

    assert [item.id for item in page["content"]] == [second.id]
    assert page["total_elements"] == 2
    assert page["last_page"] is True
