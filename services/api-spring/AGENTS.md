# Spring Boot Agent Context

## Commands

```bash
make setup        # resolve dependencies and compile application classes
make run          # run with the default MySQL profile
make test         # run the JUnit test suite
make code-format  # apply Spotless formatting and unused-import cleanup
make build        # build and verify the application
```

## Database

- The default `mysql` profile uses Spring Boot Docker Compose support and the `mysql` service in `docker-compose.yml`.
- `make run-h2` uses an in-memory H2 database and disables Docker Compose.
- `make run-postgres` starts the Compose PostgreSQL service.
- Tests select the H2 test configuration and do not require a database container.
- Database connection values can be overridden with `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, and `DB_PASSWORD`.

## Architecture

- `controller/` contains REST controllers rooted at `/api`.
- `dto/` contains request and response DTOs.
- `model/` contains JPA entities; entities inherit audit fields from `Auditable`.
- `service/` uses interface and implementation pairs for business logic.
- `repository/` contains Spring Data JPA repositories.
- `security/` contains authentication, authorization, users, and JWT handling.
- `config/`, `exception/`, and `util/` contain shared application infrastructure.

Preserve the shared behavior defined in `contracts/openapi/openapi.yaml`. Keep backend-specific behavior behind profiles or clearly named adapters.

## Naming

- Name controllers, services, repositories, entities, and DTOs for the singular domain entity.
- Name service interfaces `<Entity>Service` and implementations `<Entity>ServiceImpl`.
- Name DTOs by entity and purpose, such as `ProductCreateRequest` or `ProductDetailResponse`.
- Name tests after the class under test with a `Test` suffix in the matching package.

## Adding an Endpoint

1. Add the route to the appropriate `@RestController` under `controller/` or `security/controller/`.
2. Put business logic in the service layer and persistence in a repository.
3. Use request and response DTOs rather than exposing JPA entities.
4. Add controller tests with MockMvc and focused service tests.
5. If the endpoint is portable, update and verify the shared OpenAPI contract and both backends.

## Testing

- Tests use JUnit 5 and Spring Boot Test.
- Controller tests use MockMvc.
- Use focused Gradle test filters while developing, then run `make test`.
- Authentication tests should cover cookie and bearer-token behavior where relevant.

## Tooling

- Run `make code-format` to apply Palantir Java Format and remove unused imports through Spotless.
- Use the checked-in Gradle wrapper; do not require a system Gradle installation.
- Keep dependency and plugin changes in `build.gradle.kts`.
