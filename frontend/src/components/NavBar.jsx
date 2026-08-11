import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

export default function NavBar() {
  const { isLoggedIn, isAdmin, user, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate('/');
  }

  return (
    <header className="topbar">
      <div className="container d-flex align-items-center justify-content-between py-3 gap-3">
        <Link to="/" className="wordmark">
          Central<span>·</span>Videogames
        </Link>

        <div className="d-flex align-items-center gap-3">
          {isLoggedIn ? (
            <>
              <span className="data-face d-none d-sm-inline">
                {user.username}
                {isAdmin && ' · admin'}
              </span>
              <button className="btn btn-sm btn-outline-light" onClick={handleLogout}>
                Salir
              </button>
            </>
          ) : (
            <Link to="/acceso" className="btn btn-sm btn-outline-light">
              Entrar
            </Link>
          )}
        </div>
      </div>
    </header>
  );
}
