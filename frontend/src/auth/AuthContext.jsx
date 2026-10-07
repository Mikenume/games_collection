// Contexto de sesión: se declara en main.jsx y cualquier componente
// de debajo lo lee con useAuth(). La sesión real la lleva el backend
// (cookie de sesión); aquí se guarda lo que responde /api/auth/me para
// pintar la UI. Se pregunta al arrancar, tras el login y tras el logout.

import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api, setUnauthorizedHandler } from '../api/client';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  // { username, role } o null si no hay sesión
  const [user, setUser] = useState(null);
  // true hasta que /me responde la primera vez; mientras, no se sabe quién es
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  const loadUser = useCallback(async () => {
    try {
      setUser(await api.get('/api/auth/me'));
    } catch {
      setUser(null);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    // Antes el usuario se guardaba en localStorage; ya no hace falta
    try {
      localStorage.removeItem('cv_user');
    } catch {
      /* almacenamiento no disponible */
    }
    loadUser();
  }, [loadUser]);

  // El backend devuelve 401 cuando la sesión ha caducado; se detecta
  // aquí en vez de en cada página, y se manda a login con un aviso.
  useEffect(() => {
    setUnauthorizedHandler(() => {
      setUser(null);
      navigate('/acceso', { state: { expired: true } });
    });
    return () => setUnauthorizedHandler(null);
  }, [navigate]);

  const value = useMemo(() => {
    async function login(username, password) {
      await api.post('/api/auth/login', { username, password });
      await loadUser();
    }

    // Botón "Probar como demo": entra sin contraseña en la cuenta compartida
    async function loginAsDemo() {
      await api.post('/api/auth/demo');
      await loadUser();
    }

    async function logout() {
      try {
        await api.post('/api/auth/logout');
      } catch {
        /* aunque falle, se pregunta al backend quién queda */
      }
      await loadUser();
    }

    async function updateCredentials(username, currentPassword, newPassword) {
      const data = await api.put('/api/auth/me', { username, currentPassword, newPassword });
      setUser(data);
      return data;
    }

    return {
      user,
      loading,
      loadUser,
      login,
      loginAsDemo,
      logout,
      updateCredentials,
      isLoggedIn: Boolean(user),
      isAdmin: user?.role === 'ADMIN',
      isDemo: user?.role === 'DEMO',
    };
  }, [user, loading, loadUser]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth() se ha usado fuera de <AuthProvider>');
  }
  return context;
}
