import { useEffect, useMemo, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { storeApi } from '../api/storeApi';
import { useCart } from '../hooks/useCart';
import type { ProductDto } from '../types/api';
import { getCategoryName } from '../utils/catalog';
import { formatCurrency } from '../utils/format';
import { getProductImageUri } from '../utils/productImage';

export function ProductDetailPage() {
  const { id } = useParams();
  const cart = useCart();
  const [product, setProduct] = useState<ProductDto | null>(null);
  const [quantity, setQuantity] = useState(1);
  const [isLoading, setIsLoading] = useState(true);
  const [message, setMessage] = useState<string | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const productId = useMemo(() => Number(id), [id]);

  useEffect(() => {
    let mounted = true;

    async function loadProduct() {
      if (!Number.isFinite(productId)) {
        setErrorMessage('Invalid product id');
        setIsLoading(false);
        return;
      }

      setIsLoading(true);
      setErrorMessage(null);

      try {
        const data = await storeApi.getProduct(productId);
        if (mounted) {
          setProduct(data);
        }
      } catch (error) {
        if (mounted) {
          setErrorMessage(error instanceof Error ? error.message : 'Failed to load product');
        }
      } finally {
        if (mounted) {
          setIsLoading(false);
        }
      }
    }

    loadProduct();

    return () => {
      mounted = false;
    };
  }, [productId]);

  async function handleAddToCart(): Promise<void> {
    if (!product) {
      return;
    }

    setMessage(null);
    setErrorMessage(null);

    try {
      await cart.addProduct(product.id, quantity);
      setMessage(`${quantity} item(s) added to cart.`);
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : 'Failed to add item to cart');
    }
  }

  if (isLoading) {
    return <div className="rounded-md border border-stone-200 bg-white p-6 text-stone-500">Loading product...</div>;
  }

  if (errorMessage) {
    return <div className="rounded-md border border-rose-200 bg-rose-50 p-6 text-rose-700">{errorMessage}</div>;
  }

  if (!product) {
    return <div className="rounded-md border border-stone-200 bg-white p-6 text-stone-500">Product not found.</div>;
  }

  return (
    <section className="grid gap-6 rounded-xl border border-stone-200 bg-white p-6 shadow-sm md:grid-cols-2">
      <div className="h-72 overflow-hidden rounded-lg">
        <img
          src={getProductImageUri(product.name, product.categoryId)}
          alt={product.name}
          className="h-full w-full object-cover"
        />
      </div>

      <div className="space-y-4">
        <p className="text-xs font-medium uppercase tracking-wide text-stone-400">{getCategoryName(product.categoryId)}</p>
        <h1 className="text-3xl font-bold tracking-tight text-stone-900">{product.name}</h1>
        <p className="text-sm leading-6 text-stone-600">{product.description}</p>
        <p className="text-2xl font-bold text-stone-900">{formatCurrency(product.price)}</p>

        <div className="flex flex-wrap items-end gap-3">
          <label className="text-sm font-medium text-stone-700">
            Quantity
            <select
              value={quantity}
              onChange={(event) => setQuantity(Number(event.target.value))}
              className="ml-2 rounded-lg border border-stone-300 px-2 py-1"
            >
              {[1, 2, 3, 4, 5].map((value) => (
                <option key={value} value={value}>
                  {value}
                </option>
              ))}
            </select>
          </label>

          <button
            type="button"
            onClick={handleAddToCart}
            disabled={cart.isLoading}
            className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-indigo-700 disabled:cursor-not-allowed disabled:bg-stone-300"
          >
            Add to Cart
          </button>

          <Link to="/cart" className="text-sm font-medium text-indigo-600 underline transition-colors hover:text-indigo-700">
            View cart
          </Link>
        </div>

        {message ? <p className="text-sm text-emerald-700">{message}</p> : null}
      </div>
    </section>
  );
}
