import { useEffect, useState } from 'react';
import { storeApi } from '../api/storeApi';
import type { OrderDto } from '../types/api';
import { formatCurrency, formatDate } from '../utils/format';

function statusClass(status: OrderDto['status']): string {
  if (status === 'PAID') {
    return 'bg-emerald-100 text-emerald-800';
  }
  if (status === 'FAILED') {
    return 'bg-rose-100 text-rose-700';
  }
  if (status === 'CANCELED') {
    return 'bg-slate-200 text-slate-700';
  }
  return 'bg-amber-100 text-amber-800';
}

export function OrdersPage() {
  const [orders, setOrders] = useState<OrderDto[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    let mounted = true;

    async function loadOrders() {
      setIsLoading(true);
      setErrorMessage(null);
      try {
        const data = await storeApi.getOrders();
        if (mounted) {
          setOrders(data);
        }
      } catch (error) {
        if (mounted) {
          setErrorMessage(error instanceof Error ? error.message : 'Failed to load orders');
        }
      } finally {
        if (mounted) {
          setIsLoading(false);
        }
      }
    }

    loadOrders();

    return () => {
      mounted = false;
    };
  }, []);

  if (isLoading) {
    return <div className="rounded-md border border-slate-200 bg-white p-6">Loading orders...</div>;
  }

  if (errorMessage) {
    return <div className="rounded-md border border-rose-200 bg-rose-50 p-6 text-rose-700">{errorMessage}</div>;
  }

  return (
    <section className="space-y-4">
      <h1 className="text-2xl font-bold tracking-tight">Order History</h1>

      {orders.length === 0 ? (
        <div className="rounded-md border border-slate-200 bg-white p-6 text-slate-600">No orders yet.</div>
      ) : (
        orders.map((order) => (
          <article key={order.id} className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
            <div className="flex flex-wrap items-center justify-between gap-2">
              <p className="text-sm text-slate-600">
                Order #{order.id} • {formatDate(order.createdAt)}
              </p>
              <span className={`rounded-full px-2 py-1 text-xs font-semibold ${statusClass(order.status)}`}>
                {order.status}
              </span>
            </div>
            <ul className="mt-3 space-y-2 text-sm text-slate-700">
              {order.items.map((item) => (
                <li key={`${order.id}-${item.product.id}`} className="flex items-center justify-between">
                  <span>
                    {item.product.name} x {item.quantity}
                  </span>
                  <span className="font-medium">{formatCurrency(item.totalPrice)}</span>
                </li>
              ))}
            </ul>
            <div className="mt-3 border-t border-slate-100 pt-3 text-right text-sm font-semibold text-slate-900">
              Total: {formatCurrency(order.totalPrice)}
            </div>
          </article>
        ))
      )}
    </section>
  );
}
