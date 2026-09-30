import { useEffect, useState, type FormEvent } from "react";
import { Pencil, Plus, Trash2 } from "lucide-react";
import { api, type Category } from "../../shared/api/api";
import { EmptyState, PageLoader } from "../../shared/ui/Feedback";
import { loadCategories } from "./loadCategories";

export function CategoriesAdmin() {
  const [categories, setCategories] = useState<Category[]>([]);
  const [name, setName] = useState("");
  const [editing, setEditing] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  useEffect(() => {
    loadCategories()
      .then(setCategories)
      .catch((error) =>
        setError(
          error instanceof Error ? error.message : "Could not load categories.",
        ),
      )
      .finally(() => setLoading(false));
  }, []);
  async function save(event: FormEvent) {
    event.preventDefault();
    setBusy(true);
    setError("");
    try {
      if (editing) await api.updateCategory(editing, name.trim());
      else await api.createCategory(name.trim());
      setCategories(await loadCategories());
      setName("");
      setEditing(null);
    } catch (error) {
      setError(
        error instanceof Error ? error.message : "Could not save category.",
      );
    } finally {
      setBusy(false);
    }
  }
  async function remove(category: Category) {
    if (!window.confirm(`Delete ${category.name}? This cannot be undone.`))
      return;
    setBusy(true);
    setError("");
    try {
      await api.deleteCategory(category.id);
      setCategories((list) => list.filter((item) => item.id !== category.id));
    } catch (error) {
      setError(
        error instanceof Error
          ? error.message
          : "Could not delete category. Products in this category may need to be removed first.",
      );
    } finally {
      setBusy(false);
    }
  }
  return (
    <>
      <div className="admin-heading">
        <span className="eyebrow purple">KEEP THINGS ORGANIZED</span>
        <h1>Categories</h1>
        <p>Give every good thing a place to belong.</p>
      </div>
      {error && (
        <div className="form-error" role="alert">
          {error}
        </div>
      )}
      <form className="category-create" onSubmit={save}>
        <label>
          {editing ? "Edit category" : "New category"}
          <input
            required
            minLength={2}
            value={name}
            onChange={(event) => setName(event.target.value)}
            placeholder="e.g. Living room favorites"
          />
        </label>
        <button className="button button-dark" disabled={busy}>
          {editing ? "Save changes" : "Add category"} <Plus size={17} />
        </button>
        {editing && (
          <button
            type="button"
            className="button button-outline"
            onClick={() => {
              setEditing(null);
              setName("");
            }}
          >
            Cancel
          </button>
        )}
      </form>
      {loading ? (
        <PageLoader />
      ) : categories.length ? (
        <div className="category-admin-grid">
          {categories.map((category, index) => (
            <div
              className={`category-admin-card category-tile-${index % 4}`}
              key={category.id}
            >
              <span className="category-symbol">
                {["✳", "✿", "✦", "❋"][index % 4]}
              </span>
              <div>
                <small>#{category.id}</small>
                <strong>{category.name}</strong>
              </div>
              <div className="table-actions">
                <button
                  aria-label={`Edit ${category.name}`}
                  onClick={() => {
                    setEditing(category.id);
                    setName(category.name);
                  }}
                >
                  <Pencil size={16} />
                </button>
                <button
                  aria-label={`Delete ${category.name}`}
                  disabled={busy}
                  onClick={() => void remove(category)}
                >
                  <Trash2 size={16} />
                </button>
              </div>
            </div>
          ))}
        </div>
      ) : (
        <EmptyState
          title="No categories yet"
          text="Create a category before adding products."
        />
      )}
    </>
  );
}
