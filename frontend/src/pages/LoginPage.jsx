import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();

  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);
  const [busy, setBusy] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setBusy(true);
    setError(null);
    try {
      await login(username, password);
      navigate('/');
    } catch (err) {
      // 401 desde el backend significa credenciales incorrectas.
      setError(
        err.status === 401
          ? 'Usuario o contraseña incorrectos.'
          : err.message
      );
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="row justify-content-center">
      <div className="col-md-6 col-lg-4">
        <div className="rail">
          <p className="eyebrow mb-1">Área privada</p>
          <h1 className="display-face h4 mb-4">Acceso</h1>

          <form onSubmit={handleSubmit}>
            {error && <div className="alert alert-danger py-2">{error}</div>}

            <div className="mb-3">
              <label className="form-label" htmlFor="user">Usuario</label>
              <input
                id="user"
                className="form-control"
                autoComplete="username"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                required
              />
            </div>

            <div className="mb-4">
              <label className="form-label" htmlFor="pass">Contraseña</label>
              <input
                id="pass"
                type="password"
                className="form-control"
                autoComplete="current-password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>

            <button className="btn btn-primary w-100" disabled={busy}>
              {busy ? 'Entrando…' : 'Entrar'}
            </button>
          </form>

          <p className="data-face mt-4 mb-0">
            La consulta del catálogo es pública. Sólo hace falta entrar para crear, editar o borrar.
          </p>
        </div>
      </div>
    </div>
  );
}
