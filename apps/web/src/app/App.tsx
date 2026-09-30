import { useEffect, useState } from "react";
import { ArrowRight, Sparkles, X } from "lucide-react";
import { Link, Route, Routes, useLocation, useNavigate } from "react-router";
import { api } from "../shared/api/api";
import { useSession } from "../features/auth/session";
import { ShopContext, type Shop } from "./ShopContext";
import { Header } from "./Header";
import { Footer } from "./Footer";
import { Protected } from "./Protected";
import { HomePage } from "../features/catalog/HomePage";
import { CatalogPage } from "../features/catalog/CatalogPage";
import { ProductPage } from "../features/catalog/ProductPage";
import { AuthPage } from "../features/auth/AuthPage";
import { CartPage } from "../features/cart/CartPage";
import { CheckoutPage } from "../features/checkout/CheckoutPage";
import { OrderSuccessPage } from "../features/checkout/OrderSuccessPage";
import { AdminPage } from "../features/admin/AdminPage";

export function App() {
  const { user } = useSession();
  const navigate = useNavigate();
  const location = useLocation();
  const [count, setCount] = useState(0);
  const [toast, setToast] = useState("");
  const [cartVersion, setCartVersion] = useState(0);
  useEffect(() => {
    if (!user) {
      setCount(0);
      return;
    }
    let cancelled = false;
    api
      .cart()
      .then((cart) => {
        if (!cancelled)
          setCount(
            cart.cartItems.reduce((sum, item) => sum + item.quantity, 0),
          );
      })
      .catch(() => {});
    return () => {
      cancelled = true;
    };
  }, [user, cartVersion]);
  useEffect(() => {
    if (!toast) return;
    const timer = window.setTimeout(() => setToast(""), 3500);
    return () => window.clearTimeout(timer);
  }, [toast]);
  const shop: Shop = {
    notify: setToast,
    refreshCart: () => setCartVersion((value) => value + 1),
    async addProduct(product, quantity = 1) {
      if (!user) {
        navigate(
          `/signin?next=${encodeURIComponent(location.pathname + location.search)}`,
        );
        return;
      }
      if (product.quantity < 1) return;
      try {
        const existing = (await api.cart()).cartItems.find(
          (item) => item.product.id === product.id,
        );
        const cart = existing
          ? await api.setCartQuantity(product.id, existing.quantity + quantity)
          : await api.addToCart(product.id, quantity);
        setCount(cart.cartItems.reduce((sum, item) => sum + item.quantity, 0));
        setToast(`${product.name} added to your cart`);
      } catch (error) {
        setToast(
          error instanceof Error
            ? error.message
            : "Could not add this product.",
        );
      }
    },
  };
  return (
    <ShopContext.Provider value={shop}>
      <div className="app-shell">
        <Header count={count} />
        <main id="main-content">
          <Routes>
            <Route path="/" element={<HomePage />} />
            <Route path="/shop" element={<CatalogPage />} />
            <Route path="/products/:id" element={<ProductPage />} />
            <Route path="/signin" element={<AuthPage mode="signin" />} />
            <Route path="/signup" element={<AuthPage mode="signup" />} />
            <Route
              path="/cart"
              element={
                <Protected>
                  <CartPage />
                </Protected>
              }
            />
            <Route
              path="/checkout"
              element={
                <Protected>
                  <CheckoutPage />
                </Protected>
              }
            />
            <Route
              path="/order-success"
              element={
                <Protected>
                  <OrderSuccessPage />
                </Protected>
              }
            />
            <Route
              path="/admin"
              element={
                <Protected admin>
                  <AdminPage />
                </Protected>
              }
            />
            <Route
              path="*"
              element={
                <div className="container not-found">
                  <span>404</span>
                  <h1>We couldn't find that page.</h1>
                  <Link className="button button-dark" to="/">
                    Back home <ArrowRight size={16} />
                  </Link>
                </div>
              }
            />
          </Routes>
        </main>
        <Footer />
        {toast && (
          <div className="toast" role="status">
            <Sparkles size={18} />
            {toast}
            <button onClick={() => setToast("")} aria-label="Dismiss">
              <X size={16} />
            </button>
          </div>
        )}
      </div>
    </ShopContext.Provider>
  );
}
