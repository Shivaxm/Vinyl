import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { useSearchParams } from 'react-router-dom';
import { storeApi } from '../api/storeApi';

export function CheckoutCancelPage() {
  const [params] = useSearchParams();
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const orderId = useMemo(() => {
    const value = params.get('orderId');
    if (!value) {
      return null;
    }
    const parsed = Number(value);
    return Number.isFinite(parsed) ? parsed : null;
  }, [params]);

  useEffect(() => {
    let mounted = true;
    async function cancelPendingOrder() {
      if (!orderId) {
        return;
      }
      try {
        await storeApi.cancelPendingOrder(orderId);
      } catch (error) {
        if (mounted) {
          setErrorMessage(error instanceof Error ? error.message : 'Could not cancel pending order');
        }
      }
    }

    cancelPendingOrder();
    return () => {
      mounted = false;
    };
  }, [orderId]);

  return (
    <section className="mx-auto max-w-xl space-y-4 rounded-xl border border-amber-200 bg-white p-6 shadow-sm">
      <h1 className="text-2xl font-bold tracking-tight text-amber-800">Checkout Canceled</h1>
      <p className="text-sm text-stone-700">
        Your payment was canceled and no charge was captured. Your cart remains available.
      </p>
      {errorMessage ? <p className="text-sm text-rose-700">{errorMessage}</p> : null}
      <div className="flex gap-3">
        <Link to="/cart" className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-indigo-700">
          Back to Cart
        </Link>
        <Link to="/" className="rounded-lg border border-stone-300 px-4 py-2 text-sm font-medium text-stone-700 transition-colors hover:bg-stone-100">
          Browse Products
        </Link>
      </div>
    </section>
  );
}
