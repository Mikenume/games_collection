/* =========================================================
   Contexto de sesión.

   En PHP tenías $_SESSION: una variable global disponible en
   cualquier página. En React el equivalente es un "contexto":
   se declara arriba del todo (en main.jsx) y cualquier
   componente por debajo lo lee con useAuth().

   IMPORTANTE: esto sólo controla lo que se VE. Quien controla
   lo que se PUEDE HACER es el backend. Un usuario con las
   DevTools abiertas puede forzar isAdmin = true y le
   aparecerán los botones; cuando pulse, el backend le
   devolverá 403. La seguridad real está allí, siempre.
   ========================================================= */

import { createContext, useContext, useMemo, useState } from 'react';
import { api, setToken, getToken } from '../api/client';

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
  // Si hay token guardado de una visita anterior, recuperamos la sesión.
  const [user, setUser] = useState(() => (getToken() ? readStoredUser() : null));

  const value = useMemo(() => {
    async function login(username, password) {
      const data = await api.post('/api/auth/login', { username, password });
      // Se espera: { token: "ey...", username: "miguel", roles: ["ROLE_ADMIN"] }
      setToken(data.token);
      const stored = { username: data.username, roles: data.roles || [] };
      localStorage.setItem(USER_KEY, JSON.stringify(stored));
      setUser(stored);
      return stored;
    }

    function logout() {
      setToken(null);
      localStorage.removeItem(USER_KEY);
      setUser(null);
    }

    return {
      user,
      login,
      logout,
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
