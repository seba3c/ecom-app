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
