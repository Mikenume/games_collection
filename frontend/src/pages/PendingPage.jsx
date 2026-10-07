// Pestaña PENDIENTES: juegos enviados desde la cuenta demo.
// La demo solo los ve (y busca), para no enviar uno que ya está esperando.
// El admin además puede aprobarlos (pasan al catálogo), editarlos (siguen
// pendientes) o descartarlos (se borran).

import { useCallback, useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  approveGame, deleteGame, fetchGame, fetchPendingGames, notifyPendingChanged,
} from '../api/games';
import { useAuth } from '../auth/AuthContext';
import { youtubeThumb, youtubeWatchUrl } from '../api/youtube';
import GameFormModal from '../components/GameFormModal';

const DATE_FORMAT = new Intl.DateTimeFormat('es-ES', {
  dateStyle: 'short',
  timeStyle: 'short',
  timeZone: 'Europe/Madrid',
});

export default function PendingPage() {
  const { isAdmin } = useAuth();
  const [games, setGames] = useState([]);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  // id del juego con una acción en marcha, para desactivar sus botones
  const [busyId, setBusyId] = useState(null);
  const [confirmDiscardId, setConfirmDiscardId] = useState(null);
  // Juego completo (con géneros y ediciones) que se está editando
  const [editing, setEditing] = useState(null);

  const load = useCallback(() => {
    setLoading(true);
    fetchPendingGames()
      .then((data) => {
        setGames(data);
        setError(null);
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  useEffect(load, [load]);

  // Son pocos, así que se filtran aquí sin volver a pedirlos a la API
  const visible = useMemo(() => {
    const term = search.trim().toLowerCase();
    if (!term) return games;
    return games.filter((g) => g.title.toLowerCase().includes(term));
  }, [games, search]);

  function afterChange() {
    notifyPendingChanged();
    load();
  }

  async function run(id, action) {
    setBusyId(id);
    setError(null);
    try {
      await action();
    } catch (err) {
      setError(err.message);
    } finally {
      setBusyId(null);
    }
  }

  function handleApprove(id) {
    run(id, async () => {
      await approveGame(id);
      afterChange();
    });
  }

  function handleEdit(id) {
    run(id, async () => setEditing(await fetchGame(id)));
  }

  function handleDiscard(id) {
    run(id, async () => {
      await deleteGame(id);
      setConfirmDiscardId(null);
      afterChange();
    });
  }

  return (
    <div>
      <div className="mb-3">
        <h1 className="display-face h3 mb-1">Pendientes</h1>
        <p className="mb-0">
          Videojuegos enviados desde la cuenta DEMO y pendientes de publicar en el catálogo
          tras la aprobación del administrador.
        </p>
      </div>

      <input
        type="search"
        className="form-control mb-3 pending-search"
        placeholder="Buscar por título"
        value={search}
        onChange={(e) => setSearch(e.target.value)}
        aria-label="Buscar pendientes por título"
      />

      {error && <div className="alert alert-danger">{error}</div>}

      {loading && games.length === 0 && <div className="notice">Cargando los pendientes…</div>}

      {!loading && games.length === 0 && !error && (
        <div className="notice">No hay juegos pendientes de publicar.</div>
      )}

      {!loading && games.length > 0 && visible.length === 0 && (
        <div className="notice">Ningún juego pendiente coincide con la búsqueda.</div>
      )}

      <div className="d-flex flex-column gap-3">
        {visible.map((game) => {
          const busy = busyId === game.id;
          return (
            <section key={game.id} className="rail">
              {/* Carátula, datos y trailer propuestos: lo necesario para
                  aprobar sin abrir el formulario */}
              <div className="pending-body">
                {game.coverUrl ? (
                  <img className="pending-cover" src={game.coverUrl} alt={`Carátula de ${game.title}`} loading="lazy" />
                ) : (
                  <div className="pending-cover pending-empty data-face">Sin carátula</div>
                )}

                <div className="pending-info">
                  {/* El detalle de un pendiente solo lo puede abrir el admin */}
                  <h2 className="display-face h5 mb-1">
                    {isAdmin ? (
                      <Link to={`/juegos/${game.id}`} className="text-reset text-decoration-none">
                        {game.title}
                      </Link>
                    ) : game.title}
                  </h2>
                  <p className="data-face mb-1">
                    {[game.releaseYear, game.developer, game.publisher].filter(Boolean).join(' · ') || 'Sin más datos'}
                  </p>
                  <p className="data-face mb-1">
                    {game.platforms.length > 0
                      ? game.platforms.join(' · ')
                      : isAdmin ? 'Sin plataforma: añádela al editar' : 'Sin plataforma'}
                    {game.genres.length > 0 && ` — ${game.genres.join(', ')}`}
                  </p>
                  <p className="data-face mb-1">
                    Formato: {game.formats.length > 0 ? game.formats.join(' · ') : 'sin indicar'}
                  </p>
                  <p className="data-face mb-0">
                    Enviado por {game.createdBy ?? 'un usuario borrado'} el{' '}
                    {DATE_FORMAT.format(new Date(game.createdAt))}
                  </p>

                  <p className="eyebrow mt-3 mb-1">Sinopsis</p>
                  <p className="pending-synopsis mb-0">
                    {game.synopsis || 'Sin sinopsis.'}
                  </p>
                </div>

                <div className="pending-trailer-box">
                  <p className="eyebrow mb-1">Trailer</p>
                  {game.trailerId ? (
                    <a
                      className="pending-trailer"
                      href={youtubeWatchUrl(game.trailerId)}
                      target="_blank"
                      rel="noopener noreferrer"
                      title="Ver el trailer en YouTube"
                    >
                      <img src={youtubeThumb(game.trailerId)} alt={`Trailer de ${game.title} en YouTube`} loading="lazy" />
                      <span className="pending-trailer-label">▶ YouTube</span>
                    </a>
                  ) : (
                    <div className="pending-trailer pending-empty data-face">Sin trailer</div>
                  )}

                  {isAdmin && (
                    <div className="d-flex flex-wrap gap-2 mt-3">
                      {confirmDiscardId === game.id ? (
                        <>
                          <button className="btn btn-sm btn-danger" disabled={busy}
                                  onClick={() => handleDiscard(game.id)}>
                            Confirmar descarte
                          </button>
                          <button className="btn btn-sm btn-outline-light" disabled={busy}
                                  onClick={() => setConfirmDiscardId(null)}>
                            Cancelar
                          </button>
                        </>
                      ) : (
                        <>
                          <button className="btn btn-sm btn-primary" disabled={busy}
                                  onClick={() => handleApprove(game.id)}>
                            Aprobar
                          </button>
                          <button className="btn btn-sm btn-outline-light" disabled={busy}
                                  onClick={() => handleEdit(game.id)}>
                            Editar
                          </button>
                          <button className="btn btn-sm btn-outline-danger" disabled={busy}
                                  onClick={() => setConfirmDiscardId(game.id)}>
                            Descartar
                          </button>
                        </>
                      )}
                    </div>
                  )}
                </div>
              </div>
            </section>
          );
        })}
      </div>

      {editing && (
        <GameFormModal
          game={editing}
          onClose={() => setEditing(null)}
          onSaved={() => {
            setEditing(null);
            load();
          }}
        />
      )}
    </div>
  );
}
