from decimal import Decimal

from app.models.address import Address
from app.models.cart import Cart, CartItem
from app.models.category import Category
from app.models.product import Product
from app.models.user import User


async def make_user(session, username="buyer"):
    user = User(
        username=username,
        email=f"{username}@example.com",
        password_hash="hashed",
        roles=[],
    )
    session.add(user)
    await session.commit()
    return user


async def make_category(session, name="Electronics"):
    category = Category(name=name)
    session.add(category)
    await session.commit()
    return category


async def make_product(session, category=None, name="Laptop Pro", **changes):
    if category is None:
        category = await make_category(session)
    values = {
        "name": name,
        "description": "Portable computer",
        "quantity": 5,
        "price": Decimal("100.00"),
        "discount": Decimal("10.00"),
        "category": category,
    }
    values.update(changes)
    product = Product(**values)
    session.add(product)
    await session.commit()
    return product


async def make_address(session, user, city="Madrid"):
    address = Address(
        user_id=user.id,
        street_line1="Main Street 1",
        city=city,
        state="Madrid",
        country="Spain",
        zip_code="28001",
    )
    session.add(address)
    await session.commit()
    return address


async def make_cart(session, user, product=None, quantity=1):
    items = []
    total_price = Decimal("0")
    if product is not None:
        items = [
            CartItem(
                product=product,
                quantity=quantity,
                price=product.price,
                discount=product.discount,
            )
        ]
        total_price = (product.price - product.discount) * quantity
    cart = Cart(user_id=user.id, total_price=total_price, items=items)
    session.add(cart)
    await session.commit()
    return cart
