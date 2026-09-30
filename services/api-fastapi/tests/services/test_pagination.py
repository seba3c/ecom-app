import pytest

from app.core.exceptions import APIError
from app.services.pagination import validate_pagination


def test_validate_pagination_accepts_valid_sort_and_range():
    assert validate_pagination(0, 50, "name", {"id", "name"}) is None


@pytest.mark.parametrize(
    ("page_number", "page_size", "sort_by", "message"),
    [
        (-1, 10, "name", "Page index must not be less than zero"),
        (0, 0, "name", "Page size must not be less than one"),
        (0, 10, "unknown", "Invalid sort field"),
    ],
)
def test_validate_pagination_rejects_invalid_input(
    page_number, page_size, sort_by, message
):
    with pytest.raises(APIError, match=message) as exc:
        validate_pagination(page_number, page_size, sort_by, {"name"})

    assert exc.value.status_code == 400
