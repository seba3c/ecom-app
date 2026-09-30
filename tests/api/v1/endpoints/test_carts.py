import pytest

from tests.api.v1.endpoints.contract_helpers import category, headers, product


@pytest.mark.anyio
async def test_cart_add_update_and_admin_listing(client):
    category_id = await category(client)
    product_id = await product(client, category_id)
    cart = await client.get("/api/my_cart", headers=headers("user"))
    assert cart.status_code == 200
    assert cart.json()["cartItems"] == []
    assert cart.json()["totalPrice"] == 0
    assert (
        await client.post(
            f"/api/my_cart/{product_id}/quantity/0", headers=headers("user")
        )
    ).status_code == 400
    added = await client.post(
        f"/api/my_cart/{product_id}/quantity/2", headers=headers("user")
    )
    assert added.status_code == 201
    assert added.json()["totalPrice"] == 180
    assert added.json()["cartItems"][0]["quantity"] == 2
    assert (
        await client.post(
            f"/api/my_cart/{product_id}/quantity/2", headers=headers("user")
        )
    ).status_code == 400
    assert (
        await client.put(
            f"/api/my_cart/{product_id}/quantity/6", headers=headers("user")
        )
    ).status_code == 400
    changed = await client.put(
        f"/api/my_cart/{product_id}/quantity/3", headers=headers("user")
    )
    assert changed.json()["totalPrice"] == 270
    carts = await client.get("/api/admin/carts", headers=headers("admin"))
    assert carts.status_code == 200
    assert carts.json()["content"][0]["id"] == cart.json()["id"]
    assert (
        await client.get("/api/admin/carts", headers=headers("user"))
    ).status_code == 403


@pytest.mark.anyio
async def test_cart_remove_clears_items_and_total(client):
    category_id = await category(client)
    product_id = await product(client, category_id)
    await client.post(f"/api/my_cart/{product_id}/quantity/2", headers=headers("user"))

    removed = await client.delete(f"/api/my_cart/{product_id}", headers=headers("user"))

    assert removed.status_code == 200
    assert removed.json()["cartItems"] == []
    assert removed.json()["totalPrice"] == 0
