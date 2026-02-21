import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { useCart } from '../hooks/useCart';
import { formatCurrency } from '../utils/format';

export function CartPage() {
  const auth = useAuth();
  const cart = useCart();
  const navigate = useNavigate();
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  async function handleCheckout(): Promise<void> {
    setErrorMessage(null);

    if (!auth.isAuthenticated) {
      navigate('/login?redirect=/cart');
      return;
    }

    try {
      const result = await cart.checkout();
      window.location.assign(result.checkoutUrl);
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : 'Checkout failed');
    }
  }

  if (!cart.cart || cart.cart.items.length === 0) {
    return (
      <section className="space-y-4 rounded-xl border border-stone-200 bg-white p-6 shadow-sm">
        <h1 className="text-2xl font-bold tracking-tight text-stone-900">Your Cart</h1>
        <p className="text-stone-500">Your cart is empty. Add products from the catalog to continue.</p>
      </section>
    );
  }

  return (
    <section className="space-y-4">
      <h1 className="text-2xl font-bold tracking-tight text-stone-900">Your Cart</h1>

      <div className="overflow-hidden rounded-xl border border-stone-200 bg-white shadow-sm">
        <table className="min-w-full divide-y divide-stone-200">
          <thead className="bg-stone-50 text-left text-xs uppercase tracking-wide text-stone-500">
            <tr>
              <th className="px-4 py-3">Product</th>
              <th className="px-4 py-3">Price</th>
              <th className="px-4 py-3">Quantity</th>
              <th className="px-4 py-3">Subtotal</th>
              <th className="px-4 py-3">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-stone-100 text-sm">
            {cart.cart.items.map((item) => (
              <tr key={item.product.id}>
                <td className="px-4 py-3 font-medium text-stone-800">{item.product.name}</td>
                <td className="px-4 py-3">{formatCurrency(item.product.price)}</td>
                <td className="px-4 py-3">
                  <select
                    value={item.quantity}
                    onChange={(event) => cart.updateQuantity(item.product.id, Number(event.target.value))}
                    className="rounded-lg border border-stone-300 px-2 py-1"
                    disabled={cart.isLoading}
                  >
                    {[1, 2, 3, 4, 5].map((value) => (
                      <option key={value} value={value}>
                        {value}
                      </option>
                    ))}
                  </select>
                </td>
                <td className="px-4 py-3">{formatCurrency(item.totalPrice)}</td>
                <td className="px-4 py-3">
                  <div className="flex flex-wrap gap-2">
                    <button
                      type="button"
                      onClick={() => cart.removeOne(item.product.id)}
                      disabled={cart.isLoading}
                      className="rounded-lg border border-stone-300 px-2 py-1 text-xs text-stone-700 transition-colors hover:bg-stone-100 disabled:cursor-not-allowed"
                    >
                      -1
                    </button>
                    <button
                      type="button"
                      onClick={() => cart.removeItemCompletely(item.product.id, item.quantity)}
                      disabled={cart.isLoading}
                      className="rounded-md border border-rose-300 px-2 py-1 text-xs text-rose-700 hover:bg-rose-50 disabled:cursor-not-allowed"
                    >
                      Remove
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="flex flex-col items-start justify-between gap-3 rounded-xl border border-stone-200 bg-white p-4 shadow-sm sm:flex-row sm:items-center">
        <div>
          <p className="text-sm text-stone-500">Cart total</p>
          <p className="text-2xl font-bold text-stone-900">{formatCurrency(cart.cart.totalPrice)}</p>
        </div>
        <div className="flex gap-2">
          <button
            type="button"
            onClick={() => cart.clearCart()}
            disabled={cart.isLoading}
            className="rounded-lg border border-stone-300 px-3 py-2 text-sm text-stone-700 transition-colors hover:bg-stone-100 disabled:cursor-not-allowed"
          >
            Clear cart
          </button>
          <button
            type="button"
            onClick={handleCheckout}
            disabled={cart.isLoading}
            className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-indigo-700 disabled:cursor-not-allowed disabled:bg-stone-300"
          >
            Proceed to Checkout
          </button>
        </div>
      </div>

      {!auth.isAuthenticated ? (
        <p className="text-sm text-stone-500">You can build a cart as guest. Login is required before checkout.</p>
      ) : null}

      {errorMessage ? (
        <div className="rounded-md border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700">{errorMessage}</div>
      ) : null}
      {cart.errorMessage ? (
        <div className="rounded-md border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700">{cart.errorMessage}</div>
      ) : null}
    </section>
  );
}
