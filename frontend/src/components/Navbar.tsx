import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { useCart } from '../hooks/useCart';

export function Navbar() {
  const auth = useAuth();
  const cart = useCart();
  const navigate = useNavigate();

  function handleLogout(): void {
    auth.logout();
    navigate('/');
  }

  const linkClassName = ({ isActive }: { isActive: boolean }): string =>
    isActive
      ? 'text-slate-900 font-semibold'
      : 'text-slate-600 hover:text-slate-900 transition-colors';

  return (
    <header className="border-b border-slate-200 bg-white/85 backdrop-blur">
      <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-4 sm:px-6 lg:px-8">
        <Link to="/" className="text-lg font-bold tracking-tight text-slate-900">
          Store Frontend
        </Link>

        <nav className="flex items-center gap-4 text-sm">
          <NavLink to="/" className={linkClassName}>
            Products
          </NavLink>
          <NavLink to="/cart" className={linkClassName}>
            Cart ({cart.totalItems})
          </NavLink>
          {auth.isAuthenticated ? (
            <>
              <NavLink to="/orders" className={linkClassName}>
                Orders
              </NavLink>
              <button
                type="button"
                onClick={handleLogout}
                className="rounded-md border border-slate-300 px-3 py-1.5 text-slate-700 hover:bg-slate-100"
              >
                Logout
              </button>
            </>
          ) : (
            <>
              <NavLink to="/login" className={linkClassName}>
                Login
              </NavLink>
              <NavLink to="/register" className={linkClassName}>
                Register
              </NavLink>
            </>
          )}
        </nav>
      </div>
    </header>
  );
}
