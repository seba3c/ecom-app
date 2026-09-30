# ecom-app

Shared client workspace for two ecommerce backend implementations. This repository will contain the React frontend and developer utilities used with both APIs:

- [`seba3c/sb-ecom`](https://github.com/seba3c/sb-ecom) — Java 21 and Spring Boot implementation, served at `http://localhost:8080`.
- [`seba3c/fa-ecom`](https://github.com/seba3c/fa-ecom) — Python 3.12 and FastAPI implementation, served at `http://localhost:8000`.

The FastAPI project follows the Spring Boot API contract, including routes, payloads, pagination, authentication, and role checks. The frontend should therefore be able to use either backend by changing its API base URL.

## Repository status

The React application has not been scaffolded yet. The repository currently contains the Bruno API collection under `utils/bruno-api`.

```text
ecom-app/
|-- README.md
|-- AGENT.md
`-- utils/
    `-- bruno-api/       # Requests for exercising the shared API contract
```

React setup and development commands should be added here after the application is initialized.

## Backend setup

The backend repositories are siblings of this repository. Follow each backend's README for complete setup details.

### Spring Boot

```bash
cd ../sb-ecom
./gradlew bootRun
```

The API is available at `http://localhost:8080/api`.

### FastAPI

```bash
cd ../fa-ecom
uv sync
make migrate
make run-uvicorn
```

The API is available at `http://localhost:8000/api`, and its Swagger UI is available at `http://localhost:8000/docs`.

Run only one backend at a time unless their database and port configuration has been adjusted to avoid conflicts.

## Bruno API collection

Open `utils/bruno-api` as a collection in [Bruno](https://www.usebruno.com/). Every request uses the `api_url` environment variable.

Set `api_url` according to the backend you want to exercise:

| Backend | `api_url` |
| --- | --- |
| Spring Boot | `http://localhost:8080/api` |
| FastAPI | `http://localhost:8000/api` |

The included `Ecom API - localhost` environment targets FastAPI on port 8000 by default. Its `api_url` value is assembled from `api_host` and `api_port`.

## Development accounts

Both backends seed the same local users:

| Username | Password | Roles |
| --- | --- | --- |
| `user` | `userpass` | User |
| `seller` | `sellerpass` | Seller |
| `admin` | `adminpass` | User, seller, and administrator |

Authentication is based on JWTs. Sign-in returns a token and sets the HTTP-only `ecommerce-app` cookie used by protected API routes.

## Related documentation

- Spring Boot: [README](https://github.com/seba3c/sb-ecom/blob/main/README.md) and [agent guidance](https://github.com/seba3c/sb-ecom/blob/main/AGENTS.md)
- FastAPI: [README](https://github.com/seba3c/fa-ecom/blob/main/README.md) and [agent guidance](https://github.com/seba3c/fa-ecom/blob/main/AGENTS.md)
