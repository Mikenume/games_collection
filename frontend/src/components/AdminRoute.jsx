// Envuelve las páginas que solo puede ver el admin (o también la demo, con
// allowDemo). Si no tiene permiso, vuelve al catálogo. Mientras
// /api/auth/me no ha respondido no se sabe quién es, así que se espera
// en vez de redirigir antes de tiempo.

import { Navigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

export default function AdminRoute({ children, allowDemo = false }) {
  const { isAdmin, isDemo, loading } = useAuth();

  if (loading) return <div className="notice">Comprobando la sesión…</div>;
  if (!isAdmin && !(allowDemo && isDemo)) return <Navigate to="/" replace />;
  return children;
}
