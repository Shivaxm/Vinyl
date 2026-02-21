import { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { useCart } from '../hooks/useCart';

export function LoginPage() {
  const auth = useAuth();
  const cart = useCart();
  const navigate = useNavigate();
  const [params] = useSearchParams();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>): Promise<void> {
    event.preventDefault();
    setErrorMessage(null);
    setIsSubmitting(true);

    const guestCountBeforeLogin = cart.totalItems;

    try {
      await auth.login({ email, password });
      await cart.refreshCart();

      if (guestCountBeforeLogin > 0) {
        cart.setMergeNotice('Your guest cart items have been added to your account.');
      }

      const redirect = params.get('redirect') || '/';
      navigate(redirect, { replace: true });
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : 'Login failed');
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <section className="mx-auto max-w-md rounded-xl border border-stone-200 bg-white p-6 shadow-sm">
      <h1 className="text-2xl font-bold tracking-tight text-stone-900">Login</h1>
      <p className="mt-1 text-sm text-stone-500">Sign in to continue to checkout and order history.</p>

      <form onSubmit={handleSubmit} className="mt-5 space-y-4">
        <label className="block text-sm font-medium text-stone-700">
          Email
          <input
            type="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            className="mt-1 w-full rounded-lg border border-stone-300 px-3 py-2"
            required
          />
        </label>

        <label className="block text-sm font-medium text-stone-700">
          Password
          <input
            type="password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            className="mt-1 w-full rounded-lg border border-stone-300 px-3 py-2"
            required
          />
        </label>

        <button
          type="submit"
          disabled={isSubmitting}
          className="w-full rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-indigo-700 disabled:cursor-not-allowed disabled:bg-stone-300"
        >
          {isSubmitting ? 'Signing in...' : 'Login'}
        </button>
      </form>

      {errorMessage ? (
        <p className="mt-4 rounded-md border border-rose-200 bg-rose-50 px-3 py-2 text-sm text-rose-700">{errorMessage}</p>
      ) : null}

      <p className="mt-4 text-sm text-stone-500">
        Need an account?{' '}
        <Link to="/register" className="font-medium text-indigo-600 underline transition-colors hover:text-indigo-700">
          Register
        </Link>
      </p>
    </section>
  );
}
