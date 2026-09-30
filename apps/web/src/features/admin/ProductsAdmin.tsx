import { useEffect, useState, type FormEvent } from "react";
import { ArrowRight, Pencil, Plus, Trash2, X } from "lucide-react";
import {
  api,
  type Category,
  type Page,
  type Product,
  type ProductInput,
} from "../../shared/api/api";
import { EmptyState, ErrorState, PageLoader } from "../../shared/ui/Feedback";
import { money } from "../../shared/format/money";
import { loadCategories } from "./loadCategories";

const blankProduct: ProductInput = {
  name: "",
  description: "",
  quantity: 0,
  price: 0,
  discount: 0,
};

export function ProductsAdmin() {
  const [page, setPage] = useState<Page<Product> | null>(null);
  const [categories, setCategories] = useState<Category[]>([]);
  const [pageNumber, setPageNumber] = useState(0);
  const [editing, setEditing] = useState<Product | null>(null);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState<ProductInput>(blankProduct);
  const [categoryId, setCategoryId] = useState(0);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  async function reload() {
    setPage(
      await api.products({
        pageNumber,
        pageSize: 10,
        sortBy: "id",
        sortOrder: "desc",
      }),
    );
  }
  useEffect(() => {
    let active = true;
    setLoading(true);
    Promise.all([
      api.products({
        pageNumber,
        pageSize: 10,
        sortBy: "id",
        sortOrder: "desc",
      }),
      loadCategories(),
    ])
      .then(([result, list]) => {
        if (active) {
          setPage(result);
          setCategories(list);
          if (!categoryId) setCategoryId(list[0]?.id || 0);
        }
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
  }, [pageNumber]);
  function create() {
    setEditing(null);
    setForm(blankProduct);
    setCategoryId(categories[0]?.id || 0);
    setShowForm(true);
    setError("");
  }
  function edit(product: Product) {
    setEditing(product);
    setForm({
      name: product.name,
      description: product.description,
      quantity: product.quantity,
      price: product.price,
      discount: product.discount,
    });
    setCategoryId(product.category.id);
    setShowForm(true);
    setError("");
  }
  async function save(event: FormEvent) {
    event.preventDefault();
    if (!categoryId) {
      setError("Create a category first.");
      return;
    }
    if (form.discount > form.price) {
      setError("Discount cannot exceed the price.");
      return;
    }
    setBusy(true);
    setError("");
    try {
      if (editing) await api.updateProduct(editing.id, form);
      else await api.createProduct(categoryId, form);
      setShowForm(false);
      await reload();
    } catch (error) {
      setError(
        error instanceof Error ? error.message : "Could not save product.",
      );
    } finally {
      setBusy(false);
    }
  }
  async function remove(product: Product) {
    if (!window.confirm(`Delete ${product.name}? This cannot be undone.`))
      return;
    setBusy(true);
    setError("");
    try {
      await api.deleteProduct(product.id);
      if (page?.content.length === 1 && pageNumber > 0)
        setPageNumber(pageNumber - 1);
      else await reload();
    } catch (error) {
      setError(
        error instanceof Error ? error.message : "Could not delete product.",
      );
    } finally {
      setBusy(false);
    }
  }
  return (
    <>
      <div className="admin-heading with-action">
        <div>
          <span className="eyebrow purple">CATALOG CONTROL</span>
          <h1>Products</h1>
          <p>Keep your collection looking its best.</p>
        </div>
        <button className="button button-dark" onClick={create}>
          <Plus size={17} /> Add product
        </button>
      </div>
      {error && (
        <div className="form-error" role="alert">
          {error}
        </div>
      )}
      {showForm && (
        <div className="admin-form-panel">
          <div className="panel-heading">
            <h2>{editing ? "Edit product" : "Add a product"}</h2>
            <button
              className="icon-button"
              aria-label="Close form"
              onClick={() => setShowForm(false)}
            >
              <X size={18} />
            </button>
          </div>
          <form className="form-stack" onSubmit={save}>
            <div className="form-row">
              <label>
                Product name
                <input
                  required
                  minLength={2}
                  value={form.name}
                  onChange={(event) =>
                    setForm({ ...form, name: event.target.value })
                  }
                />
              </label>
              <label>
                Category
                <select
                  value={categoryId}
                  disabled={!!editing}
                  onChange={(event) =>
                    setCategoryId(Number(event.target.value))
                  }
                >
                  {categories.map((item) => (
                    <option key={item.id} value={item.id}>
                      {item.name}
                    </option>
                  ))}
                </select>
              </label>
            </div>
            {editing && (
              <p className="small-note">
                This API keeps a product in its original category when editing.
              </p>
            )}
            <label>
              Description
              <textarea
                required
                rows={3}
                value={form.description}
                onChange={(event) =>
                  setForm({ ...form, description: event.target.value })
                }
              />
            </label>
            <div className="form-row three">
              <label>
                Price
                <input
                  type="number"
                  required
                  min={0}
                  step="0.01"
                  value={form.price}
                  onChange={(event) =>
                    setForm({ ...form, price: Number(event.target.value) })
                  }
                />
              </label>
              <label>
                Discount amount
                <input
                  type="number"
                  required
                  min={0}
                  step="0.01"
                  value={form.discount}
                  onChange={(event) =>
                    setForm({ ...form, discount: Number(event.target.value) })
                  }
                />
              </label>
              <label>
                Stock
                <input
                  type="number"
                  required
                  min={0}
                  step="1"
                  value={form.quantity}
                  onChange={(event) =>
                    setForm({ ...form, quantity: Number(event.target.value) })
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
              <button
                className="button button-dark"
                disabled={busy || !categories.length}
              >
                {busy ? "Saving…" : "Save product"}
              </button>
            </div>
          </form>
        </div>
      )}
      {loading ? (
        <PageLoader />
      ) : !page ? (
        <ErrorState message="Could not load products." />
      ) : !page.content.length ? (
        <EmptyState
          title="No products yet"
          text="Add the first product to make the storefront come alive."
        />
      ) : (
        <div className="admin-table-wrap">
          <table className="admin-table">
            <thead>
              <tr>
                <th>Product</th>
                <th>Category</th>
                <th>Price</th>
                <th>Stock</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {page.content.map((product) => (
                <tr key={product.id}>
                  <td>
                    <strong>{product.name}</strong>
                    <small>#{product.id}</small>
                  </td>
                  <td>{product.category.name}</td>
                  <td>{money(product.price - product.discount)}</td>
                  <td>
                    <span
                      className={`stock-badge ${product.quantity < 1 ? "out" : ""}`}
                    >
                      {product.quantity < 1
                        ? "Out of stock"
                        : `${product.quantity} in stock`}
                    </span>
                  </td>
                  <td>
                    <div className="table-actions">
                      <button
                        aria-label={`Edit ${product.name}`}
                        onClick={() => edit(product)}
                      >
                        <Pencil size={16} />
                      </button>
                      <button
                        aria-label={`Delete ${product.name}`}
                        disabled={busy}
                        onClick={() => void remove(product)}
                      >
                        <Trash2 size={16} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
      {page && page.totalPages > 1 && (
        <div className="pagination">
          <button
            disabled={pageNumber === 0}
            onClick={() => setPageNumber(pageNumber - 1)}
          >
            Previous
          </button>
          <span>
            Page {pageNumber + 1} of {page.totalPages}
          </span>
          <button
            disabled={page.lastPage}
            onClick={() => setPageNumber(pageNumber + 1)}
          >
            Next <ArrowRight size={15} />
          </button>
        </div>
      )}
    </>
  );
}
