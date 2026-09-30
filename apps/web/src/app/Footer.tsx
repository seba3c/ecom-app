import { Instagram } from "lucide-react";
import { Link } from "react-router";

export function Footer() {
  return (
    <footer className="site-footer">
      <div className="container footer-grid">
        <div>
          <Link to="/" className="brand footer-brand">
            <span className="brand-mark">✳</span>goodthings
            <span className="brand-dot">.</span>
          </Link>
          <p>
            Objects with a little extra joy.
            <br />
            Made for your everyday.
          </p>
        </div>
        <div>
          <h4>Explore</h4>
          <Link to="/shop">Shop all</Link>
          <Link to="/shop?sortBy=id&sortOrder=desc">New arrivals</Link>
        </div>
        <div>
          <h4>Your space</h4>
          <Link to="/cart">Your cart</Link>
          <Link to="/signin">Your account</Link>
        </div>
        <div className="footer-note">
          <span className="footer-flower">✳</span>
          <p>Better days start with the little things.</p>
        </div>
      </div>
      <div className="container footer-bottom">
        <span>© {new Date().getFullYear()} goodthings</span>
        <span>Thoughtfully picked. Made to delight.</span>
        <Instagram size={17} aria-hidden="true" />
      </div>
    </footer>
  );
}
