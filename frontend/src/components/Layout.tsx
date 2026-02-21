import { useEffect } from 'react';
import { Outlet } from 'react-router-dom';
import { useCart } from '../hooks/useCart';
import { Navbar } from './Navbar';

export function Layout() {
  const cart = useCart();

  useEffect(() => {
    if (!cart.mergeNotice) return;
    const timer = setTimeout(() => cart.setMergeNotice(null), 5000);
    return () => clearTimeout(timer);
  }, [cart.mergeNotice, cart.setMergeNotice]);

  return (
    <div className="min-h-screen bg-slate-50 text-slate-900">
      <Navbar />
      {cart.mergeNotice ? (
        <div className="mx-auto mt-4 max-w-6xl px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between rounded-md border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-800">
            <span>{cart.mergeNotice}</span>
            <button
              type="button"
              className="font-semibold text-emerald-900"
              onClick={() => cart.setMergeNotice(null)}
            >
              Dismiss
            </button>
          </div>
        </div>
      ) : null}
      <main className="mx-auto max-w-6xl px-4 py-6 sm:px-6 lg:px-8">
        <Outlet />
      </main>
    </div>
  );
}
