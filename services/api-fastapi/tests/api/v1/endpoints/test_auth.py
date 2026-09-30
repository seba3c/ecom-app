import pytest

from tests.api.v1.endpoints.contract_helpers import headers


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
async def test_cookie_precedes_bearer_token(client):
    signin = await client.post(
        "/api/auth/signin", json={"username": "user", "password": "userpass"}
    )
    assert signin.status_code == 200
    current = await client.get("/api/auth/user", headers=headers("admin"))
    assert current.json()["username"] == "user"
