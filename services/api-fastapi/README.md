# FastAPI ecommerce API

A FastAPI implementation of the ecommerce API defined by the monorepo's [shared OpenAPI contract](../../contracts/openapi/openapi.yaml), using [uv](https://docs.astral.sh/uv/) for dependency management.

## Requirements

- Python 3.12+
- [uv](https://docs.astral.sh/uv/)
- Docker with Compose for the development MySQL database and Docker-based test workflows

## Setup

```bash
make setup
make migrate
```

## Quick Commands

| Command | Description |
| --- | --- |
| `make setup` | Install and synchronize dependencies |
| `make run` | Run the API with Uvicorn auto-reload |
| `make test` | Run tests against the Docker MySQL test database |
| `make code-format` | Fix Ruff lint issues and format the code |
| `make migrate` | Apply database migrations |
| `make docker-run` | Start the API with Docker Compose |

## Run

```bash
make run
```

- API base URL: http://localhost:8000/api
- Swagger UI: http://localhost:8000/docs
- ReDoc: http://localhost:8000/redoc

The run target starts the development database, applies Alembic migrations, and launches Uvicorn with auto-reload. `make run-fastapi` is also available for the FastAPI CLI development server.

## Test

```bash
uv run pytest -v
```

The direct pytest command uses in-memory SQLite. `make test` starts the Docker MySQL test database and sets `TEST_DATABASE_URL` before running the same API tests. Use `make docker-test` to run the test process itself in Docker.

## API and Authentication

Routes are rooted at `/api` and implement the paths, payloads, pagination, authentication, and authorization behavior in the [shared OpenAPI contract](../../contracts/openapi/openapi.yaml). FastAPI-specific diagnostics, including `GET /api/health`, are outside the portable contract.

Sign in with `POST /api/auth/signin` using `{"username":"user","password":"userpass"}`. The response contains `jwtToken` and sets the HTTP-only `ecommerce-app` cookie for `/api`. Protected routes also accept `Authorization: Bearer <jwtToken>`; the cookie takes precedence when both credentials are present. Routes under `/api/admin/**` require `ROLE_ADMIN`.

Development startup creates `user/userpass`, `seller/sellerpass`, `seller2/seller2pass`, `seller3/seller3pass`, and `admin/adminpass`. Set `JWT_SECRET` to a strong private value before production startup; the development default is rejected in production. `JWT_EXPIRATION_SECONDS` defaults to `86400`.

Run `make seed` to add 10 categories and 100 products to the development MySQL database. The command can be rerun safely; existing categories and products with matching names are left unchanged. On a fresh database, products are split evenly between `seller2` and `seller3`.

## Commands

### Dependencies and database

```bash
make setup             # Install and synchronize dependencies
make db-up             # Start the development database
make db-down           # Stop the database and preserve its volume
make db-reset          # Stop the database and delete its volume
make migrate           # Apply all Alembic migrations
make migrate-rollback  # Roll back the latest migration
make seed              # Seed the development catalog
```

Alembic autogeneration compares the models with the database named by `DATABASE_URL`.
To regenerate a baseline migration, point `DATABASE_URL` at an empty database;
running `alembic revision --autogenerate` against an already migrated database
produces an empty revision. The current single baseline is intended for a fresh
database. An existing database stamped with the removed migration IDs must be
reconciled before `make migrate` can use this baseline; recreate a disposable
development database with `make db-reset` only if its data can be discarded.

### Development and quality

```bash
make run               # Uvicorn with auto-reload
make run-fastapi       # FastAPI CLI development server
make test              # Test against Docker MySQL
make code-format       # Apply Ruff lint fixes and formatting
make pre-commit-install
make pre-commit-run
```

## Project Structure

```text
app/
├── api/v1/endpoints/  # HTTP routes
├── core/              # Settings, middleware, and errors
├── models/            # SQLAlchemy tables
├── schemas/           # Pydantic request and response models
├── repositories/      # Data access
└── services/          # Business rules
alembic/versions/      # Database migrations
tests/                 # API, repository, and service tests
```

## Configuration

Settings are defined in `app/core/config.py` with `pydantic-settings`. Create a `.env` file to override defaults:

```env
DATABASE_URL=mysql+aiomysql://fa_ecom_user:fa_ecom_pass@localhost:3306/fa_ecom
JWT_SECRET=replace-with-a-private-random-secret
JWT_EXPIRATION_SECONDS=86400
ENVIRONMENT=development
```
