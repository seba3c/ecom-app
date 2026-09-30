import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import {
  cleanup,
  fireEvent,
  render,
  screen,
  waitFor,
} from "@testing-library/react";
import { MemoryRouter } from "react-router";
import { App } from "./App";
import { SessionProvider } from "../features/auth/session";

const product = {
  id: 3,
  name: "Sunshine Desk Lamp",
  description: "A bright little lamp.",
  quantity: 8,
  price: 32,
  discount: 2,
  category: { id: 1, name: "Home Finds" },
};
const page = (content: unknown[]) => ({
  content,
  pageNumber: 0,
  pageSize: 12,
  totalElements: content.length,
  totalPages: content.length ? 1 : 0,
  lastPage: true,
});
const response = (body: unknown, status = 200) =>
  new Response(JSON.stringify(body), {
    status,
    headers: { "Content-Type": "application/json" },
  });

function mount(path: string) {
  return render(
    <MemoryRouter initialEntries={[path]}>
      <SessionProvider>
        <App />
      </SessionProvider>
    </MemoryRouter>,
  );
}

describe("storefront flows", () => {
  const fetchMock = vi.fn<typeof fetch>();
  beforeEach(() => {
    fetchMock.mockReset();
    vi.stubGlobal("fetch", fetchMock);
  });
  afterEach(() => {
    cleanup();
    vi.unstubAllGlobals();
  });

  it("loads real catalog data and sends guests to sign in when adding a product", async () => {
    fetchMock.mockImplementation(async (input) => {
      const path = String(input);
      if (path === "/api/auth/user")
        return response({ message: "No user details found" });
      if (path.startsWith("/api/public/categories"))
        return response(page([product.category]));
      if (path.startsWith("/api/public/products"))
        return response(page([product]));
      throw new Error(`Unexpected request: ${path}`);
    });
    mount("/shop");
    expect(await screen.findByText("Sunshine Desk Lamp")).toBeTruthy();
    fireEvent.click(
      screen.getByRole("button", { name: "Add Sunshine Desk Lamp to cart" }),
    );
    expect(
      await screen.findByRole("heading", { name: "Hello again." }),
    ).toBeTruthy();
    expect(
      fetchMock.mock.calls.some(([path]) =>
        String(path).includes("/my_cart/3/quantity/1"),
      ),
    ).toBe(false);
  });

  it("updates the signed-in cart through the quantity endpoint", async () => {
    const cart = {
      id: 4,
      totalPrice: 30,
      cartItems: [{ id: 5, quantity: 1, price: 32, discount: 2, product }],
    };
    fetchMock.mockImplementation(async (input, options) => {
      const path = String(input);
      if (path === "/api/auth/user")
        return response({
          id: 1,
          username: "user",
          roles: ["ROLE_USER"],
          jwtToken: null,
        });
      if (path === "/api/my_cart") return response(cart);
      if (path === "/api/my_cart/3/quantity/2" && options?.method === "PUT")
        return response({
          ...cart,
          totalPrice: 60,
          cartItems: [{ ...cart.cartItems[0], quantity: 2 }],
        });
      throw new Error(`Unexpected request: ${path}`);
    });
    mount("/cart");
    fireEvent.click(
      await screen.findByRole("button", {
        name: "Increase Sunshine Desk Lamp quantity",
      }),
    );
    await waitFor(() =>
      expect(
        fetchMock.mock.calls.some(
          ([path, options]) =>
            path === "/api/my_cart/3/quantity/2" && options?.method === "PUT",
        ),
      ).toBe(true),
    );
    await waitFor(() =>
      expect(screen.getAllByText("$60.00").length).toBeGreaterThan(0),
    );
  });

  it("increases quantity when a shopper adds an existing cart product", async () => {
    const cart = {
      id: 4,
      totalPrice: 30,
      cartItems: [{ id: 5, quantity: 1, price: 32, discount: 2, product }],
    };
    fetchMock.mockImplementation(async (input, options) => {
      const path = String(input);
      if (path === "/api/auth/user")
        return response({
          id: 1,
          username: "user",
          roles: ["ROLE_USER"],
          jwtToken: null,
        });
      if (path === "/api/my_cart") return response(cart);
      if (path.startsWith("/api/public/categories"))
        return response(page([product.category]));
      if (path.startsWith("/api/public/products"))
        return response(page([product]));
      if (path === "/api/my_cart/3/quantity/2" && options?.method === "PUT")
        return response({
          ...cart,
          totalPrice: 60,
          cartItems: [{ ...cart.cartItems[0], quantity: 2 }],
        });
      throw new Error(`Unexpected request: ${path}`);
    });
    mount("/shop");
    fireEvent.click(
      await screen.findByRole("button", {
        name: "Add Sunshine Desk Lamp to cart",
      }),
    );
    await waitFor(() =>
      expect(
        fetchMock.mock.calls.some(
          ([path, options]) =>
            path === "/api/my_cart/3/quantity/2" && options?.method === "PUT",
        ),
      ).toBe(true),
    );
  });

  it("routes an admin account into the management panel after sign-in", async () => {
    fetchMock.mockImplementation(async (input) => {
      const path = String(input);
      if (path === "/api/auth/user")
        return response({ message: "No user details found" });
      if (path === "/api/auth/signin")
        return response({
          id: 2,
          username: "admin",
          roles: ["ROLE_ADMIN"],
          jwtToken: "unused",
        });
      if (path === "/api/my_cart")
        return response({ id: 1, totalPrice: 0, cartItems: [] });
      if (path.startsWith("/api/public/products")) return response(page([]));
      if (path.startsWith("/api/public/categories")) return response(page([]));
      if (path.startsWith("/api/admin/carts")) return response(page([]));
      throw new Error(`Unexpected request: ${path}`);
    });
    mount("/signin");
    fireEvent.change(screen.getByLabelText("Username"), {
      target: { value: "admin" },
    });
    fireEvent.change(screen.getByLabelText("Password"), {
      target: { value: "adminpass" },
    });
    fireEvent.click(screen.getByRole("button", { name: /Sign in/ }));
    expect(await screen.findByText("Good morning, admin")).toBeTruthy();
  });

  it("shows a pending order after checkout", async () => {
    const cart = {
      id: 4,
      totalPrice: 30,
      cartItems: [{ id: 5, quantity: 1, price: 32, discount: 2, product }],
    };
    fetchMock.mockImplementation(async (input, options) => {
      const path = String(input);
      if (path === "/api/auth/user")
        return response({
          id: 1,
          username: "user",
          roles: ["ROLE_USER"],
          jwtToken: null,
        });
      if (path === "/api/my_cart") return response(cart);
      if (path === "/api/addresses")
        return response({
          content: [
            {
              id: 8,
              streetLine1: "42 Main St",
              streetLine2: null,
              city: "Madrid",
              state: "Madrid",
              country: "Spain",
              zipCode: "28001",
            },
          ],
        });
      if (path === "/api/orders" && options?.method === "POST")
        return response(
          {
            id: 29,
            status: "PENDING",
            totalAmount: 30,
            shippingAddressId: 8,
            orderDate: "2026-09-30T12:00:00Z",
          },
          201,
        );
      throw new Error(`Unexpected request: ${path}`);
    });
    mount("/checkout");
    fireEvent.click(
      await screen.findByRole("button", { name: /Place pending order/ }),
    );
    expect(
      await screen.findByRole("heading", { name: "Your order is in!" }),
    ).toBeTruthy();
    expect(screen.getByText("#29")).toBeTruthy();
    expect(
      fetchMock.mock.calls.some(
        ([path, options]) =>
          path === "/api/orders" && options?.method === "POST",
      ),
    ).toBe(true);
  });
});
