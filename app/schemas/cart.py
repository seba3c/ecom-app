from app.schemas.base import APIModel
from app.schemas.common import Money
from app.schemas.product import ProductDetail


class CartItemDetail(APIModel):
    id: int
    quantity: int
    price: Money
    discount: Money
    product: ProductDetail


class CartDetail(APIModel):
    id: int
    total_price: Money
    cart_items: list[CartItemDetail]
