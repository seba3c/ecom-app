# goodthings storefront

React + TypeScript + Vite frontend for the shared ecommerce API in `contracts/openapi/openapi.yaml`. It includes public browsing, account access, a cart, address management, pending-order checkout, and an admin panel for products, categories, and carts. Seller-specific management is deferred because the shared API does not expose seller operations.

## Develop

Requires Node.js 20.19+ or 22.12+. Run a backend first, then:

```bash
npm install
npm run dev
```

Or run `make web` from the repository root. The app runs at `http://localhost:5173`. Vite proxies `/api` to Spring Boot at `http://localhost:8080` by default.

For the sample Spring catalog, run `make seed-spring` once, keep `make dev-spring` running in another terminal, and then run `make web`. Seeding alone does not start the API. Check `http://localhost:8080/api/public/products` for `totalElements: 100` if the storefront appears empty.

To target FastAPI instead:

```bash
API_PROXY_TARGET=http://localhost:8000 npm run dev
```

The browser uses `/api` on the frontend's origin so the `ecommerce-app` HTTP-only cookie works for both implementations. For deployment, reverse-proxy `/api` to the chosen backend on the same public origin. `VITE_API_BASE_URL` defaults to `/api`; set it only if your deployment has a different API path and compatible cookie/CORS configuration. Copy `.env.example` to `.env` for local overrides.

The API provides monetary amounts without a currency code. The UI formats them as USD by default; set `VITE_CURRENCY` to another ISO currency code when appropriate.

## Source layout

The application entry point is `src/main.tsx`. Routes, the shared page layout, and shop context live in `src/app/`. Pages and components owned by one area live under `src/features/` (`catalog`, `auth`, `cart`, `checkout`, and `admin`). Code used across areas lives under `src/shared/` (`api`, `ui`, and `format`). Global styles are in `src/styles/global.css`; tests sit beside the code they cover. Import directly from modules rather than adding barrel files or path aliases.

Seed accounts: `user/userpass`, `seller/sellerpass`, `seller2/seller2pass`, `seller3/seller3pass`, `admin/adminpass`. Seller accounts can browse and shop; the seller panel is not part of this version. Products and categories come from the API. Empty databases show a designed empty state until an admin adds catalog data or runs the development catalog seeder.

Checkout creates an order with manual payment metadata and clearly labels payment as pending. The API does not process a charge or offer order history.

## Checks

```bash
npm run typecheck
npm test
npm run build
```
