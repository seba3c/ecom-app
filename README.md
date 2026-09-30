# ecom-app

Polyglot monorepo for two compatible ecommerce backend implementations, a future React frontend, and shared development utilities.

## Repository layout

```text
ecom-app/
|-- apps/
|   `-- web/                 # React frontend placeholder
|-- services/
|   |-- api-spring/          # Java 21, Spring Boot 4, Gradle
|   `-- api-fastapi/         # Python 3.12, FastAPI, uv
|-- contracts/
|   `-- openapi/             # Canonical shared OpenAPI contract
`-- tools/
    `-- bruno-api/           # Bruno API collection
```

The backend projects retain their native build tools and can be developed independently. Their original `main` histories were imported without squashing.

## Root development commands

Run one backend implementation at a time:

```bash
make dev-spring    # http://localhost:8080/api
make dev-fastapi   # http://localhost:8000/api
make help          # list root commands
```

Both commands run in the foreground and can be stopped with Ctrl-C. Each backend manages its own database setup as described in its service README.

The React application has not been scaffolded. For now, `make web` returns an explanatory error; the target will delegate to the frontend development server when the toolchain is introduced.

## Backend commands

Commands can also be run directly from the service directories.

### Spring Boot

```bash
cd services/api-spring
make setup
make run
make test
make code-format
```

See [the Spring API README](services/api-spring/README.md) for configuration details.

### FastAPI

```bash
cd services/api-fastapi
make setup
make migrate
make run
uv run pytest -v
make code-format
```

See [the FastAPI README](services/api-fastapi/README.md) for configuration details.

## Shared API behavior

Both implementations expose routes below `/api`. The canonical shared paths,
payloads, pagination, authentication, and authorization behavior are defined in
[`contracts/openapi/openapi.yaml`](contracts/openapi/openapi.yaml).

Local authentication uses the `ecommerce-app` HTTP-only JWT cookie. Development seed accounts are `user/userpass`, `seller/sellerpass`, and `admin/adminpass`.

## Bruno collection

Open `tools/bruno-api` in [Bruno](https://www.usebruno.com/). Requests use the `api_url` environment variable rather than a hard-coded host:

| Backend | `api_url` |
| --- | --- |
| Spring Boot | `http://localhost:8080/api` |
| FastAPI | `http://localhost:8000/api` |
