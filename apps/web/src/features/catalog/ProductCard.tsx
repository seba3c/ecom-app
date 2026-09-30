import { Link } from "react-router";
import { type Product } from "../../shared/api/api";
import { money } from "../../shared/format/money";
import { ProductArt } from "./ProductArt";

export function ProductCard({
  product,
  onAdd,
}: {
  product: Product;
  onAdd?: (product: Product) => void;
}) {
  const current = Math.max(0, product.price - product.discount);
  return (
    <article className="product-card">
      <Link to={`/products/${product.id}`} className="card-art-link">
        <ProductArt product={product} />
      </Link>
      <div className="product-card-body">
        <span className="eyebrow">{product.category.name}</span>
        <Link to={`/products/${product.id}`} className="product-name">
          {product.name}
        </Link>
        <div className="product-bottom">
          <div>
            <strong>{money(current)}</strong>
            {product.discount > 0 && <s>{money(product.price)}</s>}
          </div>
          {onAdd && (
            <button
              className="icon-button add-button"
              aria-label={`Add ${product.name} to cart`}
              title="Add to cart"
              onClick={() => onAdd(product)}
              disabled={product.quantity < 1}
            >
              +
            </button>
          )}
        </div>
        {product.quantity < 1 && <span className="stock-note">Sold out</span>}
      </div>
    </article>
  );
}
