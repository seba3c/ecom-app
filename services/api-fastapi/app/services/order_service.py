from decimal import Decimal

from app.core.exceptions import APIError, not_found
from app.models.order import Order, OrderItem, Payment
from app.models.user import User
from app.repositories.addresses import AddressRepository
from app.repositories.carts import CartRepository
from app.repositories.orders import OrderRepository
from app.repositories.products import ProductRepository
from app.schemas.order import OrderDetail, OrderInput, OrderItemDetail, PaymentDetail
from app.schemas.product import ProductDetail


class OrderService:
    def __init__(self, repository: OrderRepository):
        self.repository = repository
        session = repository.session
        self.carts = CartRepository(session)
        self.products = ProductRepository(session)
        self.addresses = AddressRepository(session)

    async def place_order(self, user: User, data: OrderInput) -> OrderDetail:
        cart = await self.carts.get(user.id, lock=True)
        if cart is None:
            raise APIError(400, "Cart not found for user")
        if not cart.items:
            raise APIError(400, "Cart is empty")
        address = await self.addresses.get(data.address_id, user.id)
        if address is None:
            raise not_found("Address", data.address_id)
        payment = Payment(
            method=data.payment_method,
            pg_name=data.pg_name,
            pg_payment_id=data.pg_payment_id,
            pg_status=data.pg_status,
            pg_response=data.pg_response,
        )
        order = Order(
            user_id=user.id,
            address_id=address.id,
            payment=payment,
            status="PENDING",
            total_amount=Decimal("0"),
            items=[],
        )
        for cart_item in cart.items:
            product = await self.products.get(cart_item.product_id, lock=True)
            if product.quantity < cart_item.quantity:
                raise APIError(400, f"Insufficient stock for product: {product.name}")
            product.quantity -= cart_item.quantity
            order.items.append(
                OrderItem(
                    product=product,
                    quantity=cart_item.quantity,
                    price=cart_item.price,
                    discount=cart_item.discount,
                )
            )
        order.total_amount = sum(
            ((item.price - item.discount) * item.quantity for item in order.items),
            Decimal("0"),
        )
        cart.items.clear()
        cart.total_price = Decimal("0")
        await self.repository.create(order)
        return OrderDetail(
            id=order.id,
            email=user.email,
            items=[
                OrderItemDetail(
                    id=item.id,
                    quantity=item.quantity,
                    price=item.price,
                    discount=item.discount,
                    product=ProductDetail.model_validate(item.product),
                )
                for item in order.items
            ],
            order_date=order.order_date,
            total_amount=order.total_amount,
            status=order.status,
            shipping_address_id=order.address_id,
            payment=PaymentDetail.model_validate(order.payment),
        )
