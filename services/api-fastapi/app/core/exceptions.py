from asgi_correlation_id import correlation_id
from fastapi import FastAPI, Request
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse


class AppException(Exception):
    status_code: int = 500

    def __init__(self, detail: str):
        self.detail = detail
        super().__init__(detail)


class EntityNotFoundError(AppException):
    status_code = 404


class CategoryNotFoundError(EntityNotFoundError):
    def __init__(self):
        super().__init__("Category not found")


class EntityDuplicatedError(AppException):
    status_code = 409


class CategoryDuplicatedError(EntityDuplicatedError):
    def __init__(self):
        super().__init__("Category with this name already exists.")


class APIError(Exception):
    def __init__(self, status_code: int, message: str):
        self.status_code = status_code
        self.message = message
        super().__init__(message)


class AuthenticationRequired(Exception):
    pass


class FieldValidationError(Exception):
    def __init__(self, errors: dict[str, str]):
        self.errors = errors


def not_found(resource: str, id: int) -> APIError:
    return APIError(404, f"{resource} with id: {id} not found")


async def api_error_handler(request: Request, exc: APIError) -> JSONResponse:
    return JSONResponse(status_code=exc.status_code, content={"message": exc.message})


async def field_validation_handler(
    request: Request, exc: FieldValidationError
) -> JSONResponse:
    return JSONResponse(status_code=400, content=exc.errors)


async def unauthorized_handler(
    request: Request, exc: AuthenticationRequired
) -> JSONResponse:
    return JSONResponse(
        status_code=401,
        content={
            "status": 401,
            "error": "Unauthorized",
            "message": "Full authentication is required to access this resource",
            "path": request.url.path,
        },
    )


async def validation_error_handler(
    request: Request, exc: RequestValidationError
) -> JSONResponse:
    errors = {}
    for error in exc.errors():
        field = str(error["loc"][-1])
        error_type = error["type"]
        if error_type == "missing" or (
            error_type in {"string_type", "int_type", "decimal_type"}
            and error.get("input") is None
        ):
            message = (
                "must not be null"
                if field in {"addressId", "quantity", "price", "discount"}
                else "must not be blank"
            )
        elif error_type in {"greater_than_equal", "decimal_ge"}:
            minimum = "0.0" if field in {"price", "discount"} else "0"
            message = f"must be greater than or equal to {minimum}"
        elif (
            error_type == "string_too_short"
            and field == "name"
            and "categories" in request.url.path
            and "products" not in request.url.path
        ):
            message = "Category name must have at least 5 characters"
        elif error_type == "string_too_short" and field == "name":
            message = "Product name must have at least 5 characters"
        elif error_type == "string_too_short" and field in {
            "streetLine1",
            "city",
            "state",
            "country",
        }:
            label, length = {
                "streetLine1": ("Street", 1),
                "city": ("City", 3),
                "state": ("State", 3),
                "country": ("Country", 2),
            }[field]
            message = f"{label} name must have at least {length} characters"
        elif error_type == "string_too_short" and field == "description":
            message = "Product description must not be blank"
        elif error_type == "string_too_short" and field in {"zipCode", "pgName"}:
            message = "must not be blank"
        elif field == "email" and error_type == "value_error":
            message = "must be a well-formed email address"
        elif error_type in {"string_too_short", "string_too_long"} and field in {
            "username",
            "password",
            "paymentMethod",
        }:
            bounds = {
                "username": (3, 20),
                "password": (8, 40),
                "paymentMethod": (4, 2147483647),
            }[field]
            message = f"size must be between {bounds[0]} and {bounds[1]}"
        elif error_type == "value_error" and "error" in error.get("ctx", {}):
            message = str(error["ctx"]["error"])
        else:
            message = error["msg"]
        errors[field] = message
    return JSONResponse(status_code=400, content=errors)


async def app_exception_handler(request: Request, exc: AppException):
    return JSONResponse(status_code=exc.status_code, content={"detail": exc.detail})


async def unhandled_exception_handler(request: Request, exc: Exception) -> JSONResponse:
    return JSONResponse(
        status_code=500,
        content={"detail": "Internal server error"},
        headers={"X-Request-ID": correlation_id.get() or ""},
    )


def register_exception_handlers(app: FastAPI) -> None:
    # Registering custom exception handlers from most specific to least specific
    app.add_exception_handler(AppException, app_exception_handler)
    app.add_exception_handler(APIError, api_error_handler)
    app.add_exception_handler(FieldValidationError, field_validation_handler)
    app.add_exception_handler(AuthenticationRequired, unauthorized_handler)
    app.add_exception_handler(RequestValidationError, validation_error_handler)
    app.add_exception_handler(Exception, unhandled_exception_handler)
