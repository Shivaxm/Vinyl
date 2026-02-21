import { Link } from 'react-router-dom';

export function NotFoundPage() {
  return (
    <section className="mx-auto max-w-xl space-y-3 rounded-xl border border-stone-200 bg-white p-6 text-center shadow-sm">
      <h1 className="text-2xl font-bold tracking-tight text-stone-900">Page Not Found</h1>
      <p className="text-sm text-stone-500">The page you requested does not exist.</p>
      <Link to="/" className="inline-block rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-indigo-700">
        Go to Catalog
      </Link>
    </section>
  );
}
