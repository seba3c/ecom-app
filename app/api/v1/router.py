from fastapi import APIRouter, FastAPI

from app.api.v1.endpoints import health, auth, products, addresses, carts, orders

from app.api.v1.endpoints import categories


def register_routers(app: FastAPI) -> None:
    api_router = APIRouter(prefix="/api")

    api_router.include_router(health.router, prefix="/health")

    api_router.include_router(categories.router, prefix="")
    api_router.include_router(auth.router)
    api_router.include_router(products.router)
    api_router.include_router(addresses.router)
    api_router.include_router(carts.router)
    api_router.include_router(orders.router)

    app.include_router(api_router)
