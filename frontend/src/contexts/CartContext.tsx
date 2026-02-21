import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react';
import { storeApi } from '../api/storeApi';
import { useAuthContext } from './AuthContext';
import type { CartDto, OrderIdDto } from '../types/api';

interface CartContextValue {
  cart: CartDto | null;
  isLoading: boolean;
  errorMessage: string | null;
  totalItems: number;
  mergeNotice: string | null;
  setMergeNotice: (message: string | null) => void;
  refreshCart: () => Promise<void>;
  addProduct: (productId: number, quantity?: number) => Promise<void>;
  updateQuantity: (productId: number, quantity: number) => Promise<void>;
  removeOne: (productId: number) => Promise<void>;
  removeItemCompletely: (productId: number, quantity: number) => Promise<void>;
  clearCart: () => Promise<void>;
  checkout: () => Promise<OrderIdDto>;
}

const CartContext = createContext<CartContextValue | undefined>(undefined);

function toMessage(error: unknown): string {
  if (error instanceof Error) {
    return error.message;
  }
  return 'Something went wrong';
}

export function CartProvider({ children }: { children: ReactNode }) {
  const auth = useAuthContext();
  const [cart, setCart] = useState<CartDto | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [mergeNotice, setMergeNotice] = useState<string | null>(null);

  const refreshCart = useCallback(async (): Promise<void> => {
    setIsLoading(true);
    setErrorMessage(null);
    try {
      const currentCart = await storeApi.getCurrentCart();
      setCart(currentCart);
    } catch (error) {
      setErrorMessage(toMessage(error));
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    if (!auth.isInitializing) {
      void refreshCart();
    }
  }, [auth.isAuthenticated, auth.isInitializing, refreshCart]);

  const addProduct = useCallback(
    async (productId: number, quantity = 1): Promise<void> => {
      setIsLoading(true);
      setErrorMessage(null);
      try {
        for (let count = 0; count < quantity; count += 1) {
          await storeApi.addProductToCurrentCart(productId);
        }
        const updated = await storeApi.getCurrentCart();
        setCart(updated);
      } catch (error) {
        setErrorMessage(toMessage(error));
        throw error;
      } finally {
        setIsLoading(false);
      }
    },
    [],
  );

  const updateQuantity = useCallback(async (productId: number, quantity: number): Promise<void> => {
    setIsLoading(true);
    setErrorMessage(null);
    try {
      await storeApi.updateCartItem(productId, quantity);
      const updated = await storeApi.getCurrentCart();
      setCart(updated);
    } catch (error) {
      setErrorMessage(toMessage(error));
      throw error;
    } finally {
      setIsLoading(false);
    }
  }, []);

  const removeOne = useCallback(async (productId: number): Promise<void> => {
    setIsLoading(true);
    setErrorMessage(null);
    try {
      await storeApi.deleteCartItem(productId);
      const updated = await storeApi.getCurrentCart();
      setCart(updated);
    } catch (error) {
      setErrorMessage(toMessage(error));
      throw error;
    } finally {
      setIsLoading(false);
    }
  }, []);

  const removeItemCompletely = useCallback(
    async (productId: number, quantity: number): Promise<void> => {
      setIsLoading(true);
      setErrorMessage(null);
      try {
        for (let count = 0; count < quantity; count += 1) {
          await storeApi.deleteCartItem(productId);
        }
        const updated = await storeApi.getCurrentCart();
        setCart(updated);
      } catch (error) {
        setErrorMessage(toMessage(error));
        throw error;
      } finally {
        setIsLoading(false);
      }
    },
    [],
  );

  const clearCart = useCallback(async (): Promise<void> => {
    setIsLoading(true);
    setErrorMessage(null);
    try {
      await storeApi.clearCart();
      const updated = await storeApi.getCurrentCart();
      setCart(updated);
    } catch (error) {
      setErrorMessage(toMessage(error));
      throw error;
    } finally {
      setIsLoading(false);
    }
  }, []);

  const checkout = useCallback(async (): Promise<OrderIdDto> => {
    setIsLoading(true);
    setErrorMessage(null);
    try {
      const checkoutResult = await storeApi.checkout();
      return checkoutResult;
    } catch (error) {
      setErrorMessage(toMessage(error));
      throw error;
    } finally {
      setIsLoading(false);
    }
  }, []);

  const totalItems = useMemo(
    () => (cart?.items ?? []).reduce((sum, item) => sum + item.quantity, 0),
    [cart?.items],
  );

  const value = useMemo<CartContextValue>(
    () => ({
      cart,
      isLoading,
      errorMessage,
      totalItems,
      mergeNotice,
      setMergeNotice,
      refreshCart,
      addProduct,
      updateQuantity,
      removeOne,
      removeItemCompletely,
      clearCart,
      checkout,
    }),
    [
      addProduct,
      cart,
      checkout,
      clearCart,
      errorMessage,
      isLoading,
      mergeNotice,
      refreshCart,
      removeItemCompletely,
      removeOne,
      totalItems,
      updateQuantity,
    ],
  );

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
}

export function useCartContext(): CartContextValue {
  const context = useContext(CartContext);
  if (!context) {
    throw new Error('useCartContext must be used within CartProvider');
  }
  return context;
}
