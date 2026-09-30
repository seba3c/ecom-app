import pytest

from tests.api.v1.endpoints.contract_helpers import ADDRESS, headers


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
