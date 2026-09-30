# AGENTS.md

## Build Commands

```bash
./gradlew build
./gradlew bootRun      # starts on port 8080
./gradlew test
./gradlew test --tests "com.ecommerce.project.SbEcomApplicationTests"
```

## Architecture

Spring Boot 4.0 REST API (Java 21) with **layer-based packaging**:

```
src/main/java/com/ecommerce/project/
├── model/          # JPA entities (Product, Category, Cart, CartItem, Address, Order, OrderItem, OrderStatus, Payment, User, Role)
│   └── Auditable   # @MappedSuperclass with createdAt/updatedAt/createdBy/updatedBy
├── dto/            # Request/Response DTOs (<Entity><Action>Request/Response convention)
├── repository/     # Spring Data JPA repositories
├── service/        # Interface + Impl pattern
├── controller/     # @RestController classes
├── config/         # Configuration classes (AppConfig, AuditConfig, H2ConsoleConfig)
├── exception/      # GlobalExceptionHandler, ResourceNotFoundException, APIException
├── util/           # AuthUtils (current-user helpers)
└── security/       # Spring Security + JWT sub-package
    ├── config/     # WebSecurityConfig, SeedUserProperties
    ├── controller/ # AuthController
    ├── dto/        # LoginRequest, SignupRequest, UserInfoResponse, MessageResponse
    ├── jwt/        # JwtUtils, JwtGenerator, JwtParser, JwtValidator, JwtAuthTokenFilter, JwtAuthEntryPoint
    ├── repository/ # UserRepository, RoleRepository
    └── service/    # UserDetailsServiceImpl, UserDetailsImpl
```

## Key Facts

- Uses **Spring Data JPA**; default active profile is **mysql** (Docker Compose spins up MySQL)
- Switch to H2 with `-Dspring.profiles.active=h2`; H2 console at `/h2-console` (`jdbc:h2:mem:testdb`)
- PostgreSQL profile also available (`-Dspring.profiles.active=postgres`)
- **Spring Security is active** — stateless JWT cookie-based auth (`ecommerce-app` cookie, 24 h)
- Roles: `ROLE_USER`, `ROLE_SELLER`, `ROLE_ADMIN`
- Seed users created on startup (configured via `spring.app.seed-users` in `application.properties`):
  - `user / userpass` → ROLE_USER
  - `seller / sellerpass` → ROLE_SELLER
  - `admin / adminpass` → ROLE_USER, ROLE_SELLER, ROLE_ADMIN
- All entities extend `Auditable` — JPA auditing writes `created_at`, `updated_at`, `created_by`, `updated_by` automatically

## API Endpoints

| Method | Path | Auth |
|--------|------|------|
| POST | `/api/auth/signin` | public |
| POST | `/api/auth/signup` | public |
| GET | `/api/auth/username` | public |
| GET | `/api/auth/user` | public |
| POST | `/api/auth/signout` | public |
| GET | `/api/public/categories` | public |
| POST | `/api/admin/categories` | ROLE_ADMIN |
| PUT | `/api/admin/categories/{id}` | ROLE_ADMIN |
| DELETE | `/api/admin/categories/{id}` | ROLE_ADMIN |
| GET | `/api/public/products` | public |
| GET | `/api/public/products/{productId}` | public |
| GET | `/api/public/products/keyword/{keyword}` | public |
| GET | `/api/public/categories/{categoryId}/products` | public |
| POST | `/api/admin/categories/{categoryId}/products` | ROLE_ADMIN |
| PUT | `/api/admin/products/{productId}` | ROLE_ADMIN |
| DELETE | `/api/admin/products/{productId}` | ROLE_ADMIN |
| GET | `/api/addresses` | authenticated |
| GET | `/api/addresses/{id}` | authenticated |
| POST | `/api/addresses` | authenticated |
| PUT | `/api/addresses/{id}` | authenticated |
| DELETE | `/api/addresses/{id}` | authenticated |
| GET | `/api/my_cart` | authenticated |
| POST | `/api/my_cart/{productId}/quantity/{quantity}` | authenticated |
| PUT | `/api/my_cart/{productId}/quantity/{quantity}` | authenticated |
| DELETE | `/api/my_cart/{productId}` | authenticated |
| GET | `/api/admin/carts` | ROLE_ADMIN |
| POST | `/api/orders` | authenticated |

## Git Branch Naming

`<ai_tool_name>/<type>/<branch-name>` (e.g., `gemini/feature/add-product-entity`)

Where `<ai_tool_name>` is the name of the AI tool executing, i.e., claude, gemini, codex, opencode, etc.

## Branching Rules

- **Never commit directly to `main`.** All new features, bug fixes, and changes must be implemented on a dedicated branch.
- Create a branch before making any code changes.
- Open a pull request to merge changes back into `main`.

## Commit Message Format

Commit messages must include the AI tool name and the model used in the footer:

```
<type>: <short description>

<optional body>

AI-Tool: <tool-name> (<model-id>)
```

If the changes were authored by one model but executed (e.g., applied, run, or reviewed) by a different model, list both:

```
AI-Tool: <tool-name> (<authoring-model-id>) / <tool-name> (<executing-model-id>)
```

Examples:

```
feat: add product search endpoint

AI-Tool: claude (claude-sonnet-4-6)
```

```
fix: correct category validation logic

AI-Tool: gemini (gemini-2.5-pro) / claude (claude-sonnet-4-6)
```

## Testing

- JUnit 5 with Spring Boot Test
- Integration tests use `@SpringBootTest`
- MockMvc for controller tests

## No Lint/Typecheck

No Checkstyle, Spotless, or other lint tools configured. Build success = code is valid.

## Custom Agents

OpenCode uses `.opencode/agents/` for custom agents. To invoke:

```
@spring-test-planner analyze CategoryServiceImpl
```

This will create a test plan and hand off to `@spring-test-writer` to implement.
