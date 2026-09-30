import { useEffect, useState } from "react";
import { ArrowLeft, ArrowRight, Sparkles, Truck } from "lucide-react";
import { Link, useNavigate, useParams } from "react-router";
import { api, type Product } from "../../shared/api/api";
import { ErrorState, PageLoader } from "../../shared/ui/Feedback";
import { money } from "../../shared/format/money";
import { useShop } from "../../app/ShopContext";
import { ProductArt } from "./ProductArt";

export function ProductPage() {
  const { id } = useParams();
  const [product, setProduct] = useState<Product | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [quantity, setQuantity] = useState(1);
  const { addProduct } = useShop();
  const navigate = useNavigate();
  useEffect(() => {
    setLoading(true);
    setError("");
    setProduct(null);
    setQuantity(1);
    const parsed = Number(id);
    if (!Number.isInteger(parsed) || parsed < 1) {
      setError("This product does not exist.");
      setLoading(false);
      return;
    }
    api
      .product(parsed)
      .then(setProduct)
      .catch((error) =>
        setError(
          error instanceof Error ? error.message : "Could not load product.",
        ),
      )
      .finally(() => setLoading(false));
  }, [id]);
  if (loading) return <PageLoader />;
  if (error || !product)
    return (
      <div className="container">
        <ErrorState message={error || "Product not found."} />
        <button className="text-link" onClick={() => navigate("/shop")}>
          Back to shop
        </button>
      </div>
    );
  const current = Math.max(0, product.price - product.discount);
  return (
    <div className="container product-page">
      <Link className="back-link" to="/shop">
        <ArrowLeft size={17} /> Back to shop
      </Link>
      <div className="product-detail">
        <ProductArt product={product} className="detail-art" />
        <div className="product-info">
          <span className="eyebrow purple">{product.category.name}</span>
          <h1>{product.name}</h1>
          <p className="product-description">{product.description}</p>
          <div className="detail-price">
            <strong>{money(current)}</strong>
            {product.discount > 0 && (
              <>
                <s>{money(product.price)}</s>
                <span className="sale-pill">
                  Save {money(product.discount)}
                </span>
              </>
            )}
          </div>
          <div className="detail-divider" />
          <p className="stock-line">
            {product.quantity > 0 ? (
              <>
                <span className="stock-dot" /> In stock · {product.quantity}{" "}
                available
              </>
            ) : (
              "Currently sold out"
            )}
          </p>
          <div className="detail-actions">
            <div className="quantity-stepper">
              <button
                aria-label="Decrease quantity"
                disabled={quantity <= 1}
                onClick={() => setQuantity(quantity - 1)}
              >
                −
              </button>
              <span>{quantity}</span>
              <button
                aria-label="Increase quantity"
                disabled={quantity >= product.quantity}
                onClick={() => setQuantity(quantity + 1)}
              >
                +
              </button>
            </div>
            <button
              className="button button-dark"
              disabled={product.quantity < 1}
              onClick={() => void addProduct(product, quantity)}
            >
              Add to cart <ArrowRight size={18} />
            </button>
          </div>
          <div className="detail-perks">
            <span>
              <Sparkles size={18} /> Chosen with care
            </span>
            <span>
              <Truck size={18} /> Made for your everyday
            </span>
          </div>
        </div>
      </div>
    </div>
  );
}
