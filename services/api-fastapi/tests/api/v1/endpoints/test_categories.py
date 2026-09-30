import json
from unittest.mock import patch

import pytest

from app.api.dependencies import get_settings
from app.services.auth_service import issue_token

PUBLIC = "/api/public/categories"
ADMIN = "/api/admin/categories"


def admin_headers():
    return {"Authorization": f"Bearer {issue_token('admin', get_settings())}"}


@pytest.mark.anyio
async def test_category_contract(client):
    headers = admin_headers()
    with patch("app.api.v1.endpoints.categories.notify_category_created"):
        created = await client.post(
            ADMIN, json={"name": "Electronics"}, headers=headers
        )
    assert created.status_code == 201
    assert set(created.json()) == {"id", "name"}
    category_id = created.json()["id"]

    extra_get = await client.get(f"{ADMIN}/{category_id}", headers=headers)
    assert extra_get.status_code == 200
    assert extra_get.json()["name"] == "Electronics"

    listed = await client.get(PUBLIC, params={"pageNumber": 0, "pageSize": 1})
    assert listed.status_code == 200
    assert listed.json() == {
        "content": [{"id": category_id, "name": "Electronics"}],
        "pageNumber": 0,
        "pageSize": 1,
        "totalElements": 1,
        "totalPages": 1,
        "lastPage": True,
    }

    updated = await client.put(
        f"{ADMIN}/{category_id}", json={"name": "Appliances"}, headers=headers
    )
    assert updated.status_code == 200
    assert updated.json()["name"] == "Appliances"
    deleted = await client.delete(f"{ADMIN}/{category_id}", headers=headers)
    assert deleted.status_code == 200
    assert deleted.json() == updated.json()


@pytest.mark.anyio
async def test_category_errors_and_access(client):
    assert (await client.post(ADMIN, json={"name": "Electronics"})).status_code == 401
    assert (
        await client.post(
            ADMIN,
            json={"name": "Electronics"},
            headers={"Authorization": f"Bearer {issue_token('user', get_settings())}"},
        )
    ).status_code == 403
    headers = admin_headers()
    invalid = await client.post(ADMIN, json={"name": "bad"}, headers=headers)
    assert invalid.status_code == 400
    assert invalid.json() == {"name": "Category name must have at least 5 characters"}
    first = await client.post(ADMIN, json={"name": "Electronics"}, headers=headers)
    duplicate = await client.post(ADMIN, json={"name": "Electronics"}, headers=headers)
    assert duplicate.status_code == 400
    assert duplicate.json() == {
        "message": "Category with the name Electronics already exists"
    }
    missing = await client.delete(f"{ADMIN}/999", headers=headers)
    assert missing.status_code == 404
    assert missing.json() == {"message": "Category with id: 999 not found"}
    assert first.status_code == 201


@pytest.mark.anyio
async def test_category_extra_stream(client):
    await client.post(ADMIN, json={"name": "Electronics"}, headers=admin_headers())
    stream = await client.get(f"{PUBLIC}/stream")
    assert stream.status_code == 200
    assert json.loads(stream.text.strip())["name"] == "Electronics"
