import { createContext, useContext } from "react";
import type { Product } from "../shared/api/api";

export type Shop = {
  addProduct: (product: Product, quantity?: number) => Promise<void>;
  notify: (message: string) => void;
  refreshCart: () => void;
};
export const ShopContext = createContext<Shop | null>(null);
export function useShop() {
  const value = useContext(ShopContext);
  if (!value) throw new Error("Shop context required");
  return value;
}
