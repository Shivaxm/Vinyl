import { useEffect, useMemo, useState } from 'react';
import { ProductCard } from '../components/ProductCard';
import { storeApi } from '../api/storeApi';
import { useCart } from '../hooks/useCart';
import type { ProductDto } from '../types/api';
import { getCategoryMapFromProducts } from '../utils/catalog';

export function HomePage() {
  const cart = useCart();
  const [products, setProducts] = useState<ProductDto[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [activeCategory, setActiveCategory] = useState<number | 'all'>('all');
  const [pendingProductIds, setPendingProductIds] = useState<Set<number>>(new Set());

  useEffect(() => {
    let mounted = true;

    async function loadProducts() {
      setIsLoading(true);
      setErrorMessage(null);
      try {
        const data = await storeApi.getProducts();
        if (mounted) {
          setProducts(data);
        }
      } catch (error) {
        if (mounted) {
          setErrorMessage(error instanceof Error ? error.message : 'Failed to load products');
        }
      } finally {
        if (mounted) {
          setIsLoading(false);
        }
      }
    }

    loadProducts();

    return () => {
      mounted = false;
    };
  }, []);

  const categories = useMemo(() => getCategoryMapFromProducts(products), [products]);

  const filteredProducts = useMemo(() => {
    if (activeCategory === 'all') {
      return products;
    }
    return products.filter((product) => product.categoryId === activeCategory);
  }, [activeCategory, products]);

  async function handleAddToCart(productId: number): Promise<void> {
    if (pendingProductIds.has(productId)) {
      return;
    }

    setPendingProductIds((previous) => {
      const next = new Set(previous);
      next.add(productId);
      return next;
    });

    try {
      await cart.addProduct(productId, 1);
    } finally {
      setPendingProductIds((previous) => {
        const next = new Set(previous);
        next.delete(productId);
        return next;
      });
    }
  }

  if (isLoading) {
    return <div className="rounded-md border border-stone-200 bg-white p-6 text-stone-500">Loading catalog...</div>;
  }

  if (errorMessage) {
    return <div className="rounded-md border border-rose-200 bg-rose-50 p-6 text-rose-700">{errorMessage}</div>;
  }

  return (
    <section className="space-y-6">
      <header className="space-y-1">
        <h1 className="text-2xl font-bold tracking-tight text-stone-900">Product Catalog</h1>
        <p className="text-sm text-stone-500">Browse products, add items as a guest, then log in to merge your cart.</p>
      </header>

      <div className="flex flex-wrap items-center gap-2">
        <button
          type="button"
          onClick={() => setActiveCategory('all')}
          className={`rounded-full px-3 py-1.5 text-sm ${
            activeCategory === 'all'
              ? 'bg-indigo-600 text-white'
              : 'border border-stone-200 bg-white text-stone-600 transition-colors hover:bg-stone-50'
          }`}
        >
          All
        </button>
        {categories.map((category) => (
          <button
            key={category.id}
            type="button"
            onClick={() => setActiveCategory(category.id)}
            className={`rounded-full px-3 py-1.5 text-sm ${
              activeCategory === category.id
                ? 'bg-indigo-600 text-white'
                : 'border border-stone-200 bg-white text-stone-600 transition-colors hover:bg-stone-50'
            }`}
          >
            {category.label}
          </button>
        ))}
      </div>

      {filteredProducts.length === 0 ? (
        <div className="rounded-md border border-stone-200 bg-white p-6 text-stone-500">No products found for this category.</div>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {filteredProducts.map((product) => (
            <ProductCard
              key={product.id}
              product={product}
              onAddToCart={handleAddToCart}
              disabled={pendingProductIds.has(product.id)}
            />
          ))}
        </div>
      )}
    </section>
  );
}
