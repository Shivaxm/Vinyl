const CATEGORY_NAME_MAP: Record<number, string> = {
  1: 'Electronics',
  2: 'Books',
  3: 'Clothing',
  4: 'Home',
  5: 'Toys',
};

export function getCategoryName(categoryId: number): string {
  return CATEGORY_NAME_MAP[categoryId] ?? `Category ${categoryId}`;
}

export function getCategoryMapFromProducts(
  products: Array<{ categoryId: number }>,
): Array<{ id: number; label: string }> {
  const ids = Array.from(new Set(products.map((product) => product.categoryId))).sort(
    (a, b) => a - b,
  );

  return ids.map((id) => ({ id, label: getCategoryName(id) }));
}
