import { useEffect, useState } from "react";
import {
  Menu,
  Search,
  ShoppingBag,
  Sparkles,
  UserRound,
  X,
} from "lucide-react";
import { Link, useLocation, useNavigate } from "react-router";
import { useShop } from "./ShopContext";
import { useSession } from "../features/auth/session";

export function Header({ count }: { count: number }) {
  const { user, signOut } = useSession();
  const { notify } = useShop();
  const [menu, setMenu] = useState(false);
  const [search, setSearch] = useState("");
  const navigate = useNavigate();
  const location = useLocation();
  useEffect(() => setMenu(false), [location.pathname]);
  return (
    <>
      <div className="announcement">
        <Sparkles size={14} /> A little something to make your day{" "}
        <span>✦</span> Find your next favorite
      </div>
      <header className="site-header">
        <div className="header-inner container">
          <Link to="/" className="brand" aria-label="goodthings home">
            <span className="brand-mark">✳</span>goodthings
            <span className="brand-dot">.</span>
          </Link>
          <nav
            className={`main-nav ${menu ? "open" : ""}`}
            aria-label="Main navigation"
          >
            <Link to="/shop">Shop all</Link>
            <Link to="/shop?sortBy=id&sortOrder=desc">New arrivals</Link>
            {user?.roles.includes("ROLE_ADMIN") && (
              <Link to="/admin">Admin studio</Link>
            )}
            {user && (
              <button
                className="mobile-signout"
                onClick={() => {
                  void signOut()
                    .then(() => navigate("/"))
                    .catch((error) =>
                      notify(
                        error instanceof Error
                          ? error.message
                          : "Could not sign out.",
                      ),
                    );
                }}
              >
                Sign out
              </button>
            )}
          </nav>
          <form
            className="header-search"
            onSubmit={(event) => {
              event.preventDefault();
              navigate(`/shop?search=${encodeURIComponent(search.trim())}`);
            }}
          >
            <Search size={18} />
            <input
              aria-label="Search products"
              placeholder="Search good things..."
              value={search}
              onChange={(event) => setSearch(event.target.value)}
            />
          </form>
          <div className="header-actions">
            {user ? (
              <div className="account-menu">
                <span className="welcome">Hi, {user.username}</span>
                <button
                  className="text-button"
                  onClick={() => {
                    void signOut()
                      .then(() => navigate("/"))
                      .catch((error) =>
                        notify(
                          error instanceof Error
                            ? error.message
                            : "Could not sign out.",
                        ),
                      );
                  }}
                >
                  Sign out
                </button>
              </div>
            ) : (
              <Link className="header-icon" to="/signin" aria-label="Sign in">
                <UserRound size={21} />
              </Link>
            )}
            <Link
              className="header-icon cart-icon"
              to={user ? "/cart" : "/signin?next=%2Fcart"}
              aria-label={`Cart, ${count} items`}
            >
              <ShoppingBag size={21} />
              {count > 0 && <span className="cart-count">{count}</span>}
            </Link>
            <button
              className="header-icon menu-toggle"
              aria-label={menu ? "Close menu" : "Open menu"}
              aria-expanded={menu}
              onClick={() => setMenu(!menu)}
            >
              {menu ? <X size={22} /> : <Menu size={22} />}
            </button>
          </div>
        </div>
      </header>
    </>
  );
}
