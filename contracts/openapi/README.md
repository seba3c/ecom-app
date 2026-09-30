# Shared API contract

[`openapi.yaml`](openapi.yaml) is the canonical OpenAPI 3.1 contract shared by
the Spring Boot API, the FastAPI API, and client applications.

The document covers the portable `/api` surface implemented by both backends.
Backend-specific diagnostic or experimental routes are intentionally excluded.
Protected operations support either the `ecommerce-app` HTTP-only JWT cookie or
a bearer token; the cookie takes precedence when both are present.

## Local servers

| Backend | Base URL |
| --- | --- |
| Spring Boot | `http://localhost:8080/api` |
| FastAPI | `http://localhost:8000/api` |

Import `openapi.yaml` into an OpenAPI 3.1-compatible viewer, validator, or code
generator. Treat changes to paths, request and response schemas, pagination, or
security as cross-backend changes and verify them against both implementations.
