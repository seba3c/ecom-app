import { useEffect, useState } from "react";
import { ArrowRight, Heart, Sparkles, Truck } from "lucide-react";
import { Link } from "react-router";
import { api, type Category, type Product } from "../../shared/api/api";
import { EmptyState, ErrorState, PageLoader } from "../../shared/ui/Feedback";
import { useShop } from "../../app/ShopContext";
import { ProductCard } from "./ProductCard";
import { allCategories } from "./allCategories";

export function HomePage() {
  const [products, setProducts] = useState<Product[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const { addProduct } = useShop();
  useEffect(() => {
    Promise.all([
      api.products({ pageSize: 8, sortBy: "id", sortOrder: "desc" }),
      allCategories(),
    ])
      .then(([page, list]) => {
        setProducts(page.content);
        setCategories(list.slice(0, 4));
      })
      .catch((error) =>
        setError(
          error instanceof Error ? error.message : "Could not load products.",
        ),
      )
      .finally(() => setLoading(false));
  }, []);
  return (
    <>
      <section className="hero container">
        <div className="hero-copy">
          <div className="hero-kicker">
            <span>✳</span> CURATED FOR EVERYDAY JOY
          </div>
          <h1>
            Find the good
            <br />
            <em>in everything.</em>
          </h1>
          <p>
            Colorful finds, useful favorites, and little surprises that make
            ordinary days feel anything but ordinary.
          </p>
          <Link className="button button-dark hero-cta" to="/shop">
            Explore the collection <ArrowRight size={18} />
          </Link>
          <div className="hero-subline">
            <span className="mini-avatars">✿ ✦ ✳</span>
            <span>Good things are waiting for you</span>
          </div>
        </div>
        <div className="hero-art" aria-hidden="true">
          <div className="hero-sun" />
          <div className="hero-arc" />
          <div className="hero-object hero-object-one">
            <span>✿</span>
          </div>
          <div className="hero-object hero-object-two">
            <span>✦</span>
          </div>
          <div className="hero-object hero-object-three">
            <span>✳</span>
          </div>
          <span className="hero-orbit hero-orbit-a">✦</span>
          <span className="hero-orbit hero-orbit-b">✳</span>
          <div className="hero-label">
            a little more
            <br />
            <strong>lovely</strong> every day!
          </div>
        </div>
      </section>
      <div className="benefit-bar">
        <div className="container benefit-inner">
          <span>
            <Sparkles size={19} /> Thoughtfully curated
          </span>
          <span>
            <Truck size={19} /> Made for real life
          </span>
          <span>
            <Heart size={19} /> Packed with joy
          </span>
        </div>
      </div>
      <section className="section container">
        <div className="section-heading">
          <div>
            <span className="eyebrow purple">THE GOOD STUFF</span>
            <h2>
              Something for everyone<span className="heading-flower">✳</span>
            </h2>
            <p>Discover a favorite you didn't know you needed.</p>
          </div>
          <Link className="text-link" to="/shop">
            Shop all products <ArrowRight size={18} />
          </Link>
        </div>
        {loading ? (
          <PageLoader />
        ) : error ? (
          <ErrorState message={error} />
        ) : products.length ? (
          <div className="product-grid">
            {products.map((product) => (
              <ProductCard
                key={product.id}
                product={product}
                onAdd={addProduct}
              />
            ))}
          </div>
        ) : (
          <EmptyState
            title="The shelves are getting ready"
            text="No products have been added yet. Check back soon for new favorites."
          />
        )}
      </section>
      <section className="category-section">
        <div className="container">
          <div className="section-heading">
            <div>
              <span className="eyebrow purple">FIND YOUR THING</span>
              <h2>Browse by mood.</h2>
              <p>Follow your curiosity wherever it goes.</p>
            </div>
          </div>
          <div className="category-grid">
            {categories.length ? (
              categories.map((category, index) => (
                <Link
                  className={`category-tile category-tile-${index % 4}`}
                  key={category.id}
                  to={`/shop?category=${category.id}`}
                >
                  <span className="category-symbol">
                    {["✳", "✿", "✦", "❋"][index % 4]}
                  </span>
                  <span>{category.name}</span>
                  <ArrowRight size={22} />
                </Link>
              ))
            ) : (
              <Link className="category-tile category-tile-0" to="/shop">
                <span className="category-symbol">✳</span>
                <span>Explore the shop</span>
                <ArrowRight size={22} />
              </Link>
            )}
          </div>
        </div>
      </section>
      <section className="container final-cta">
        <div>
          <span className="eyebrow">A LITTLE NOTE FROM US</span>
          <h2>
            Life's better with
            <br />
            good things in it.
          </h2>
          <p>Take a look around. Your next favorite might be one click away.</p>
          <Link className="button button-light" to="/shop">
            Let's find it <ArrowRight size={18} />
          </Link>
        </div>
        <span className="cta-sun" aria-hidden="true">
          ✳
        </span>
      </section>
    </>
  );
}
