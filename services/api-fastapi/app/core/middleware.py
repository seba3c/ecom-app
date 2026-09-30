import logging
import time

from asgi_correlation_id import CorrelationIdMiddleware
from fastapi import FastAPI, Request

logger = logging.getLogger(__name__)


async def add_process_time_header(request: Request, call_next):
    start_time = time.perf_counter()
    response = await call_next(request)
    process_time = time.perf_counter() - start_time
    response.headers["X-Process-Time"] = "%.4f" % process_time
    return response


async def log_requests(request: Request, call_next):
    client_host = request.client.host if request.client else "unknown"
    logger.info("[%s] %s from %s", request.method, request.url.path, client_host)
    return await call_next(request)


def register_middleware(app: FastAPI) -> None:
    app.middleware("http")(add_process_time_header)
    app.middleware("http")(log_requests)
    app.add_middleware(CorrelationIdMiddleware)
