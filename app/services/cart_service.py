from decimal import Decimal

from app.core.exceptions import APIError, not_found
from app.models.cart import Cart, CartItem
from app.repositories.carts import CartRepository
from app.repositories.products import ProductRepository
from app.schemas.cart import CartDetail, CartItemDetail
from app.schemas.product import ProductDetail


def cart_detail(cart: Cart) -> CartDetail:
    return CartDetail(
        id=cart.id,
        total_price=cart.total_price,
        cart_items=[
            CartItemDetail(
                id=item.id,
                quantity=item.quantity,
                price=item.price,
                discount=item.discount,
                product=ProductDetail.model_validate(item.product),
            )
            for item in cart.items
        ],
    )


class CartService:
    def __init__(self, repository: CartRepository):
        self.repository = repository
        self.session = repository.session
        self.products = ProductRepository(self.session)

    async def cart(self, user_id: int, create: bool = False) -> Cart:
        cart = await self.repository.get(user_id)
        if cart is None and create:
            cart = Cart(user_id=user_id, total_price=Decimal("0"), items=[])
            self.session.add(cart)
            await self.session.commit()
            await self.session.refresh(cart)
        if cart is None:
            raise APIError(400, "Cart not found")
        return cart

    async def change_cart(
        self, user_id: int, product_id: int, quantity: int | None, action: str
    ) -> CartDetail:
        cart = await self.cart(user_id, create=action == "add")
        product = await self.products.get(product_id)
        if product is None:
            raise not_found("Product", product_id)
        item = next(
            (item for item in cart.items if item.product_id == product_id), None
        )
        if action == "add" and item is not None:
            raise APIError(400, "Product already in cart. Use PUT to update quantity.")
        if action != "add" and item is None:
            raise APIError(400, "Product not found in cart")
        if quantity is not None and product.quantity < quantity:
            raise APIError(400, f"Insufficient stock for product: {product.name}")
        if action == "add":
            item = CartItem(
                product=product,
                quantity=quantity,
                price=product.price,
                discount=product.discount,
            )
            cart.items.append(item)
        elif action == "update":
            item.quantity = quantity
        else:
            cart.items.remove(item)
        cart.total_price = sum(
            ((i.price - i.discount) * i.quantity for i in cart.items), Decimal("0")
        )
        await self.session.commit()
        return cart_detail(cart)
