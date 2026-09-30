import { api } from "../../shared/api/api";

export async function loadCategories() {
  const first = await api.categories(0, 50);
  const rest = await Promise.all(
    Array.from({ length: Math.max(0, first.totalPages - 1) }, (_, i) =>
      api.categories(i + 1, 50),
    ),
  );
  return [first, ...rest].flatMap((page) => page.content);
}
