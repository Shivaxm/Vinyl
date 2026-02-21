import { useEffect, useMemo, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { storeApi } from '../api/storeApi';
import type { OrderDto } from '../types/api';
import { formatCurrency } from '../utils/format';

export function OrderSuccessPage() {
  const [params] = useSearchParams();
  const [order, setOrder] = useState<OrderDto | null>(null);
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

    async function maybeLoadOrder() {
      if (!orderId) {
        return;
      }

      try {
        const data = await storeApi.getOrder(orderId);
        if (mounted) {
          setOrder(data);
        }
      } catch (error) {
        if (mounted) {
          setErrorMessage(error instanceof Error ? error.message : 'Could not load order details');
        }
      }
    }

    maybeLoadOrder();

    return () => {
      mounted = false;
    };
  }, [orderId]);

  return (
    <section className="mx-auto max-w-2xl space-y-4 rounded-xl border border-emerald-200 bg-white p-6 shadow-sm">
      <h1 className="text-2xl font-bold tracking-tight text-emerald-800">Order Confirmed</h1>
      <p className="text-sm text-slate-700">
        Payment was completed and Stripe redirected you back to the app.
      </p>

      {orderId ? <p className="text-sm text-slate-600">Order ID: {orderId}</p> : null}

      {order ? (
        <div className="rounded-md border border-slate-200 bg-slate-50 p-4 text-sm">
          <p className="font-medium text-slate-800">Status: {order.status}</p>
          <p className="mt-1 text-slate-700">Total: {formatCurrency(order.totalPrice)}</p>
        </div>
      ) : null}

      {errorMessage ? <p className="text-sm text-rose-700">{errorMessage}</p> : null}

      <div className="flex gap-3">
        <Link to="/orders" className="rounded-md bg-slate-900 px-4 py-2 text-sm font-medium text-white hover:bg-slate-700">
          View Orders
        </Link>
        <Link to="/" className="rounded-md border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-100">
          Continue Shopping
        </Link>
      </div>
    </section>
  );
}
