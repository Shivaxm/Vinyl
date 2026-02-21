import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
  type ReactNode,
} from 'react';
import { configureApiAuth } from '../api/client';
import { storeApi } from '../api/storeApi';
import type { RegisterUserRequest, UserDto, UserRequest } from '../types/api';

interface AuthContextValue {
  user: UserDto | null;
  accessToken: string | null;
  isAuthenticated: boolean;
  isInitializing: boolean;
  login: (payload: UserRequest) => Promise<void>;
  register: (payload: RegisterUserRequest) => Promise<void>;
  logout: () => void;
  refreshAccessToken: () => Promise<string | null>;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserDto | null>(null);
  const [accessToken, setAccessToken] = useState<string | null>(null);
  const [isInitializing, setIsInitializing] = useState(true);
  const accessTokenRef = useRef<string | null>(null);

  useEffect(() => {
    accessTokenRef.current = accessToken;
  }, [accessToken]);

  const fetchCurrentUser = useCallback(async (): Promise<UserDto | null> => {
    try {
      const currentUser = await storeApi.getCurrentUser();
      setUser(currentUser);
      return currentUser;
    } catch {
      setUser(null);
      return null;
    }
  }, []);

  const refreshAccessToken = useCallback(async (): Promise<string | null> => {
    try {
      const jwt = await storeApi.refreshToken();
      setAccessToken(jwt.token);
      accessTokenRef.current = jwt.token;
      return jwt.token;
    } catch {
      setAccessToken(null);
      accessTokenRef.current = null;
      setUser(null);
      return null;
    }
  }, []);

  useEffect(() => {
    configureApiAuth(() => accessTokenRef.current, refreshAccessToken);
  }, [refreshAccessToken]);

  useEffect(() => {
    let mounted = true;

    async function initializeAuth(): Promise<void> {
      const token = await refreshAccessToken();
      if (token) {
        await fetchCurrentUser();
      }
      if (mounted) {
        setIsInitializing(false);
      }
    }

    initializeAuth();

    return () => {
      mounted = false;
    };
  }, [fetchCurrentUser, refreshAccessToken]);

  const login = useCallback(
    async (payload: UserRequest): Promise<void> => {
      const jwt = await storeApi.login(payload);
      setAccessToken(jwt.token);
      accessTokenRef.current = jwt.token;
      await fetchCurrentUser();
    },
    [fetchCurrentUser],
  );

  const register = useCallback(async (payload: RegisterUserRequest): Promise<void> => {
    await storeApi.register(payload);
  }, []);

  const logout = useCallback(() => {
    setAccessToken(null);
    accessTokenRef.current = null;
    setUser(null);
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      accessToken,
      isAuthenticated: Boolean(accessToken),
      isInitializing,
      login,
      register,
      logout,
      refreshAccessToken,
    }),
    [accessToken, isInitializing, login, logout, refreshAccessToken, register, user],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuthContext(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuthContext must be used within AuthProvider');
  }
  return context;
}
