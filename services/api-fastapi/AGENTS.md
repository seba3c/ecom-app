# FastAPI Sandbox — Agent Context

## Commands

```bash
uv sync                                      # install / sync dependencies
uv run uvicorn app.main:app --reload         # start dev server
uv run pytest -v                             # run tests
uv add <package>                             # add runtime dependency
uv add --dev <package>                       # add dev dependency
```

## Database

- **`make db-up`** — starts the dev DB (`db`, port `3306`) and waits until it is healthy.
- **`make db-down`** — stops the dev DB container but preserves its volume.
- **`make db-reset`** — stops the dev DB container **and** deletes its volume for a completely fresh start.
- **`make run-uvicorn` / `make run-fastapi`** — automatically start the dev DB before launching the app.
- **`make test`** — overrides the `db` service with `docker-compose.test.yml` (test DB config), starts it, then runs pytest against `localhost:3307/fa_ecom_test`.
- **`make migrate`** — runs `alembic upgrade head` against the dev DB.
- **`make migrate-rollback`** — runs `alembic downgrade -1` against the dev DB.
- The Docker `test` service connects to `db:3306/fa_ecom_test` internally.

## Architecture

- **`app/main.py`** — FastAPI app factory; registers the `api/v1` router
- **`app/core/config.py`** — `pydantic-settings` `Settings` class; reads `.env` automatically via `SettingsConfigDict(env_file=".env")`
- **`app/api/dependencies.py`** — shared FastAPI dependencies; `get_settings()` is cached with `lru_cache`
- **`app/api/v1/router.py`** — aggregates all v1 endpoint routers
- **`app/api/v1/endpoints/`** — individual endpoint modules (health, json_tests, etc.)
- **`app/models/`** — database models (Pydantic schemas go in `app/schemas/`)
- **`app/services/`** — business logic layer
- **`app/repositories/`** — data access layer

## Naming

- Use plural entity names for endpoint and repository modules, such as `products.py` and `carts.py`.
- Use singular entity names for model and schema modules, such as `product.py` and `cart.py`.
- Name business logic modules `<entity>_service.py`, such as `cart_service.py`.
- Name tests after the module they cover within the matching test package: `test_carts.py` for an endpoint or repository module, and `test_cart_service.py` for a service module.
- Use descriptive feature or utility names for modules without an entity collection, such as `auth.py`, `health.py`, and `pagination.py`.

## Adding a New Endpoint

1. Create `app/api/v1/endpoints/<feature>.py` with an `APIRouter`
2. Import and register in `app/api/v1/router.py`: `api_router.include_router(feature.router, prefix="/<feature>", tags=["<feature>"])`
3. Add tests in `tests/api/v1/endpoints/test_<feature>.py` using the `AsyncClient` + `ASGITransport` pattern from `tests/api/v1/endpoints/test_health.py`

## Testing

- Tests use `httpx.AsyncClient` with `ASGITransport` (no live server needed).
- `pytest-anyio` runs every `@pytest.mark.anyio` test against **both asyncio and trio** automatically.
- Example pattern:

```python
@pytest.mark.anyio
async def test_health():
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as client:
        response = await client.get("/api/v1/health")
    assert response.status_code == 200
```

## Tooling

- **Lint / format**: `ruff` is installed as a dev dependency; pre-commit hooks run `ruff check --fix` and `ruff format` automatically on every commit.
- **Pre-commit**: Install hooks with `uv run pre-commit install`. Run manually across all files with `uv run pre-commit run --all-files`.
- **JSON optimization**: `orjson` is a runtime dependency (see `app/api/v1/json_tests.py` for usage patterns).
