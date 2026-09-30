FROM ghcr.io/astral-sh/uv:python3.12-bookworm AS base

WORKDIR /app

COPY pyproject.toml uv.lock* ./
RUN uv sync --no-install-project --no-editable

COPY app/ app/
COPY tests/ tests/
COPY alembic/ alembic/
COPY alembic.ini ./

FROM base AS api

EXPOSE 8000

CMD ["sh", "-c", "uv run alembic upgrade head && uv run uvicorn app.main:app --host 0.0.0.0 --port 8000"]

FROM base AS test

CMD ["uv", "run", "pytest", "-v"]
