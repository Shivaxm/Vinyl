import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

export function RegisterPage() {
  const auth = useAuth();
  const navigate = useNavigate();

  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>): Promise<void> {
    event.preventDefault();

    if (password !== confirmPassword) {
      setErrorMessage('Passwords do not match');
      return;
    }

    setErrorMessage(null);
    setIsSubmitting(true);

    try {
      await auth.register({ name, email, password });
      navigate('/login', { replace: true });
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : 'Registration failed');
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <section className="mx-auto max-w-md rounded-xl border border-stone-200 bg-white p-6 shadow-sm">
      <h1 className="text-2xl font-bold tracking-tight text-stone-900">Create Account</h1>
      <p className="mt-1 text-sm text-stone-500">Register to track orders and complete checkout.</p>

      <form onSubmit={handleSubmit} className="mt-5 space-y-4">
        <label className="block text-sm font-medium text-stone-700">
          Name
          <input
            type="text"
            value={name}
            onChange={(event) => setName(event.target.value)}
            className="mt-1 w-full rounded-lg border border-stone-300 px-3 py-2"
            required
          />
        </label>

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
            minLength={6}
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            className="mt-1 w-full rounded-lg border border-stone-300 px-3 py-2"
            required
          />
        </label>

        <label className="block text-sm font-medium text-stone-700">
          Confirm Password
          <input
            type="password"
            minLength={6}
            value={confirmPassword}
            onChange={(event) => setConfirmPassword(event.target.value)}
            className="mt-1 w-full rounded-lg border border-stone-300 px-3 py-2"
            required
          />
        </label>

        <button
          type="submit"
          disabled={isSubmitting}
          className="w-full rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-indigo-700 disabled:cursor-not-allowed disabled:bg-stone-300"
        >
          {isSubmitting ? 'Creating account...' : 'Register'}
        </button>
      </form>

      {errorMessage ? (
        <p className="mt-4 rounded-md border border-rose-200 bg-rose-50 px-3 py-2 text-sm text-rose-700">{errorMessage}</p>
      ) : null}

      <p className="mt-4 text-sm text-stone-500">
        Already have an account?{' '}
        <Link to="/login" className="font-medium text-indigo-600 underline transition-colors hover:text-indigo-700">
          Login
        </Link>
      </p>
    </section>
  );
}
