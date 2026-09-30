import pytest

from tests.api.v1.endpoints.contract_helpers import PRODUCT, category, headers, product


@pytest.mark.anyio
async def test_product_contract(client):
    category_id = await category(client)
    product_id = await product(client, category_id)
    detail = await client.get(f"/api/public/products/{product_id}")
    assert detail.status_code == 200
    assert detail.json() == {
        "id": product_id,
        **PRODUCT,
        "category": {"id": category_id, "name": "Electronics"},
    }
    for url in (
        "/api/public/products",
        "/api/public/products/keyword/laptop",
        f"/api/public/categories/{category_id}/products",
    ):
        page = await client.get(url)
        assert page.status_code == 200
        assert page.json()["content"][0]["id"] == product_id
        assert page.json()["totalElements"] == 1
    updated = await client.put(
        f"/api/admin/products/{product_id}",
        json={**PRODUCT, "name": "Laptop Ultra"},
        headers=headers("admin"),
    )
    assert updated.status_code == 200
    assert updated.json()["name"] == "Laptop Ultra"
    deleted = await client.delete(
        f"/api/admin/products/{product_id}", headers=headers("admin")
    )
    assert deleted.status_code == 200
    assert deleted.json()["id"] == product_id
    assert (await client.get(f"/api/public/products/{product_id}")).status_code == 404


@pytest.mark.anyio
async def test_product_validation_and_authorization(client):
    category_id = await category(client)
    url = f"/api/admin/categories/{category_id}/products"
    assert (await client.post(url, json=PRODUCT)).status_code == 401
    assert (
        await client.post(url, json=PRODUCT, headers=headers("seller"))
    ).status_code == 403
    invalid = await client.post(
        url, json={**PRODUCT, "quantity": -1}, headers=headers("admin")
    )
    assert invalid.status_code == 400
    assert invalid.json() == {"quantity": "must be greater than or equal to 0"}
    await product(client, category_id)
    duplicate = await client.post(url, json=PRODUCT, headers=headers("admin"))
    assert duplicate.status_code == 400
    assert duplicate.json() == {
        "message": "Product with the name Laptop Pro already exists"
    }
    assert (await client.get("/api/public/categories/999/products")).status_code == 404
    assert (await client.get("/api/public/products/999")).status_code == 404


@pytest.mark.anyio
async def test_product_page_sort_and_keyword(client):
    category_id = await category(client)
    await product(client, category_id)
    await client.post(
        f"/api/admin/categories/{category_id}/products",
        json={**PRODUCT, "name": "Phone Plus", "description": "Pocket device"},
        headers=headers("admin"),
    )
    first = await client.get(
        "/api/public/products",
        params={"pageNumber": 0, "pageSize": 1, "sortBy": "name", "sortOrder": "desc"},
    )
    assert first.json()["content"][0]["name"] == "Phone Plus"
    assert first.json()["lastPage"] is False
    second = await client.get(
        "/api/public/products",
        params={"pageNumber": 1, "pageSize": 1, "sortBy": "name", "sortOrder": "desc"},
    )
    assert second.json()["content"][0]["name"] == "Laptop Pro"
    assert second.json()["lastPage"] is True
    keyword = await client.get("/api/public/products/keyword/POCKET")
    assert [item["name"] for item in keyword.json()["content"]] == ["Phone Plus"]


@pytest.mark.anyio
async def test_product_name_two_character_boundary(client):
    category_id = await category(client)
    url = f"/api/admin/categories/{category_id}/products"
    admin = headers("admin")
    created = await client.post(url, json={**PRODUCT, "name": "TV"}, headers=admin)
    assert created.status_code == 201
    updated = await client.put(
        f"/api/admin/products/{created.json()['id']}",
        json={**PRODUCT, "name": "PC"},
        headers=admin,
    )
    assert updated.status_code == 200
    for method_url, method in (
        (url, client.post),
        (f"/api/admin/products/{created.json()['id']}", client.put),
    ):
        invalid = await method(method_url, json={**PRODUCT, "name": "P"}, headers=admin)
        assert invalid.status_code == 400
        assert invalid.json() == {
            "name": "Product name must have at least 2 characters"
        }
    blank = await client.post(url, json={**PRODUCT, "name": "  "}, headers=admin)
    assert blank.status_code == 400
