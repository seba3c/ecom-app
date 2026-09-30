# Spring Boot ecommerce API

A Java 21 and Spring Boot implementation of the ecommerce API defined by the monorepo's [shared OpenAPI contract](../../contracts/openapi/openapi.yaml), built with the Gradle wrapper.

## Requirements

- Java 21+
- Docker with Compose for the default MySQL or optional PostgreSQL development profile

## Setup

```bash
make setup
```

The Gradle wrapper downloads the required Gradle distribution and project dependencies. No system Gradle installation is required.

## Quick Commands

| Command | Description |
| --- | --- |
| `make setup` | Resolve dependencies and compile the application classes |
| `make run` | Run the API with the default MySQL profile |
| `make test` | Run the test suite with the test H2 configuration |
| `make code-format` | Apply Spotless formatting and unused-import cleanup |
| `make build` | Build and verify the application |
| `make run-h2` | Run locally with the in-memory H2 profile |

## Run

```bash
make run
```

- API base URL: http://localhost:8080/api
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI document: http://localhost:8080/v3/api-docs

The default `mysql` profile lets Spring Boot Docker Compose support start the MySQL service from `docker-compose.yml`. Use `make run-h2` for an in-memory database or `make run-postgres` for PostgreSQL.

## Test

```bash
make test
```

Tests use JUnit 5 and Spring Boot Test. The test configuration selects H2, so the standard test suite does not require a running database container.

## API and Authentication

Routes are rooted at `/api` and implement the paths, payloads, pagination, authentication, and authorization behavior in the [shared OpenAPI contract](../../contracts/openapi/openapi.yaml).

Sign in with `POST /api/auth/signin` using `{"username":"user","password":"userpass"}`. The response contains `jwtToken` and sets the HTTP-only `ecommerce-app` cookie for `/api`. Protected routes also accept `Authorization: Bearer <jwtToken>`; the cookie takes precedence when both credentials are present. Routes under `/api/admin/**` require `ROLE_ADMIN`.

Development startup creates `user/userpass`, `seller/sellerpass`, and `admin/adminpass`. The JWT lifetime and cookie lifetime both default to 24 hours.

## Commands

### Build and run

```bash
make setup         # Resolve dependencies and compile classes
make run           # Run with MySQL
make run-h2        # Run with in-memory H2
make run-postgres  # Run with PostgreSQL
make test          # Run tests
make build         # Build and verify
```

### Code quality

```bash
make code-format   # Apply Spotless formatting and unused-import cleanup
```

## Project Structure

```text
src/main/java/com/ecommerce/project/
├── controller/  # REST controllers
├── dto/         # Request and response DTOs
├── model/       # JPA entities
├── repository/  # Spring Data repositories
├── service/     # Business interfaces and implementations
├── security/    # Authentication, authorization, users, and JWT handling
├── config/      # Application, auditing, H2, and OpenAPI configuration
├── exception/   # API exceptions and global error handling
└── util/        # Shared helpers
src/test/        # Unit and Spring integration tests
```

## Configuration

The default profile is `mysql`; the `h2`, `postgres`, and `prod` profiles are also available. Development defaults are defined under `src/main/resources/application-dev*.properties`.

Database settings can be overridden with environment variables:

```env
DB_HOST=localhost
DB_PORT=3306
DB_NAME=sb_ecom
DB_USERNAME=sb_ecom
DB_PASSWORD=sb_ecom_pass
```

The production profile additionally requires `JWT_SECRET`, `ADMIN_USERNAME`, and `ADMIN_PASSWORD`; `ADMIN_EMAIL` is optional.
