import { useEffect, useState } from 'react';
import { Link, NavLink, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { fetchPendingGames, onPendingChanged } from '../api/games';

export default function NavBar() {
  const { isLoggedIn, isAdmin, isDemo, user, logout, loginAsDemo } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [pendingCount, setPendingCount] = useState(0);
  const [enteringDemo, setEnteringDemo] = useState(false);

  // Contador de la pestaña PENDIENTES: se pide al cambiar de página y
  // cuando alguna pantalla avisa de que la lista ha cambiado
  const showTabs = isAdmin || isDemo;

  useEffect(() => {
    if (!showTabs) return undefined;

    function refresh() {
      fetchPendingGames()
        .then((games) => setPendingCount(games.length))
        .catch(() => setPendingCount(0));
    }

    refresh();
    return onPendingChanged(refresh);
  }, [showTabs, location.pathname]);

  async function handleLogout() {
    await logout();
    navigate('/');
  }

  async function handleDemo() {
    setEnteringDemo(true);
    try {
      await loginAsDemo();
      navigate('/');
    } catch (err) {
      navigate('/acceso', { state: { error: err.message } });
    } finally {
      setEnteringDemo(false);
    }
  }

  return (
    <header className="topbar">
      <div className="container d-flex align-items-center justify-content-between py-3 gap-3">
        <Link to="/" className="wordmark">
          Central<span>·</span>Videogames
        </Link>

        <div className="d-flex align-items-center gap-2 gap-sm-3">
          {isLoggedIn ? (
            <>
              <span className="data-face d-none d-sm-inline">
                {user.username}
                {isAdmin && ' · admin'}
                {isDemo && ' · demo'}
              </span>
              {isAdmin && (
                <Link to="/ajustes" className="btn btn-sm btn-outline-light">
                  Ajustes
                </Link>
              )}
              <button className="btn btn-sm btn-outline-light" onClick={handleLogout}>
                Salir
              </button>
            </>
          ) : (
            <>
              <button className="btn btn-sm btn-primary" onClick={handleDemo} disabled={enteringDemo}>
                {enteringDemo ? 'Entrando…' : 'Probar demo'}
              </button>
              <Link to="/acceso" className="btn btn-sm btn-outline-light">
                Entrar
              </Link>
            </>
          )}
        </div>
      </div>

      {showTabs && (
        <nav className="container d-flex gap-4 section-tabs" aria-label="Secciones">
          <NavLink to="/" end className="section-tab">
            Catálogo
          </NavLink>
          <NavLink to="/pendientes" className="section-tab">
            Pendientes
            {pendingCount > 0 && (
              <span className="badge rounded-pill text-bg-warning ms-2">{pendingCount}</span>
            )}
          </NavLink>
          {isAdmin && (
            <NavLink to="/coleccion" className="section-tab">
              Colección
            </NavLink>
          )}
        </nav>
      )}
    </header>
  );
}
