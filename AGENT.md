# Agent guidance

## Project purpose

This repository is the shared client workspace for two ecommerce backend implementations. It will contain:

- A React application that can run against either backend.
- Utilities for developing, testing, and comparing the implementations.
- The Bruno API collection in `utils/bruno-api`.

The React application has not been scaffolded yet. Do not assume a package manager, build tool, framework extension, or command until the corresponding configuration exists in this repository.

## Related repositories

The backend repositories are sibling directories:

| Project | Path | Stack | Local API base URL |
| --- | --- | --- | --- |
| Spring Boot API | `../sb-ecom` | Java 21, Spring Boot 4, Gradle | `http://localhost:8080/api` |
| FastAPI API | `../fa-ecom` | Python 3.12, FastAPI, uv | `http://localhost:8000/api` |

Read each backend's `README.md` and `AGENTS.md` before making integration assumptions. Treat the Java API's paths, request and response payloads, pagination, authentication, and authorization behavior as the shared client contract. The FastAPI implementation aims to remain compatible with it.

Do not modify either sibling backend unless the task explicitly includes that repository.

## API integration

- Keep the backend base URL configurable so the same frontend build can target either implementation.
- API routes are rooted at `/api`.
- Local authentication uses the `ecommerce-app` HTTP-only JWT cookie. Protected endpoints may also accept a bearer token.
- Local seed accounts are `user/userpass`, `seller/sellerpass`, and `admin/adminpass`.
- Preserve the shared API contract when adding client types, fixtures, mocks, or request helpers.
- Check both backend implementations when behavior differs or the contract is unclear.

## Bruno collection

- The collection root is `utils/bruno-api`.
- All request URLs must use `{{api_url}}`; do not hardcode a hostname or port in request files.
- The local environment is `utils/bruno-api/environments/Ecom API - localhost.yml`.
- Set `api_url` to `http://localhost:8080/api` for Spring Boot or `http://localhost:8000/api` for FastAPI.
- Keep endpoint paths and example payloads compatible with both backends unless a request is explicitly backend-specific.

## Common backend commands

Run these commands from the relevant sibling repository.

### Spring Boot (`../sb-ecom`)

```bash
./gradlew bootRun
./gradlew test
./gradlew spotlessCheck
```

### FastAPI (`../fa-ecom`)

```bash
uv sync
make migrate
make run-uvicorn
uv run pytest -v
make code-check
```

## Working conventions

- Keep reusable development tools under `utils/` and document their setup in the root README.
- Update `README.md` when the React toolchain, install command, environment variables, or run commands are introduced or changed.
- Avoid committing secrets, JWT signing keys, production credentials, generated build output, or dependency directories.
- Keep backend-specific behavior isolated behind configuration or a clearly named adapter when it cannot be represented by the shared contract.
- Verify client changes against both backend base URLs when the relevant backend services are available.
