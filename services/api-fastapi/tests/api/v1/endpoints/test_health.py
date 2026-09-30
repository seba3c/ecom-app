import pytest


@pytest.mark.anyio
async def test_health(client):
    response = await client.get("/api/health")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "ok"
    assert "timestamp" in data
    assert "version" in data
    assert "environment" in data
    assert "uptime_seconds" in data


@pytest.mark.anyio
async def test_health_response_includes_x_request_id_when_not_provided(client):
    response = await client.get("/api/health")
    assert response.status_code == 200
    assert "x-request-id" in response.headers
    assert response.headers["x-request-id"]


@pytest.mark.anyio
async def test_health_response_preserves_x_request_id_when_provided(client):
    request_id = "81510beb-5ab1-4a4f-a1fc-827065d60b11"
    response = await client.get("/api/health", headers={"X-Request-ID": request_id})
    assert response.status_code == 200
    assert response.headers["x-request-id"] == request_id
