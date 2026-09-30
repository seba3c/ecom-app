import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { api } from "./api";

const json = (value: unknown, status = 200) =>
  new Response(JSON.stringify(value), {
    status,
    headers: { "Content-Type": "application/json" },
  });

describe("shared API client", () => {
  const fetchMock = vi.fn<typeof fetch>();
  beforeEach(() => {
    fetchMock.mockReset();
    vi.stubGlobal("fetch", fetchMock);
  });
  afterEach(() => vi.unstubAllGlobals());

  it("uses contract paths, pagination and cookie credentials for public requests", async () => {
    fetchMock.mockResolvedValue(
      json({
        content: [],
        pageNumber: 1,
        pageSize: 12,
        totalElements: 0,
        totalPages: 0,
        lastPage: true,
      }),
    );
    await api.searchProducts("desk lamp", {
      pageNumber: 1,
      pageSize: 12,
      sortBy: "price",
      sortOrder: "desc",
    });
    expect(fetchMock).toHaveBeenCalledWith(
      "/api/public/products/keyword/desk%20lamp?pageNumber=1&pageSize=12&sortBy=price&sortOrder=desc",
      expect.objectContaining({ credentials: "include" }),
    );
  });

  it("uses the cookie session for protected cart changes", async () => {
    fetchMock.mockResolvedValue(
      json({ id: 1, totalPrice: 0, cartItems: [] }, 201),
    );
    await api.addToCart(7, 2);
    expect(fetchMock).toHaveBeenCalledWith(
      "/api/my_cart/7/quantity/2",
      expect.objectContaining({ method: "POST", credentials: "include" }),
    );
    expect(fetchMock.mock.calls[0][1]?.headers).not.toHaveProperty(
      "Authorization",
    );
  });

  it("places a pending order without fabricated payment identifiers", async () => {
    fetchMock.mockResolvedValue(
      json(
        {
          id: 42,
          status: "PENDING",
          totalAmount: 19.99,
          shippingAddressId: 3,
          orderDate: "2026-09-30T12:00:00Z",
        },
        201,
      ),
    );
    await api.placeOrder(3);
    const [url, options] = fetchMock.mock.calls[0];
    expect(url).toBe("/api/orders");
    expect(options?.method).toBe("POST");
    expect(JSON.parse(String(options?.body))).toEqual({
      addressId: 3,
      paymentMethod: "MANUAL",
      pgName: "Pending",
      pgPaymentId: null,
      pgStatus: null,
      pgResponse: null,
    });
  });

  it("creates products in a category with the shared admin payload", async () => {
    fetchMock.mockResolvedValue(json({ id: 9 }, 201));
    await api.createProduct(4, {
      name: "Desk Lamp",
      description: "Warm light",
      quantity: 5,
      price: 30,
      discount: 2,
    });
    const [url, options] = fetchMock.mock.calls[0];
    expect(url).toBe("/api/admin/categories/4/products");
    expect(options?.method).toBe("POST");
    expect(JSON.parse(String(options?.body))).toEqual({
      name: "Desk Lamp",
      description: "Warm light",
      quantity: 5,
      price: 30,
      discount: 2,
    });
  });

  it("surfaces backend field validation messages", async () => {
    fetchMock.mockResolvedValue(
      json({ quantity: "Stock is insufficient" }, 400),
    );
    await expect(api.addToCart(7, 99)).rejects.toMatchObject({
      status: 400,
      message: "Stock is insufficient",
    });
  });
});
