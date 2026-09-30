import pytest

from tests.api.v1.endpoints.contract_helpers import (
    ADDRESS,
    ORDER,
    PRODUCT,
    category,
    headers,
    product,
)


@pytest.mark.anyio
async def test_place_order_updates_stock_and_clears_cart(client):
    category_id = await category(client)
    product_id = await product(client, category_id)
    await client.post(f"/api/my_cart/{product_id}/quantity/3", headers=headers("user"))
    address = await client.post("/api/addresses", json=ADDRESS, headers=headers("user"))
    order = await client.post(
        "/api/orders",
        json={**ORDER, "addressId": address.json()["id"]},
        headers=headers("user"),
    )
    assert order.status_code == 201, order.text
    assert order.json()["status"] == "PENDING"
    assert order.json()["totalAmount"] == 270
    assert order.json()["payment"]["pgName"] == "Stripe"
    assert order.json()["items"][0]["quantity"] == 3
    assert (await client.get(f"/api/public/products/{product_id}")).json()[
        "quantity"
    ] == 2
    assert (await client.get("/api/my_cart", headers=headers("user"))).json()[
        "cartItems"
    ] == []
    assert (
        await client.post(
            "/api/orders",
            json={**ORDER, "addressId": address.json()["id"]},
            headers=headers("user"),
        )
    ).status_code == 400


@pytest.mark.anyio
async def test_missing_address_keeps_cart_unchanged(client):
    category_id = await category(client)
    product_id = await product(client, category_id)
    await client.post(f"/api/my_cart/{product_id}/quantity/2", headers=headers("user"))
    missing_address = await client.post(
        "/api/orders", json={**ORDER, "addressId": 999}, headers=headers("user")
    )
    assert missing_address.status_code == 404
    assert (await client.get("/api/my_cart", headers=headers("user"))).json()[
        "totalPrice"
    ] == 180


@pytest.mark.anyio
async def test_order_rejects_another_users_address(client):
    category_id = await category(client)
    product_id = await product(client, category_id)
    await client.post(f"/api/my_cart/{product_id}/quantity/1", headers=headers("user"))
    address = await client.post(
        "/api/addresses", json=ADDRESS, headers=headers("seller")
    )
    result = await client.post(
        "/api/orders",
        json={**ORDER, "addressId": address.json()["id"]},
        headers=headers("user"),
    )
    assert result.status_code == 404
    assert (await client.get(f"/api/public/products/{product_id}")).json()[
        "quantity"
    ] == 5


@pytest.mark.anyio
async def test_order_stock_failure_is_atomic(client):
    category_id = await category(client)
    first_id = await product(client, category_id)
    second_data = {**PRODUCT, "name": "Phone Plus"}
    second = await client.post(
        f"/api/admin/categories/{category_id}/products",
        json=second_data,
        headers=headers("admin"),
    )
    second_id = second.json()["id"]
    await client.post(f"/api/my_cart/{first_id}/quantity/2", headers=headers("user"))
    await client.post(f"/api/my_cart/{second_id}/quantity/2", headers=headers("user"))
    await client.put(
        f"/api/admin/products/{second_id}",
        json={**second_data, "quantity": 1},
        headers=headers("admin"),
    )
    address = await client.post("/api/addresses", json=ADDRESS, headers=headers("user"))
    result = await client.post(
        "/api/orders",
        json={**ORDER, "addressId": address.json()["id"]},
        headers=headers("user"),
    )
    assert result.status_code == 400
    assert result.json() == {"message": "Insufficient stock for product: Phone Plus"}
    assert (await client.get(f"/api/public/products/{first_id}")).json()[
        "quantity"
    ] == 5
    assert (
        len(
            (await client.get("/api/my_cart", headers=headers("user"))).json()[
                "cartItems"
            ]
        )
        == 2
    )
