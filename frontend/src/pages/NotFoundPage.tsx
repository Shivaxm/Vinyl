import { Link } from 'react-router-dom';

export function NotFoundPage() {
  return (
    <section className="mx-auto max-w-xl space-y-3 rounded-xl border border-slate-200 bg-white p-6 shadow-sm text-center">
      <h1 className="text-2xl font-bold tracking-tight">Page Not Found</h1>
      <p className="text-sm text-slate-600">The page you requested does not exist.</p>
      <Link to="/" className="inline-block rounded-md bg-slate-900 px-4 py-2 text-sm font-medium text-white hover:bg-slate-700">
        Go to Catalog
      </Link>
    </section>
  );
}
