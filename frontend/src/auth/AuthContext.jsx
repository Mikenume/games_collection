// Contexto de sesión: se declara en main.jsx y cualquier componente
// de debajo lo lee con useAuth(). La sesión real la lleva el backend
// (cookie de sesión); aquí solo se guarda el usuario para pintar la UI.

import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api, setUnauthorizedHandler } from '../api/client';

const AuthContext = createContext(null);

const USER_KEY = 'cv_user';

function readStoredUser() {
  try {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(readStoredUser);
  const navigate = useNavigate();

  // El backend devuelve 401/403 cuando la sesión ha caducado; se detecta
  // aquí en vez de en cada página, y se manda a login con un aviso.
  useEffect(() => {
    setUnauthorizedHandler(() => {
      localStorage.removeItem(USER_KEY);
      setUser(null);
      navigate('/acceso', { state: { expired: true } });
    });
    return () => setUnauthorizedHandler(null);
  }, [navigate]);

  const value = useMemo(() => {
    async function login(username, password) {
      const data = await api.post('/api/auth/login', { username, password });
      const stored = { username: data.username, roles: data.roles || [] };
      localStorage.setItem(USER_KEY, JSON.stringify(stored));
      setUser(stored);
      return stored;
    }

    function logout() {
      localStorage.removeItem(USER_KEY);
      setUser(null);
    }

    async function updateCredentials(username, currentPassword, newPassword) {
      const data = await api.put('/api/auth/me', { username, currentPassword, newPassword });
      const stored = { username: data.username, roles: data.roles || [] };
      localStorage.setItem(USER_KEY, JSON.stringify(stored));
      setUser(stored);
      return stored;
    }

    return {
      user,
      login,
      logout,
      updateCredentials,
      isLoggedIn: Boolean(user),
      isAdmin: Boolean(user?.roles?.includes('ROLE_ADMIN')),
    };
  }, [user]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth() se ha usado fuera de <AuthProvider>');
  }
  return context;
}
