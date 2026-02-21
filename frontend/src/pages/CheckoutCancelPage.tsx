import { Link } from 'react-router-dom';

export function CheckoutCancelPage() {
  return (
    <section className="mx-auto max-w-xl space-y-4 rounded-xl border border-amber-200 bg-white p-6 shadow-sm">
      <h1 className="text-2xl font-bold tracking-tight text-amber-800">Checkout Canceled</h1>
      <p className="text-sm text-slate-700">
        Your payment was canceled and no charge was captured. Your cart remains available.
      </p>
      <div className="flex gap-3">
        <Link to="/cart" className="rounded-md bg-slate-900 px-4 py-2 text-sm font-medium text-white hover:bg-slate-700">
          Back to Cart
        </Link>
        <Link to="/" className="rounded-md border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-100">
          Browse Products
        </Link>
      </div>
    </section>
  );
}
