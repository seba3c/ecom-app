import { ArrowRight, Check, ChevronRight, MapPin } from "lucide-react";
import { Link, useLocation } from "react-router";
import { type Order } from "../../shared/api/api";
import { money } from "../../shared/format/money";

export function OrderSuccessPage() {
  const location = useLocation();
  const order = (location.state as { order?: Order } | null)?.order;
  return (
    <div className="container success-page">
      <div className="success-icon">
        <Check size={36} />
      </div>
      <span className="eyebrow purple">GOOD NEWS</span>
      <h1>
        {order
          ? "Your order is in!"
          : "Your order details are unavailable here."}
      </h1>
      {order ? (
        <>
          <p>
            Order <strong>#{order.id}</strong> was placed for{" "}
            <strong>{money(order.totalAmount)}</strong>. Its status is{" "}
            {order.status.toLowerCase()}, and payment is still pending.
          </p>
          <div className="success-detail">
            <MapPin size={19} /> Delivery address #{order.shippingAddressId}
            <ChevronRight size={18} />
          </div>
        </>
      ) : (
        <p>
          The API does not provide order history yet. You can continue exploring
          the shop.
        </p>
      )}
      <Link className="button button-dark" to="/shop">
        Keep exploring <ArrowRight size={18} />
      </Link>
    </div>
  );
}
