export type ID = number;

export interface JwtResponse {
  token: string;
}

export interface UserRequest {
  email: string;
  password: string;
}

export interface RegisterUserRequest {
  name: string;
  email: string;
  password: string;
}

export interface UserDto {
  id: ID;
  name: string;
  email: string;
  createdAt: string;
}

export interface ProductDto {
  id: ID;
  name: string;
  description: string;
  price: number;
  categoryId: number;
}

export interface RegisterProductRequest {
  name: string;
  description: string;
  price: number;
  categoryId: number;
}

export interface CartProductDto {
  id: ID;
  name: string;
  price: number;
}

export interface CartItemDto {
  product: CartProductDto;
  quantity: number;
  totalPrice: number;
}

export interface CartDto {
  id: string;
  items: CartItemDto[];
  totalPrice: number;
}

export interface CartItemRequestDto {
  id: ID;
}

export interface UpdateCartItemRequest {
  quantity: number;
}

export type OrderStatus = 'PENDING' | 'PAID' | 'FAILED' | 'CANCELED';

export interface OrderItemDto {
  product: CartProductDto;
  quantity: number;
  totalPrice: number;
}

export interface OrderDto {
  id: ID;
  items: OrderItemDto[];
  status: OrderStatus;
  createdAt: string;
  totalPrice: number;
}

export interface OrderIdDto {
  orderId: ID;
  checkoutUrl: string;
}

export interface ApiErrorBody {
  error?: string;
  message?: string;
  [key: string]: unknown;
}
