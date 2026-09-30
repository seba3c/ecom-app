import { useEffect, useState } from "react";
import { ArrowRight, Package, ShoppingCart, Tag } from "lucide-react";
import { api } from "../../shared/api/api";
import { ErrorState, PageLoader } from "../../shared/ui/Feedback";
import type { Tab } from "./types";

export function Overview({ onTab }: { onTab: (tab: Tab) => void }) {
  const [counts, setCounts] = useState<{
    products: number;
    categories: number;
    carts: number;
  } | null>(null);
  const [error, setError] = useState("");
  useEffect(() => {
    Promise.all([
      api.products({ pageSize: 1 }),
      api.categories(0, 1),
      api.adminCarts(0),
    ])
      .then(([products, categories, carts]) =>
        setCounts({
          products: products.totalElements,
          categories: categories.totalElements,
          carts: carts.totalElements,
        }),
      )
      .catch((error) =>
        setError(
          error instanceof Error ? error.message : "Could not load overview.",
        ),
      );
  }, []);
  return (
    <>
      <div className="admin-heading">
        <span className="eyebrow purple">YOUR COMMAND CENTER</span>
        <h1>
          Good morning, admin <span>✳</span>
        </h1>
        <p>Here's what is happening in your store.</p>
      </div>
      {error ? (
        <ErrorState message={error} />
      ) : !counts ? (
        <PageLoader />
      ) : (
        <>
          <div className="stat-grid">
            <div className="stat-card lavender">
              <Package size={24} />
              <span>Total products</span>
              <strong>{counts.products}</strong>
              <button onClick={() => onTab("products")}>
                Manage products <ArrowRight size={16} />
              </button>
            </div>
            <div className="stat-card peach">
              <Tag size={24} />
              <span>Categories</span>
              <strong>{counts.categories}</strong>
              <button onClick={() => onTab("categories")}>
                Manage categories <ArrowRight size={16} />
              </button>
            </div>
            <div className="stat-card mint">
              <ShoppingCart size={24} />
              <span>Active carts</span>
              <strong>{counts.carts}</strong>
              <button onClick={() => onTab("carts")}>
                View carts <ArrowRight size={16} />
              </button>
            </div>
          </div>
          <div className="admin-callout">
            <div>
              <span className="eyebrow">KEEP THINGS FRESH</span>
              <h2>Something new for the shelves?</h2>
              <p>Add products to give shoppers more good things to discover.</p>
              <button
                className="button button-dark"
                onClick={() => onTab("products")}
              >
                Manage catalog <ArrowRight size={17} />
              </button>
            </div>
            <span>✳</span>
          </div>
        </>
      )}
    </>
  );
}
