# FastAPI ecommerce API

A FastAPI implementation of the [sb-ecom](https://github.com/seba3c/sb-ecom) HTTP API, using [uv](https://docs.astral.sh/uv/) for dependency management.

The Java API paths, payloads, pagination, authentication, and role checks are the client contract. The FastAPI database and JWT signing secret are independent from the Java deployment.

## Requirements

- Python 3.12+
- [uv](https://docs.astral.sh/uv/)
- MySQL 8 and Docker for the development server or MySQL test run

## Setup

```bash
uv sync
make migrate
```

## Quick Commands

| Command | Description |
|---------|-------------|
| `make run-uvicorn` | Run API with uvicorn auto-reload |
| `make run-fastapi` | Run via FastAPI CLI dev server |
| `make test` | Run tests against the Docker MySQL test database |
| `make code-check` | Check linting and formatting (read-only) |
| `make code-fix` | Fix linting issues |
| `make code-format` | Format code |
| `make dep-sync` | Sync dependencies |
| `make pre-commit-install` | Install git hooks |
| `make pre-commit-run` | Run hooks manually |
| `make docker-run` | Start API via Docker Compose |
| `make docker-test` | Run tests in Docker |

## Run

```bash
make run-uvicorn
```

- API: http://localhost:8000
- Swagger UI: http://localhost:8000/docs
- ReDoc: http://localhost:8000/redoc

## Test

```bash
uv run pytest -v
```

The default test database is in-memory SQLite. `make test` sets `TEST_DATABASE_URL` to the Docker MySQL test database. Both paths use the same API tests.

## API and authentication

Routes follow the Java API under `/api`: `/auth`, `/public/categories`, `/public/products`, `/admin/categories`, `/admin/products`, `/admin/carts`, `/addresses`, `/my_cart`, and `/orders`. FastAPI-only health and category stream routes remain available.

Sign in with `POST /api/auth/signin` using `{"username":"user","password":"userpass"}`. The response contains `jwtToken` and sets the HTTP-only `ecommerce-app` cookie for `/api`. Protected routes also accept `Authorization: Bearer <jwtToken>`. A cookie takes precedence if both credentials are present. `/api/admin/**` requires `ROLE_ADMIN`.

Development startup creates the same accounts as the Java project: `user/userpass`, `seller/sellerpass`, and `admin/adminpass`. Set `JWT_SECRET` to a strong private value before production startup; the development default is rejected in production. `JWT_EXPIRATION_SECONDS` defaults to 86400.

## Commands

### Dependency management

```bash
uv sync                              # Install / sync all dependencies
uv add <package>                     # Add a runtime dependency
uv add --dev <package>               # Add a dev dependency
```

### Run development server

```bash
uv run fastapi dev                   # FastAPI CLI with auto-reload
# or
uv run uvicorn app.main:app --reload # Uvicorn directly
```

- API: http://localhost:8000
- Swagger UI: http://localhost:8000/docs
- ReDoc: http://localhost:8000/redoc

### Lint & format

```bash
uv run pre-commit run --all-files    # Run all hooks on all files
uv run ruff check .                  # Lint only
uv run ruff check --fix .            # Lint with auto-fix
uv run ruff format .                 # Format only
```

### Pre-commit hooks

```bash
uv run pre-commit install            # Install git hooks (runs on every commit)
uv run pre-commit run --all-files    # Run manually across all files
```

## Project Structure

```
app/
├── api/v1/endpoints/          # HTTP routes
├── core/                      # Settings, middleware, errors
├── models/                    # SQLAlchemy tables
├── schemas/                   # Pydantic input and output models
├── repositories/              # Data access
└── services/                  # Business rules
alembic/versions/             # Database migrations
tests/                        # API, repository, and service tests
```

## Configuration

Settings are defined in `app/core/config.py` using `pydantic-settings`. Create a `.env` file to override defaults:

```env
DATABASE_URL=mysql+aiomysql://fa_ecom_user:fa_ecom_pass@localhost:3306/fa_ecom
JWT_SECRET=replace-with-a-private-random-secret
ENVIRONMENT=development
```
