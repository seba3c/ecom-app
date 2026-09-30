# Agent guidance

## Project purpose

This polyglot monorepo contains two compatible ecommerce APIs, a future React frontend, and shared development utilities:

| Project | Path | Stack | Local API base URL |
| --- | --- | --- | --- |
| Spring Boot API | `services/api-spring` | Java 21, Spring Boot 4, Gradle | `http://localhost:8080/api` |
| FastAPI API | `services/api-fastapi` | Python 3.12, FastAPI, uv | `http://localhost:8000/api` |
| React frontend | `apps/web` | Not scaffolded | N/A |

Read a service's own `AGENTS.md` and `README.md` before modifying it. Treat the Spring API's paths, payloads, pagination, authentication, and authorization behavior as the shared client contract until the canonical OpenAPI document is introduced under `contracts/openapi`.

## Root commands

```bash
make help
make dev-spring
make dev-fastapi
make web
```

The backend targets run in the foreground. `make web` intentionally fails with an explanatory message until the React application is scaffolded.

## API integration

- Keep the backend base URL configurable so the future frontend can target either implementation.
- API routes are rooted at `/api`.
- Local authentication uses the `ecommerce-app` HTTP-only JWT cookie. Protected endpoints may also accept a bearer token.
- Local seed accounts are `user/userpass`, `seller/sellerpass`, and `admin/adminpass`.
- Preserve the shared API contract when adding client types, fixtures, mocks, or request helpers.
- Check both backend implementations when behavior differs or the contract is unclear.

## Bruno collection

- The collection root is `tools/bruno-api`.
- All request URLs must use `{{api_url}}`; do not hardcode a hostname or port in request files.
- The local environment is `tools/bruno-api/environments/Ecom API - localhost.yml`.
- Use `http://localhost:8080/api` for Spring Boot or `http://localhost:8000/api` for FastAPI.
- Keep endpoint paths and example payloads compatible with both backends unless a request is explicitly backend-specific.

## Working conventions

- Keep deployable backend services under `services/`, frontend applications under `apps/`, shared API definitions under `contracts/`, and reusable development tools under `tools/`.
- Keep each service's native build and dependency tooling; root commands should delegate rather than reimplement it.
- Update the root README and Makefile when developer commands or paths change.
- Avoid committing secrets, JWT signing keys, production credentials, generated build output, or dependency directories.
- Keep backend-specific behavior isolated behind configuration or a clearly named adapter when it cannot be represented by the shared contract.
- Verify shared client or contract changes against both backend implementations.
