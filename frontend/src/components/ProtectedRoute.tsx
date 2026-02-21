import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

export function ProtectedRoute() {
  const auth = useAuth();
  const location = useLocation();

  if (auth.isInitializing) {
    return <div className="rounded-md border border-slate-200 bg-white p-4">Checking session...</div>;
  }

  if (!auth.isAuthenticated) {
    const redirect = `${location.pathname}${location.search}`;
    return <Navigate to={`/login?redirect=${encodeURIComponent(redirect)}`} replace />;
  }

  return <Outlet />;
}
