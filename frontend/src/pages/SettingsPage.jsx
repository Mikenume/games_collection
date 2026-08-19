import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

export default function SettingsPage() {
  const { user, isAdmin, updateCredentials } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (!isAdmin) navigate('/');
  }, [isAdmin, navigate]);

  const [username, setUsername] = useState(user?.username ?? '');
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);
  const [busy, setBusy] = useState(false);

  if (!isAdmin) return null;

  async function handleSubmit(event) {
    event.preventDefault();
    setError(null);
    setSuccess(false);

    if (newPassword && newPassword !== confirmPassword) {
      setError('Las contraseñas nuevas no coinciden.');
      return;
    }

    setBusy(true);
    try {
      await updateCredentials(username, currentPassword, newPassword || null);
      setSuccess(true);
      setCurrentPassword('');
      setNewPassword('');
      setConfirmPassword('');
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="row justify-content-center">
      <div className="col-md-6 col-lg-4">
        <div className="rail">
          <p className="eyebrow mb-1">Área privada</p>
          <h1 className="display-face h4 mb-4">Ajustes de la cuenta</h1>

          <form onSubmit={handleSubmit}>
            {error && <div className="alert alert-danger py-2">{error}</div>}
            {success && <div className="alert alert-success py-2">Cambios guardados.</div>}

            <div className="mb-3">
              <label className="form-label" htmlFor="s-user">Usuario</label>
              <input
                id="s-user"
                className="form-control"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                required
              />
            </div>

            <div className="mb-3">
              <label className="form-label" htmlFor="s-current">Contraseña actual</label>
              <input
                id="s-current"
                type="password"
                className="form-control"
                autoComplete="current-password"
                value={currentPassword}
                onChange={(e) => setCurrentPassword(e.target.value)}
                required
              />
            </div>

            <div className="mb-3">
              <label className="form-label" htmlFor="s-new">Contraseña nueva</label>
              <input
                id="s-new"
                type="password"
                className="form-control"
                autoComplete="new-password"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                placeholder="Déjalo en blanco para no cambiarla"
              />
            </div>

            <div className="mb-4">
              <label className="form-label" htmlFor="s-confirm">Repite la contraseña nueva</label>
              <input
                id="s-confirm"
                type="password"
                className="form-control"
                autoComplete="new-password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                disabled={!newPassword}
              />
            </div>

            <button className="btn btn-primary w-100" disabled={busy}>
              {busy ? 'Guardando…' : 'Guardar cambios'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}
