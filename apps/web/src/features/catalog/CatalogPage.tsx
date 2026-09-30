import { useEffect, useState, type FormEvent } from "react";
import { ArrowRight, Search, SlidersHorizontal } from "lucide-react";
import { useSearchParams } from "react-router";
import {
  api,
  type CatalogParams,
  type Category,
  type Page,
  type Product,
} from "../../shared/api/api";
import { EmptyState, ErrorState, PageLoader } from "../../shared/ui/Feedback";
import { useShop } from "../../app/ShopContext";
import { ProductCard } from "./ProductCard";
import { allCategories } from "./allCategories";

export function CatalogPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const search = searchParams.get("search")?.trim() || "";
  const category = Number(searchParams.get("category") || 0);
  const pageNumber = Math.max(0, Number(searchParams.get("page") || 0) || 0);
  const sortBy = searchParams.get("sortBy") || "id";
  const sortOrder = searchParams.get("sortOrder") === "desc" ? "desc" : "asc";
  const [input, setInput] = useState(search);
  const [categories, setCategories] = useState<Category[]>([]);
  const [page, setPage] = useState<Page<Product> | null>(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [reloadKey, setReloadKey] = useState(0);
  const { addProduct } = useShop();
  useEffect(() => {
    setInput(search);
  }, [search]);
  useEffect(() => {
    allCategories()
      .then(setCategories)
      .catch(() => {});
  }, []);
  useEffect(() => {
    let active = true;
    setLoading(true);
    setError("");
    const options: CatalogParams = {
      pageNumber,
      pageSize: 12,
      sortBy,
      sortOrder,
    };
    const pending = search
      ? api.searchProducts(search, options)
      : category
        ? api.categoryProducts(category, options)
        : api.products(options);
    pending
      .then((result) => {
        if (active) setPage(result);
      })
      .catch((error) => {
        if (active)
          setError(
            error instanceof Error ? error.message : "Could not load products.",
          );
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [search, category, pageNumber, sortBy, sortOrder, reloadKey]);
  function update(values: Record<string, string | null>) {
    const next = new URLSearchParams(searchParams);
    Object.entries(values).forEach(([key, value]) =>
      value ? next.set(key, value) : next.delete(key),
    );
    next.delete("page");
    setSearchParams(next);
  }
  function submit(event: FormEvent) {
    event.preventDefault();
    update({ search: input.trim(), category: null });
  }
  return (
    <div className="container catalog-page">
      <div className="page-intro">
        <span className="eyebrow purple">THE COLLECTION</span>
        <h1>
          All the good things<span className="heading-flower">✳</span>
        </h1>
        <p>
          Wonderful, useful, and full of personality. Find something that feels
          like you.
        </p>
      </div>
      <div className="catalog-toolbar">
        <form className="catalog-search" onSubmit={submit}>
          <Search size={19} />
          <input
            value={input}
            onChange={(event) => setInput(event.target.value)}
            placeholder="What are you looking for?"
            aria-label="Search the catalog"
          />
          <button type="submit">Search</button>
        </form>
        <label className="sort-select">
          <SlidersHorizontal size={18} />
          <select
            aria-label="Sort products"
            value={`${sortBy}:${sortOrder}`}
            onChange={(event) => {
              const [key, direction] = event.target.value.split(":");
              update({ sortBy: key, sortOrder: direction });
            }}
          >
            <option value="id:asc">Featured</option>
            <option value="id:desc">Newest</option>
            <option value="price:asc">Price: low to high</option>
            <option value="price:desc">Price: high to low</option>
            <option value="name:asc">Name: A to Z</option>
          </select>
        </label>
      </div>
      <div className="category-filters">
        <button
          className={!category && !search ? "active" : ""}
          onClick={() => update({ category: null, search: null })}
        >
          All products
        </button>
        {categories.map((item) => (
          <button
            key={item.id}
            className={category === item.id ? "active" : ""}
            onClick={() => update({ category: String(item.id), search: null })}
          >
            {item.name}
          </button>
        ))}
      </div>
      {search && (
        <p className="result-note">
          Results for <strong>“{search}”</strong>{" "}
          <button onClick={() => update({ search: null })}>Clear search</button>
        </p>
      )}
      {loading ? (
        <PageLoader />
      ) : error ? (
        <ErrorState
          message={error}
          retry={() => setReloadKey((value) => value + 1)}
        />
      ) : page?.content.length ? (
        <>
          <div className="catalog-count">
            Showing {page.content.length} of {page.totalElements} good things
          </div>
          <div className="product-grid">
            {page.content.map((product) => (
              <ProductCard
                key={product.id}
                product={product}
                onAdd={addProduct}
              />
            ))}
          </div>
          {page.totalPages > 1 && (
            <div className="pagination">
              <button
                disabled={pageNumber === 0}
                onClick={() => {
                  const next = new URLSearchParams(searchParams);
                  next.set("page", String(pageNumber - 1));
                  setSearchParams(next);
                }}
              >
                Previous
              </button>
              <span>
                Page {pageNumber + 1} of {page.totalPages}
              </span>
              <button
                disabled={page.lastPage}
                onClick={() => {
                  const next = new URLSearchParams(searchParams);
                  next.set("page", String(pageNumber + 1));
                  setSearchParams(next);
                }}
              >
                Next <ArrowRight size={15} />
              </button>
            </div>
          )}
        </>
      ) : (
        <EmptyState
          title="No good things found"
          text="Try another search or explore every product in the shop."
          link="/shop"
          linkText="View all products"
        />
      )}
    </div>
  );
}
