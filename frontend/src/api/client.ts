import type { ApiErrorBody } from '../types/api';

type GetAccessToken = () => string | null;
type RefreshAccessToken = () => Promise<string | null>;

interface RequestOptions extends Omit<RequestInit, 'body'> {
  body?: unknown;
  auth?: boolean;
  skipRefresh?: boolean;
}

export class ApiError extends Error {
  public readonly status: number;
  public readonly body: ApiErrorBody | null;

  constructor(status: number, message: string, body: ApiErrorBody | null = null) {
    super(message);
    this.status = status;
    this.body = body;
  }
}

const API_BASE_URL = import.meta.env.VITE_API_URL ?? '';

let getAccessToken: GetAccessToken = () => null;
let refreshAccessToken: RefreshAccessToken = async () => null;

export function configureApiAuth(getToken: GetAccessToken, refreshToken: RefreshAccessToken): void {
  getAccessToken = getToken;
  refreshAccessToken = refreshToken;
}

function buildUrl(path: string): string {
  if (/^https?:\/\//.test(path)) {
    return path;
  }

  if (API_BASE_URL.endsWith('/') && path.startsWith('/')) {
    return `${API_BASE_URL.slice(0, -1)}${path}`;
  }

  if (!API_BASE_URL.endsWith('/') && !path.startsWith('/')) {
    return `${API_BASE_URL}/${path}`;
  }

  return `${API_BASE_URL}${path}`;
}

function normalizeErrorMessage(status: number, body: ApiErrorBody | null): string {
  if (body?.error && typeof body.error === 'string') {
    return body.error;
  }

  if (body?.message && typeof body.message === 'string') {
    return body.message;
  }

  if (status === 401) {
    return 'Unauthorized';
  }

  if (status === 403) {
    return 'Forbidden';
  }

  if (status === 404) {
    return 'Not found';
  }

  return 'Request failed';
}

function isJsonResponse(response: Response): boolean {
  const contentType = response.headers.get('content-type');
  return contentType?.includes('application/json') ?? false;
}

async function parseResponseBody(response: Response): Promise<unknown> {
  if (response.status === 204) {
    return null;
  }

  if (isJsonResponse(response)) {
    return response.json();
  }

  const text = await response.text();
  return text.length === 0 ? null : { message: text };
}

async function requestInternal<T>(path: string, options: RequestOptions, hasRetried: boolean): Promise<T> {
  const {
    body,
    auth = true,
    skipRefresh = false,
    headers: requestHeaders,
    credentials,
    ...rest
  } = options;

  const headers = new Headers(requestHeaders);
  if (!headers.has('Accept')) {
    headers.set('Accept', 'application/json');
  }
  if (body !== undefined && !(body instanceof FormData)) {
    headers.set('Content-Type', 'application/json');
  }

  if (auth) {
    const token = getAccessToken();
    if (token) {
      headers.set('Authorization', `Bearer ${token}`);
    }
  }

  const response = await fetch(buildUrl(path), {
    ...rest,
    headers,
    body: body === undefined || body instanceof FormData ? (body as BodyInit | null | undefined) : JSON.stringify(body),
    credentials: credentials ?? 'include',
  });

  if (response.status === 401 && auth && !skipRefresh && !hasRetried) {
    const refreshedToken = await refreshAccessToken();
    if (refreshedToken) {
      return requestInternal<T>(path, options, true);
    }
  }

  if (!response.ok) {
    const parsed = (await parseResponseBody(response)) as ApiErrorBody | null;
    throw new ApiError(response.status, normalizeErrorMessage(response.status, parsed), parsed);
  }

  const parsed = (await parseResponseBody(response)) as T;
  return parsed;
}

export async function apiRequest<T>(path: string, options: RequestOptions = {}): Promise<T> {
  return requestInternal<T>(path, options, false);
}
