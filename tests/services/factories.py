from datetime import datetime, timezone
from decimal import Decimal

from app.models.address import Address
from app.models.cart import Cart, CartItem
from app.models.category import Category
from app.models.product import Product
from app.models.user import User
from app.schemas.address import AddressInput
from app.schemas.product import ProductInput


def category():
    return Category(id=1, name="Electronics")


def product(name="Laptop Pro", quantity=5):
    return Product(
        id=2,
        name=name,
        description="Portable computer",
        quantity=quantity,
        price=Decimal("100.00"),
        discount=Decimal("10.00"),
        category=category(),
    )


def product_input(name="Laptop Pro"):
    return ProductInput(
        name=name,
        description="Portable computer",
        quantity=5,
        price=Decimal("100.00"),
        discount=Decimal("10.00"),
    )


def address(user_id=1):
    return Address(
        id=3,
        user_id=user_id,
        street_line1="Main Street 1",
        city="Madrid",
        state="Madrid",
        country="Spain",
        zip_code="28001",
    )


def address_input(city="Madrid"):
    return AddressInput(
        street_line1="Main Street 1",
        city=city,
        state="Madrid",
        country="Spain",
        zip_code="28001",
    )


def user():
    return User(id=1, username="buyer", email="buyer@example.com", password_hash="hash")


def cart(items=None):
    items = [] if items is None else items
    return Cart(id=4, user_id=1, total_price=Decimal("0"), items=items)


def cart_item(item_product=None, quantity=2):
    item_product = item_product or product()
    return CartItem(
        id=5,
        product=item_product,
        product_id=item_product.id,
        quantity=quantity,
        price=item_product.price,
        discount=item_product.discount,
    )


def order_date():
    return datetime(2026, 1, 1, tzinfo=timezone.utc)
