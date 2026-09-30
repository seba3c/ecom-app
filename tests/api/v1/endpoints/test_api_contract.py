import pytest

from app.api.dependencies import get_settings
from app.services.auth_service import issue_token

PRODUCT = {
    "name": "Laptop Pro",
    "description": "Portable computer",
    "quantity": 5,
    "price": 100.00,
    "discount": 10.00,
}
ADDRESS = {
    "streetLine1": "123 Main St",
    "streetLine2": None,
    "city": "New York",
    "state": "New York",
    "country": "USA",
    "zipCode": "10001",
}
ORDER = {
    "addressId": 1,
    "paymentMethod": "CARD",
    "pgName": "Stripe",
    "pgPaymentId": "pay_1",
    "pgStatus": "paid",
    "pgResponse": "ok",
}


def headers(username: str):
    return {"Authorization": f"Bearer {issue_token(username, get_settings())}"}


async def category(client):
    response = await client.post(
        "/api/admin/categories", json={"name": "Electronics"}, headers=headers("admin")
    )
    assert response.status_code == 201, response.text
    return response.json()["id"]


async def product(client, category_id):
    response = await client.post(
        f"/api/admin/categories/{category_id}/products",
        json=PRODUCT,
        headers=headers("admin"),
    )
    assert response.status_code == 201, response.text
    return response.json()["id"]


@pytest.mark.anyio
async def test_auth_contract(client):
    bad = await client.post(
        "/api/auth/signin", json={"username": "user", "password": "wrong"}
    )
    assert bad.status_code == 401
    assert bad.json() == {"message": "Bad credentials", "status": False}
    signin = await client.post(
        "/api/auth/signin", json={"username": "user", "password": "userpass"}
    )
    assert signin.status_code == 200
    assert signin.json()["roles"] == ["ROLE_USER"]
    assert signin.json()["jwtToken"]
    assert "ecommerce-app=" in signin.headers["set-cookie"]
    assert "HttpOnly" in signin.headers["set-cookie"]
    assert (await client.get("/api/auth/username")).text == "user"
    current = await client.get("/api/auth/user")
    assert current.json()["jwtToken"] is None
    signed_out = await client.post("/api/auth/signout")
    assert signed_out.status_code == 200
    assert signed_out.json() == {"message": "User signed out successfully!"}
    assert (await client.get("/api/auth/user")).json() == {
        "message": "No user details found"
    }
    assert (await client.get("/api/auth/username")).text == ""
    assert (await client.post("/api/auth/signout")).status_code == 400


@pytest.mark.anyio
async def test_signup_roles_and_duplicates(client):
    data = {
        "username": "newuser",
        "email": "new@example.com",
        "password": "password1",
        "roles": ["seller"],
    }
    signup = await client.post("/api/auth/signup", json=data)
    assert signup.status_code == 200
    assert signup.json() == {"message": "User registered successfully!"}
    signin = await client.post(
        "/api/auth/signin", json={"username": "newuser", "password": "password1"}
    )
    assert signin.json()["roles"] == ["ROLE_SELLER"]
    duplicate = await client.post("/api/auth/signup", json=data)
    assert duplicate.status_code == 400
    assert duplicate.json() == {"message": "Error: Username is already taken!"}
    assert (
        await client.post("/api/auth/signup", json={**data, "username": "otheruser"})
    ).json() == {"message": "Error: Email is already taken!"}
    invalid_roles = await client.post(
        "/api/auth/signup",
        json={
            **data,
            "username": "thirduser",
            "email": "third@example.com",
            "roles": ["admin"],
        },
    )
    assert invalid_roles.status_code == 400
    assert invalid_roles.json() == {"roles": "Each role must be one of: user, seller"}


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
async def test_address_contract_and_ownership(client):
    assert (await client.get("/api/addresses")).status_code == 401
    created = await client.post("/api/addresses", json=ADDRESS, headers=headers("user"))
    assert created.status_code == 201
    address_id = created.json()["id"]
    assert created.json() == {"id": address_id, **ADDRESS}
    assert (await client.get("/api/addresses", headers=headers("user"))).json()[
        "content"
    ] == [created.json()]
    assert (
        await client.get(f"/api/addresses/{address_id}", headers=headers("user"))
    ).status_code == 200
    other = await client.get(f"/api/addresses/{address_id}", headers=headers("seller"))
    assert other.status_code == 404
    assert other.json() == {"message": f"Address with id: {address_id} not found"}
    changed = await client.put(
        f"/api/addresses/{address_id}",
        json={**ADDRESS, "city": "Boston"},
        headers=headers("user"),
    )
    assert changed.status_code == 200
    assert changed.json()["city"] == "Boston"
    assert (
        await client.delete(f"/api/addresses/{address_id}", headers=headers("user"))
    ).status_code == 204
    assert (
        await client.get(f"/api/addresses/{address_id}", headers=headers("user"))
    ).status_code == 404


@pytest.mark.anyio
async def test_cart_and_order_contract(client):
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
async def test_cart_remove_and_failed_order_preserve_state(client):
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
    removed = await client.delete(f"/api/my_cart/{product_id}", headers=headers("user"))
    assert removed.status_code == 200
    assert removed.json()["cartItems"] == []
    assert removed.json()["totalPrice"] == 0


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


@pytest.mark.anyio
async def test_cookie_precedes_bearer_token(client):
    signin = await client.post(
        "/api/auth/signin", json={"username": "user", "password": "userpass"}
    )
    assert signin.status_code == 200
    current = await client.get("/api/auth/user", headers=headers("admin"))
    assert current.json()["username"] == "user"
