# FastAPI Agent Context

## Commands

```bash
make setup        # install and synchronize dependencies
make run          # start the development database, migrate, and run Uvicorn
make test         # run tests against the Docker MySQL test database
make code-format  # apply Ruff lint fixes and formatting
uv run pytest -v  # run tests directly with in-memory SQLite
```

## Database

- `make db-up` starts the development MySQL service on port 3306.
- `make db-down` stops the service and preserves its volume.
- `make db-reset` stops the service and deletes its volume.
- `make migrate` and `make migrate-rollback` apply or revert Alembic migrations.
- `make test` starts the MySQL test service on port 3307 and sets `TEST_DATABASE_URL`.
- Direct pytest runs use in-memory SQLite unless `TEST_DATABASE_URL` is set.

## Architecture

- `app/main.py` creates and configures the FastAPI application.
- `app/api/v1/router.py` assembles the routes rooted at `/api`.
- `app/api/v1/endpoints/` contains route handlers.
- `app/schemas/` and `app/models/` contain Pydantic schemas and SQLAlchemy tables.
- `app/services/` contains business logic; `app/repositories/` contains data access.
- `app/core/config.py` defines settings and reads `.env`.

Preserve the shared behavior defined in `contracts/openapi/openapi.yaml`. Keep backend-specific routes clearly separated from that contract.

## Naming

- Use plural entity names for endpoint and repository modules, such as `products.py`.
- Use singular entity names for model and schema modules, such as `product.py`.
- Name business logic modules `<entity>_service.py`.
- Place tests in the matching test package and name them after the module under test.

## Adding an Endpoint

1. Add the handler to the appropriate module under `app/api/v1/endpoints/`.
2. Register a new router in `app/api/v1/router.py` when needed.
3. Add API tests under `tests/api/v1/endpoints/` using `httpx.AsyncClient` with `ASGITransport`.
4. If the endpoint is portable, update and verify the shared OpenAPI contract and both backends.

## Testing

- Async API tests use `httpx.AsyncClient` with `ASGITransport`; no live server is required.
- `pytest-anyio` runs marked async tests using the configured async backends.
- Use `/api/health` for the FastAPI-specific health endpoint.
- Prefer focused tests while developing, then run the full suite.

## Tooling

- Run `make code-format` to apply Ruff lint fixes and formatting.
- Install or run repository hooks with `make pre-commit-install` and `make pre-commit-run`.
- Manage dependencies through uv and `pyproject.toml`; do not edit `uv.lock` manually.
