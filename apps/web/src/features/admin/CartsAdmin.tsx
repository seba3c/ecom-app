import { useEffect, useState } from "react";
import { ArrowRight } from "lucide-react";
import { api, type Cart, type Page } from "../../shared/api/api";
import { EmptyState, ErrorState, PageLoader } from "../../shared/ui/Feedback";
import { money } from "../../shared/format/money";

export function CartsAdmin() {
  const [page, setPage] = useState<Page<Cart> | null>(null);
  const [pageNumber, setPageNumber] = useState(0);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  useEffect(() => {
    let active = true;
    setLoading(true);
    api
      .adminCarts(pageNumber)
      .then((result) => {
        if (active) setPage(result);
      })
      .catch((error) => {
        if (active)
          setError(
            error instanceof Error ? error.message : "Could not load carts.",
          );
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [pageNumber]);
  return (
    <>
      <div className="admin-heading">
        <span className="eyebrow purple">SHOPPER ACTIVITY</span>
        <h1>Active carts</h1>
        <p>A snapshot of carts currently in the store.</p>
      </div>
      {loading ? (
        <PageLoader />
      ) : error ? (
        <ErrorState message={error} />
      ) : !page?.content.length ? (
        <EmptyState
          title="No active carts"
          text="Carts will appear here when shoppers start adding products."
        />
      ) : (
        <>
          <div className="admin-table-wrap">
            <table className="admin-table">
              <thead>
                <tr>
                  <th>Cart</th>
                  <th>Items</th>
                  <th>Products</th>
                  <th>Total</th>
                </tr>
              </thead>
              <tbody>
                {page.content.map((cart) => (
                  <tr key={cart.id}>
                    <td>
                      <strong>Cart #{cart.id}</strong>
                    </td>
                    <td>
                      {cart.cartItems.reduce(
                        (sum, item) => sum + item.quantity,
                        0,
                      )}
                    </td>
                    <td>
                      {cart.cartItems.length
                        ? cart.cartItems
                            .map((item) => item.product.name)
                            .join(", ")
                        : "Empty"}
                    </td>
                    <td>
                      <strong>{money(cart.totalPrice)}</strong>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          {page.totalPages > 1 && (
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
      )}
    </>
  );
}
