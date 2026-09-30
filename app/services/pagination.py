from collections.abc import Collection

from app.core.exceptions import APIError


def validate_pagination(
    page_number: int, page_size: int, sort_by: str, sortable: Collection[str]
) -> None:
    errors = {}
    if page_number < 0:
        errors["pageNumber"] = "Page index must not be less than zero"
    if page_size < 1:
        errors["pageSize"] = "Page size must not be less than one"
    if sort_by not in sortable:
        errors["sortBy"] = "Invalid sort field"
    if errors:
        raise APIError(400, next(iter(errors.values())))
