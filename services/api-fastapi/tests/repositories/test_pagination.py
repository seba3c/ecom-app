import pytest
from sqlalchemy import select

from app.models.category import Category
from app.repositories.pagination import paginate
from tests.repositories.factories import make_category


@pytest.mark.anyio
async def test_empty_page_reports_zero_totals(db_session):
    page = await paginate(db_session, select(Category), 0, 2, Category.name, "asc")

    assert page == {
        "content": [],
        "page_number": 0,
        "page_size": 2,
        "total_elements": 0,
        "total_pages": 0,
        "last_page": True,
    }


@pytest.mark.anyio
async def test_page_counts_filtered_rows_and_descending_sort(db_session):
    await make_category(db_session, "Books")
    await make_category(db_session, "Electronics")
    await make_category(db_session, "Food")
    query = select(Category).where(Category.name != "Food")

    page = await paginate(db_session, query, 0, 1, Category.name, "DESC")

    assert [item.name for item in page["content"]] == ["Electronics"]
    assert page["total_elements"] == 2
    assert page["total_pages"] == 2
    assert page["last_page"] is False
