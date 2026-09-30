export function money(value: number) {
  return new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: import.meta.env.VITE_CURRENCY || "USD",
  }).format(value);
}
