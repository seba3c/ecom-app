import { useEffect, useState, type FormEvent } from "react";
import { ArrowLeft, ArrowRight, CreditCard } from "lucide-react";
import { Link, useNavigate } from "react-router";
import {
  api,
  type Address,
  type AddressInput,
  type Cart,
} from "../../shared/api/api";
import { EmptyState, ErrorState, PageLoader } from "../../shared/ui/Feedback";
import { money } from "../../shared/format/money";
import { useShop } from "../../app/ShopContext";

const blankAddress: AddressInput = {
  streetLine1: "",
  streetLine2: null,
  city: "",
  state: "",
  country: "",
  zipCode: "",
};

export function CheckoutPage() {
  const [cart, setCart] = useState<Cart | null>(null);
  const [addresses, setAddresses] = useState<Address[]>([]);
  const [selected, setSelected] = useState<number | null>(null);
  const [editing, setEditing] = useState<number | null>(null);
  const [form, setForm] = useState<AddressInput>(blankAddress);
  const [showForm, setShowForm] = useState(false);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const navigate = useNavigate();
  const { refreshCart } = useShop();
  useEffect(() => {
    Promise.all([api.cart(), api.addresses()])
      .then(([cartResult, addressResult]) => {
        setCart(cartResult);
        setAddresses(addressResult.content);
        setSelected(addressResult.content[0]?.id ?? null);
      })
      .catch((error) =>
        setError(
          error instanceof Error ? error.message : "Could not load checkout.",
        ),
      )
      .finally(() => setLoading(false));
  }, []);
  function beginEdit(address: Address) {
    setForm({
      streetLine1: address.streetLine1,
      streetLine2: address.streetLine2,
      city: address.city,
      state: address.state,
      country: address.country,
      zipCode: address.zipCode,
    });
    setEditing(address.id);
    setShowForm(true);
  }
  async function saveAddress(event: FormEvent) {
    event.preventDefault();
    setBusy(true);
    setError("");
    try {
      const saved = editing
        ? await api.updateAddress(editing, form)
        : await api.createAddress(form);
      setAddresses((list) =>
        editing
          ? list.map((item) => (item.id === saved.id ? saved : item))
          : [...list, saved],
      );
      setSelected(saved.id);
      setForm(blankAddress);
      setEditing(null);
      setShowForm(false);
    } catch (error) {
      setError(
        error instanceof Error ? error.message : "Could not save address.",
      );
    } finally {
      setBusy(false);
    }
  }
  async function removeAddress(id: number) {
    if (!window.confirm("Remove this address?")) return;
    setBusy(true);
    setError("");
    try {
      await api.deleteAddress(id);
      setAddresses((list) => list.filter((item) => item.id !== id));
      if (selected === id)
        setSelected(addresses.find((item) => item.id !== id)?.id ?? null);
    } catch (error) {
      setError(
        error instanceof Error ? error.message : "Could not remove address.",
      );
    } finally {
      setBusy(false);
    }
  }
  async function placeOrder() {
    if (!selected) return;
    setBusy(true);
    setError("");
    try {
      const order = await api.placeOrder(selected);
      refreshCart();
      navigate("/order-success", { replace: true, state: { order } });
    } catch (error) {
      setError(
        error instanceof Error ? error.message : "Could not place your order.",
      );
    } finally {
      setBusy(false);
    }
  }
  if (loading) return <PageLoader />;
  if (error && !cart)
    return (
      <div className="container">
        <ErrorState message={error} />
      </div>
    );
  if (!cart?.cartItems.length)
    return (
      <div className="container cart-page">
        <EmptyState
          title="Your cart is empty"
          text="Choose a few good things before checking out."
          link="/shop"
        />
      </div>
    );
  return (
    <div className="container checkout-page">
      <Link className="back-link" to="/cart">
        <ArrowLeft size={17} /> Back to cart
      </Link>
      <div className="page-intro">
        <span className="eyebrow purple">ALMOST THERE</span>
        <h1>
          Checkout<span className="heading-flower">✳</span>
        </h1>
        <p>One last step, then the good things are on their way.</p>
      </div>
      <div className="cart-layout">
        <div className="checkout-main">
          <section className="checkout-card">
            <div className="checkout-section-heading">
              <div>
                <span className="step-number">1</span>
                <h2>Delivery address</h2>
              </div>
              <button
                className="text-link"
                onClick={() => {
                  setEditing(null);
                  setForm(blankAddress);
                  setShowForm(true);
                }}
              >
                + Add address
              </button>
            </div>
            <div className="address-list">
              {addresses.map((address) => (
                <div
                  className={`address-option ${selected === address.id ? "selected" : ""}`}
                  key={address.id}
                >
                  <label>
                    <input
                      type="radio"
                      name="address"
                      checked={selected === address.id}
                      onChange={() => setSelected(address.id)}
                    />
                    <span>
                      <strong>
                        {address.streetLine1}
                        {address.streetLine2 ? `, ${address.streetLine2}` : ""}
                      </strong>
                      <small>
                        {address.city}, {address.state} {address.zipCode},{" "}
                        {address.country}
                      </small>
                    </span>
                  </label>
                  <div className="address-actions">
                    <button onClick={() => beginEdit(address)}>Edit</button>
                    <button
                      onClick={() => void removeAddress(address.id)}
                      disabled={busy}
                    >
                      Remove
                    </button>
                  </div>
                </div>
              ))}
              {!addresses.length && !showForm && (
                <p className="muted">Add a delivery address to continue.</p>
              )}
            </div>
            {showForm && (
              <form className="address-form form-stack" onSubmit={saveAddress}>
                <h3>{editing ? "Edit address" : "New address"}</h3>
                <label>
                  Street address
                  <input
                    required
                    value={form.streetLine1}
                    onChange={(event) =>
                      setForm({ ...form, streetLine1: event.target.value })
                    }
                  />
                </label>
                <label>
                  Apartment, suite, etc. <span>(optional)</span>
                  <input
                    value={form.streetLine2 || ""}
                    onChange={(event) =>
                      setForm({
                        ...form,
                        streetLine2: event.target.value || null,
                      })
                    }
                  />
                </label>
                <div className="form-row">
                  <label>
                    City
                    <input
                      required
                      minLength={3}
                      value={form.city}
                      onChange={(event) =>
                        setForm({ ...form, city: event.target.value })
                      }
                    />
                  </label>
                  <label>
                    State / region
                    <input
                      required
                      minLength={3}
                      value={form.state}
                      onChange={(event) =>
                        setForm({ ...form, state: event.target.value })
                      }
                    />
                  </label>
                </div>
                <div className="form-row">
                  <label>
                    Country
                    <input
                      required
                      minLength={2}
                      value={form.country}
                      onChange={(event) =>
                        setForm({ ...form, country: event.target.value })
                      }
                    />
                  </label>
                  <label>
                    ZIP / postal code
                    <input
                      required
                      value={form.zipCode}
                      onChange={(event) =>
                        setForm({ ...form, zipCode: event.target.value })
                      }
                    />
                  </label>
                </div>
                <div className="form-actions">
                  <button
                    type="button"
                    className="button button-outline"
                    onClick={() => setShowForm(false)}
                  >
                    Cancel
                  </button>
                  <button className="button button-dark" disabled={busy}>
                    Save address
                  </button>
                </div>
              </form>
            )}
          </section>
          <section className="checkout-card">
            <div className="checkout-section-heading">
              <div>
                <span className="step-number">2</span>
                <h2>Payment</h2>
              </div>
            </div>
            <div className="payment-note">
              <CreditCard size={23} />
              <div>
                <strong>Payment pending</strong>
                <p>
                  Your order will be placed now. No payment is collected here;
                  payment details will be arranged separately.
                </p>
              </div>
            </div>
          </section>
          {error && (
            <div className="form-error" role="alert">
              {error}
            </div>
          )}
        </div>
        <aside className="order-summary">
          <h3>Your order</h3>
          {cart.cartItems.map((item) => (
            <div className="checkout-line" key={item.id}>
              <span>
                {item.product.name} <small>× {item.quantity}</small>
              </span>
              <strong>
                {money((item.price - item.discount) * item.quantity)}
              </strong>
            </div>
          ))}
          <div className="summary-total">
            <span>Total</span>
            <strong>{money(cart.totalPrice)}</strong>
          </div>
          <button
            className="button button-dark full-width"
            disabled={busy || !selected}
            onClick={() => void placeOrder()}
          >
            {busy ? "Placing order…" : "Place pending order"}{" "}
            <ArrowRight size={17} />
          </button>
          <p className="small-note">
            Placing an order reduces stock and empties your cart.
          </p>
        </aside>
      </div>
    </div>
  );
}
