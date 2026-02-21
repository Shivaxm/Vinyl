import { Navigate, Route, Routes } from 'react-router-dom';
import { Layout } from './components/Layout';
import { ProtectedRoute } from './components/ProtectedRoute';
import { CartPage } from './pages/CartPage';
import { CheckoutCancelPage } from './pages/CheckoutCancelPage';
import { HomePage } from './pages/HomePage';
import { LoginPage } from './pages/LoginPage';
import { NotFoundPage } from './pages/NotFoundPage';
import { OrderSuccessPage } from './pages/OrderSuccessPage';
import { OrdersPage } from './pages/OrdersPage';
import { ProductDetailPage } from './pages/ProductDetailPage';
import { RegisterPage } from './pages/RegisterPage';

export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route path="/" element={<HomePage />} />
        <Route path="/products/:id" element={<ProductDetailPage />} />
        <Route path="/cart" element={<CartPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/checkout-success" element={<OrderSuccessPage />} />
        <Route path="/orders/success" element={<OrderSuccessPage />} />
        <Route path="/checkout-cancel" element={<CheckoutCancelPage />} />

        <Route element={<ProtectedRoute />}>
          <Route path="/orders" element={<OrdersPage />} />
        </Route>

        <Route path="*" element={<NotFoundPage />} />
      </Route>

      <Route path="" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
