import pytest


@pytest.mark.anyio
async def test_process_time_header(client):
    response = await client.get("/api/health")
    assert response.status_code == 200
    assert "X-Process-Time" in response.headers

    process_time = float(response.headers["X-Process-Time"])
    assert process_time >= 0.0
