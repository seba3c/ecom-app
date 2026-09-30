.PHONY: setup run db-up db-down db-reset run-uvicorn run-fastapi test migrate migrate-rollback code-format dep-sync pre-commit-install pre-commit-run docker-run docker-test seed

# Install and synchronize project dependencies
setup: dep-sync

# Run the default development server
run: run-uvicorn

# Start the dev DB container and wait until healthy
db-up:
	docker compose up -d db --wait

# Stop dev DB container but keep volumes
db-down:
	docker compose down db

# Stop dev DB container and delete volumes (fresh start)
db-reset:
	docker compose down db -v

# Run the FastAPI app with auto-reload (uvicorn --reload)
run-uvicorn: migrate
	uv run uvicorn app.main:app --reload

# Run via FastAPI CLI dev server (supports auto-reload and type checking)
run-fastapi: migrate
	uv run fastapi dev

# Run all tests with verbose output against the test DB (overrides db with test config)
test: db-down
	docker compose -f docker-compose.yml -f docker-compose.test.yml up -d db --wait
	TEST_DATABASE_URL=mysql+aiomysql://fa_ecom_user:fa_ecom_pass@localhost:3307/fa_ecom_test uv run pytest -v

# Run Alembic migrations against the dev DB
migrate: db-up
	uv run alembic upgrade head

# Rollback the last migration
migrate-rollback: db-up
	uv run alembic downgrade -1

# Auto-fix lint issues and format the code
code-format:
	uv run ruff check --fix .
	uv run ruff format .

# Sync dependencies from pyproject.toml
dep-sync:
	uv sync

# Install pre-commit git hooks (runs automatically on commit after this)
pre-commit-install:
	uv run pre-commit install

# Run all pre-commit hooks on all files (lint, format, yaml, whitespace, etc.)
pre-commit-run:
	uv run pre-commit run --all-files

# Seed the shared development catalog into the dev DB
seed: migrate
	PYTHONPATH=. uv run python app/db/seeds/catalog.py

# Build and start the API container via Docker Compose
docker-run:
	docker compose up api --build

# Run tests inside a Docker container (uses test DB override)
docker-test:
	docker compose -f docker-compose.yml -f docker-compose.test.yml run test
