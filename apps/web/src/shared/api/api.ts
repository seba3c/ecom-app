// Shapes mirror contracts/openapi/openapi.yaml. Authentication stays in the
// HTTP-only cookie; the JWT returned at sign-in is never stored by the client.
export type Role = "ROLE_USER" | "ROLE_SELLER" | "ROLE_ADMIN";
export type UserInfo = {
  id: number;
  username: string;
  roles: Role[];
  jwtToken: string | null;
};
export type Page<T> = {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  lastPage: boolean;
};
export type Category = { id: number; name: string };
export type Product = {
  id: number;
  name: string;
  description: string;
  quantity: number;
  price: number;
  discount: number;
  category: Category;
};
export type ProductInput = Pick<
  Product,
  "name" | "description" | "quantity" | "price" | "discount"
>;
export type AddressInput = {
  streetLine1: string;
  streetLine2: string | null;
  city: string;
  state: string;
  country: string;
  zipCode: string;
};
export type Address = AddressInput & { id: number };
export type CartItem = {
  id: number;
  quantity: number;
  price: number;
  discount: number;
  product: Product;
};
export type Cart = { id: number; totalPrice: number; cartItems: CartItem[] };
export type Order = {
  id: number;
  status: string;
  totalAmount: number;
  shippingAddressId: number;
  orderDate: string;
};
export type CatalogParams = {
  pageNumber?: number;
  pageSize?: number;
  sortBy?: string;
  sortOrder?: "asc" | "desc";
};

const base = (import.meta.env.VITE_API_BASE_URL || "/api").replace(/\/$/, "");

export class ApiError extends Error {
  constructor(
    public status: number,
    message: string,
  ) {
    super(message);
    this.name = "ApiError";
  }
}

function errorMessage(body: unknown, fallback: string): string {
  if (body && typeof body === "object") {
    const record = body as Record<string, unknown>;
    if (typeof record.message === "string") return record.message;
    if (typeof record.detail === "string") return record.detail;
    const first = Object.values(record).find(
      (value) => typeof value === "string",
    );
    if (typeof first === "string") return first;
  }
  return fallback;
}

export async function request<T>(
  path: string,
  options: RequestInit = {},
): Promise<T> {
  let response: Response;
  try {
    response = await fetch(`${base}${path}`, {
      ...options,
      credentials: "include",
      headers: {
        ...(options.body ? { "Content-Type": "application/json" } : {}),
        ...options.headers,
      },
    });
  } catch {
    throw new ApiError(
      0,
      "Could not reach the store. Check that the API is running.",
    );
  }
  const raw = await response.text();
  let body: unknown = raw;
  if (raw) {
    try {
      body = JSON.parse(raw);
    } catch {
      /* Some endpoints return plain text. */
    }
  }
  if (!response.ok)
    throw new ApiError(
      response.status,
      errorMessage(body, `Request failed (${response.status}).`),
    );
  return body as T;
}

function query(params: Record<string, string | number | undefined>): string {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== "") search.set(key, String(value));
  });
  return search.toString() ? `?${search}` : "";
}

export const api = {
  currentUser: () => request<UserInfo | { message: string }>("/auth/user"),
  signIn: (username: string, password: string) =>
    request<UserInfo>("/auth/signin", {
      method: "POST",
      body: JSON.stringify({ username, password }),
    }),
  signUp: (username: string, email: string, password: string) =>
    request<{ message: string }>("/auth/signup", {
      method: "POST",
      body: JSON.stringify({ username, email, password }),
    }),
  signOut: () =>
    request<{ message: string }>("/auth/signout", { method: "POST" }),
  categories: (pageNumber = 0, pageSize = 50) =>
    request<Page<Category>>(
      `/public/categories${query({ pageNumber, pageSize, sortBy: "name", sortOrder: "asc" })}`,
    ),
  products: (params: CatalogParams = {}) =>
    request<Page<Product>>(`/public/products${query(params)}`),
  searchProducts: (keyword: string, params: CatalogParams = {}) =>
    request<Page<Product>>(
      `/public/products/keyword/${encodeURIComponent(keyword)}${query(params)}`,
    ),
  categoryProducts: (id: number, params: CatalogParams = {}) =>
    request<Page<Product>>(`/public/categories/${id}/products${query(params)}`),
  product: (id: number) => request<Product>(`/public/products/${id}`),
  cart: () => request<Cart>("/my_cart"),
  addToCart: (id: number, quantity: number) =>
    request<Cart>(`/my_cart/${id}/quantity/${quantity}`, { method: "POST" }),
  setCartQuantity: (id: number, quantity: number) =>
    request<Cart>(`/my_cart/${id}/quantity/${quantity}`, { method: "PUT" }),
  removeFromCart: (id: number) =>
    request<Cart>(`/my_cart/${id}`, { method: "DELETE" }),
  addresses: () => request<{ content: Address[] }>("/addresses"),
  createAddress: (data: AddressInput) =>
    request<Address>("/addresses", {
      method: "POST",
      body: JSON.stringify(data),
    }),
  updateAddress: (id: number, data: AddressInput) =>
    request<Address>(`/addresses/${id}`, {
      method: "PUT",
      body: JSON.stringify(data),
    }),
  deleteAddress: (id: number) =>
    request<void>(`/addresses/${id}`, { method: "DELETE" }),
  placeOrder: (addressId: number) =>
    request<Order>("/orders", {
      method: "POST",
      body: JSON.stringify({
        addressId,
        paymentMethod: "MANUAL",
        pgName: "Pending",
        pgPaymentId: null,
        pgStatus: null,
        pgResponse: null,
      }),
    }),
  createCategory: (name: string) =>
    request<Category>("/admin/categories", {
      method: "POST",
      body: JSON.stringify({ name }),
    }),
  updateCategory: (id: number, name: string) =>
    request<Category>(`/admin/categories/${id}`, {
      method: "PUT",
      body: JSON.stringify({ name }),
    }),
  deleteCategory: (id: number) =>
    request<Category>(`/admin/categories/${id}`, { method: "DELETE" }),
  createProduct: (categoryId: number, data: ProductInput) =>
    request<Product>(`/admin/categories/${categoryId}/products`, {
      method: "POST",
      body: JSON.stringify(data),
    }),
  updateProduct: (id: number, data: ProductInput) =>
    request<Product>(`/admin/products/${id}`, {
      method: "PUT",
      body: JSON.stringify(data),
    }),
  deleteProduct: (id: number) =>
    request<Product>(`/admin/products/${id}`, { method: "DELETE" }),
  adminCarts: (pageNumber = 0) =>
    request<Page<Cart>>(`/admin/carts${query({ pageNumber, pageSize: 10 })}`),
};
