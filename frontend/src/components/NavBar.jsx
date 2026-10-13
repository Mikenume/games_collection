import { useEffect, useState } from 'react';
import { Link, NavLink, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { fetchPendingGames, onPendingChanged } from '../api/games';
import InfoTip from './InfoTip';

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
              {/* En el móvil no hay sitio para el usuario; al invitado se le deja
                  "invitado" y el (i) que explica qué puede hacer la cuenta */}
              <span className={`data-face ${isDemo ? 'd-inline-flex align-items-center gap-1' : 'd-none d-sm-inline'}`}>
                <span className={isDemo ? 'd-none d-sm-inline' : undefined}>
                  {user.username}
                  {isAdmin && ' · admin'}
                  {isDemo && ' ·'}
                </span>
                {isDemo && (
                  <>
                    invitado
                    <InfoTip label="Qué puede hacer el usuario invitado">
                      <span className="d-block mb-2">
                        Como usuario invitado puedes añadir juegos, que aparecerán en el catálogo cuando el administrador los valide.
                      </span>
                      <span className="d-block">
                        Puedes consultar los juegos pendientes de aprobación en la pestaña "PENDIENTES".
                      </span>
                    </InfoTip>
                  </>
                )}
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
                {enteringDemo ? 'Entrando…' : 'Invitado'}
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
