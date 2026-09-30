import { useEffect, useState } from "react";
import { ArrowLeft, ArrowRight, Minus, Plus, Trash2 } from "lucide-react";
import { Link } from "react-router";
import { api, type Cart } from "../../shared/api/api";
import { EmptyState, ErrorState, PageLoader } from "../../shared/ui/Feedback";
import { money } from "../../shared/format/money";
import { useShop } from "../../app/ShopContext";
import { ProductArt } from "../catalog/ProductArt";

export function CartPage() {
  const [cart, setCart] = useState<Cart | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [busy, setBusy] = useState<number | null>(null);
  const { refreshCart, notify } = useShop();
  useEffect(() => {
    api
      .cart()
      .then(setCart)
      .catch((error) =>
        setError(
          error instanceof Error ? error.message : "Could not load cart.",
        ),
      )
      .finally(() => setLoading(false));
  }, []);
  async function change(productId: number, quantity?: number) {
    setBusy(productId);
    setError("");
    try {
      const next =
        quantity === undefined
          ? await api.removeFromCart(productId)
          : await api.setCartQuantity(productId, quantity);
      setCart(next);
      refreshCart();
    } catch (error) {
      const message =
        error instanceof Error ? error.message : "Could not update your cart.";
      setError(message);
      notify(message);
    } finally {
      setBusy(null);
    }
  }
  return (
    <div className="container cart-page">
      <div className="page-intro">
        <span className="eyebrow purple">YOUR LITTLE HAUL</span>
        <h1>
          Your cart<span className="heading-flower">✳</span>
        </h1>
        <p>All your good finds, together in one place.</p>
      </div>
      {loading ? (
        <PageLoader />
      ) : error && !cart ? (
        <ErrorState message={error} />
      ) : !cart?.cartItems.length ? (
        <EmptyState
          title="Your cart is feeling a little lonely"
          text="Let's fill it with something lovely."
          link="/shop"
          linkText="Explore the shop"
        />
      ) : (
        <div className="cart-layout">
          <div className="cart-items">
            {error && (
              <div className="form-error" role="alert">
                {error}
              </div>
            )}
            {cart.cartItems.map((item) => (
              <div className="cart-item" key={item.id}>
                <Link to={`/products/${item.product.id}`}>
                  <ProductArt product={item.product} />
                </Link>
                <div className="cart-item-info">
                  <span className="eyebrow">{item.product.category.name}</span>
                  <Link
                    to={`/products/${item.product.id}`}
                    className="cart-item-name"
                  >
                    {item.product.name}
                  </Link>
                  <p>{money(item.price - item.discount)} each</p>
                  <div className="cart-item-controls">
                    <div className="quantity-stepper">
                      <button
                        aria-label={`Decrease ${item.product.name} quantity`}
                        disabled={
                          busy === item.product.id || item.quantity <= 1
                        }
                        onClick={() =>
                          void change(item.product.id, item.quantity - 1)
                        }
                      >
                        <Minus size={14} />
                      </button>
                      <span>{item.quantity}</span>
                      <button
                        aria-label={`Increase ${item.product.name} quantity`}
                        disabled={
                          busy === item.product.id ||
                          item.quantity >= item.product.quantity
                        }
                        onClick={() =>
                          void change(item.product.id, item.quantity + 1)
                        }
                      >
                        <Plus size={14} />
                      </button>
                    </div>
                    <button
                      className="remove-button"
                      disabled={busy === item.product.id}
                      onClick={() => void change(item.product.id)}
                    >
                      <Trash2 size={15} /> Remove
                    </button>
                  </div>
                </div>
                <strong className="cart-line-total">
                  {money((item.price - item.discount) * item.quantity)}
                </strong>
              </div>
            ))}
          </div>
          <aside className="order-summary">
            <h3>Order summary</h3>
            <div className="summary-row">
              <span>
                Items (
                {cart.cartItems.reduce((sum, item) => sum + item.quantity, 0)})
              </span>
              <strong>{money(cart.totalPrice)}</strong>
            </div>
            <div className="summary-row">
              <span>Shipping</span>
              <span>Not included</span>
            </div>
            <div className="summary-total">
              <span>Subtotal</span>
              <strong>{money(cart.totalPrice)}</strong>
            </div>
            <p>Payment is arranged after your order is placed.</p>
            <Link className="button button-dark full-width" to="/checkout">
              Continue to checkout <ArrowRight size={17} />
            </Link>
            <Link className="back-link centered" to="/shop">
              <ArrowLeft size={16} /> Keep browsing
            </Link>
          </aside>
        </div>
      )}
    </div>
  );
}
