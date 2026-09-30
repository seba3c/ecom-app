import { useState } from "react";
import { LayoutDashboard, Package, ShoppingCart, Tag } from "lucide-react";
import { Link } from "react-router";
import { useSession } from "../auth/session";
import type { Tab } from "./types";
import { Overview } from "./Overview";
import { ProductsAdmin } from "./ProductsAdmin";
import { CategoriesAdmin } from "./CategoriesAdmin";
import { CartsAdmin } from "./CartsAdmin";

export function AdminPage() {
  const [tab, setTab] = useState<Tab>("overview");
  const { user } = useSession();
  const tabs: { id: Tab; label: string; icon: typeof LayoutDashboard }[] = [
    { id: "overview", label: "Overview", icon: LayoutDashboard },
    { id: "products", label: "Products", icon: Package },
    { id: "categories", label: "Categories", icon: Tag },
    { id: "carts", label: "Carts", icon: ShoppingCart },
  ];
  return (
    <div className="admin-page">
      <aside className="admin-sidebar">
        <div className="admin-sidebar-title">
          <span className="brand-mark">✳</span>
          <div>
            <strong>goodthings.</strong>
            <small>ADMIN STUDIO</small>
          </div>
        </div>
        <nav aria-label="Admin sections">
          {tabs.map((item) => (
            <button
              key={item.id}
              className={tab === item.id ? "active" : ""}
              onClick={() => setTab(item.id)}
            >
              <item.icon size={18} />
              {item.label}
            </button>
          ))}
        </nav>
        <Link to="/" className="admin-back">
          ← Back to storefront
        </Link>
      </aside>
      <div className="admin-main">
        <div className="admin-topbar">
          <span>Store management</span>
          <span className="admin-avatar">
            {user?.username.slice(0, 1).toUpperCase()}
          </span>
        </div>
        <div className="admin-content">
          {tab === "overview" && <Overview onTab={setTab} />}
          {tab === "products" && <ProductsAdmin />}
          {tab === "categories" && <CategoriesAdmin />}
          {tab === "carts" && <CartsAdmin />}
        </div>
      </div>
    </div>
  );
}
