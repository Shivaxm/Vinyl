import { apiRequest } from './client';
import type {
  CartDto,
  CartItemDto,
  JwtResponse,
  OrderDto,
  OrderIdDto,
  ProductDto,
  RegisterUserRequest,
  UpdateCartItemRequest,
  UserDto,
  UserRequest,
} from '../types/api';

export const storeApi = {
  getProducts(categoryId?: number): Promise<ProductDto[]> {
    const query = categoryId ? `?categoryId=${categoryId}` : '';
    return apiRequest<ProductDto[]>(`/products${query}`, { method: 'GET', auth: false });
  },

  getProduct(id: number): Promise<ProductDto> {
    return apiRequest<ProductDto>(`/products/${id}`, { method: 'GET', auth: false });
  },

  register(payload: RegisterUserRequest): Promise<UserDto> {
    return apiRequest<UserDto>('/users', {
      method: 'POST',
      body: payload,
      auth: false,
    });
  },

  login(payload: UserRequest): Promise<JwtResponse> {
    return apiRequest<JwtResponse>('/auth/login', {
      method: 'POST',
      body: payload,
      auth: false,
      skipRefresh: true,
    });
  },

  refreshToken(): Promise<JwtResponse> {
    return apiRequest<JwtResponse>('/auth/refresh', {
      method: 'POST',
      auth: false,
      skipRefresh: true,
    });
  },

  getCurrentUser(): Promise<UserDto> {
    return apiRequest<UserDto>('/auth/me', { method: 'GET' });
  },

  getCurrentCart(): Promise<CartDto> {
    return apiRequest<CartDto>('/carts/current', { method: 'GET' });
  },

  addProductToCurrentCart(productId: number): Promise<CartItemDto> {
    return apiRequest<CartItemDto>('/carts/current/items', {
      method: 'POST',
      body: { id: productId },
    });
  },

  updateCartItem(productId: number, quantity: number): Promise<CartItemDto> {
    const payload: UpdateCartItemRequest = { quantity };
    return apiRequest<CartItemDto>(`/carts/current/items/${productId}`, {
      method: 'PUT',
      body: payload,
    });
  },

  deleteCartItem(productId: number): Promise<void> {
    return apiRequest<void>(`/carts/current/items/${productId}`, {
      method: 'DELETE',
    });
  },

  clearCart(): Promise<void> {
    return apiRequest<void>('/carts/current/items', {
      method: 'DELETE',
    });
  },

  checkout(): Promise<OrderIdDto> {
    return apiRequest<OrderIdDto>('/checkout', {
      method: 'POST',
    });
  },

  getOrders(): Promise<OrderDto[]> {
    return apiRequest<OrderDto[]>('/orders', {
      method: 'GET',
    });
  },

  getOrder(orderId: number): Promise<OrderDto> {
    return apiRequest<OrderDto>(`/orders/${orderId}`, {
      method: 'GET',
    });
  },
};
