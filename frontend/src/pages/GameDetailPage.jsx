import { useState, useEffect, useRef } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { fetchGame, deleteGame, toNames, platformCodes, shellColor, coverImage } from '../api/games';
import { useAuth } from '../auth/AuthContext';
import GameFormModal from '../components/GameFormModal';
import GameTrailer from '../components/GameTrailer';

export default function GameDetailPage() {
  const { id } = useParams();          // lee el :id de la URL /juegos/:id
  const navigate = useNavigate();
  const { isAdmin } = useAuth();

  const [game, setGame] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [editing, setEditing] = useState(false);
  const [confirmDelete, setConfirmDelete] = useState(false);

  function load() {
    setLoading(true);
    setError(null);
    fetchGame(id)
      .then(setGame)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }

  useEffect(load, [id]);

  // La carátula mide lo mismo que la barra del título, que depende de
  // cuántas líneas ocupe el texto: se mide y se sigue si cambia.
  const heroRef = useRef(null);
  const [heroHeight, setHeroHeight] = useState(0);

  useEffect(() => {
    const hero = heroRef.current;
    if (!hero) return undefined;
    const observer = new ResizeObserver(() => setHeroHeight(hero.offsetHeight));
    observer.observe(hero);
    return () => observer.disconnect();
  }, [game, loading]);

  async function handleDelete() {
    try {
      await deleteGame(id);
      navigate('/');
    } catch (err) {
      setError(err.message);
      setConfirmDelete(false);
    }
  }

  if (loading) return <div className="notice">Cargando el juego…</div>;

  if (error) {
    return (
      <div className="notice">
        <p className="mb-3">{error}</p>
        <Link to="/" className="btn btn-outline-light btn-sm">Volver al catálogo</Link>
      </div>
    );
  }

  if (!game) return null;

  const codes = platformCodes(game);
  const genres = toNames(game.genres);
  const cover = game.coverUrl || coverImage(game);

  return (
    <article style={{ '--shell': shellColor(codes[0] || '') }}>
      <Link to="/" className="eyebrow text-decoration-none d-inline-block mb-4">
        ← Catálogo
      </Link>

      {/* Dos columnas independientes: a la izquierda el juego (cabecera,
          sinopsis, notas, géneros); a la derecha el trailer y, para el
          admin, las ediciones. Lo que mida una no empuja a la otra. */}
      <div className="row g-4">
        <div className="col-lg-7">
          <div className="detail-header mb-4">
            {cover && (
              <img className="detail-cover" src={cover} alt={`Carátula de ${game.title}`} style={{ height: heroHeight || undefined }} />
            )}

            <div className="detail-hero" ref={heroRef}>
              <p className="eyebrow mb-1">
                {[codes.join(' · ') || 'Sin plataforma', game.releaseYear].filter(Boolean).join(' · ')}
              </p>
              <h1 className="display-face mb-2">{game.title}</h1>
              {game.developer && <p className="data-face mb-0">Desarrolla: {game.developer}</p>}
              {game.publisher && <p className="data-face mb-0">Distribuye: {game.publisher}</p>}
              {game.editionType && <p className="data-face mb-0">Versión: {game.editionType}</p>}
            </div>
          </div>

          {isAdmin && (
            <div className="d-flex gap-2 mb-4">
              <button className="btn btn-sm btn-primary" onClick={() => setEditing(true)}>
                Editar
              </button>
              {confirmDelete ? (
                <>
                  <button className="btn btn-sm btn-danger" onClick={handleDelete}>
                    Confirmar borrado
                  </button>
                  <button className="btn btn-sm btn-outline-light" onClick={() => setConfirmDelete(false)}>
                    Cancelar
                  </button>
                </>
              ) : (
                <button className="btn btn-sm btn-outline-danger" onClick={() => setConfirmDelete(true)}>
                  Borrar
                </button>
              )}
            </div>
          )}

          <p className="eyebrow">Sinopsis</p>
          <p style={{ lineHeight: 1.7 }}>
            {game.synopsis || 'Todavía no hay sinopsis para este juego.'}
          </p>

          {game.notes && (
            <>
              <p className="eyebrow mt-4">Notas</p>
              <p>{game.notes}</p>
            </>
          )}

          <p className="eyebrow mt-4">Géneros</p>
          <div>
            {genres.length > 0
              ? genres.map((g) => <span key={g} className="tag">{g}</span>)
              : <span className="data-face">Sin géneros asignados.</span>}
          </div>
        </div>

        {(game.trailerId || isAdmin) && (
          <div className="col-lg-5">
            {game.trailerId && (
              <div className="mb-4">
                <p className="eyebrow">Trailer</p>
                <GameTrailer youtubeId={game.trailerId} title={game.title} />
              </div>
            )}

            {/* Lo que tengo y lo que no es cosa mía: solo lo ve el admin */}
            {isAdmin && (
              <>
                <p className="eyebrow">Ediciones en la colección</p>

                {(game.editions ?? []).map((edition) => (
                  <div
                    key={edition.id}
                    className="edition-row"
                    style={{ '--shell': shellColor(edition.platformAbbreviation) }}
                  >
                    <div className="d-flex justify-content-between align-items-start gap-2">
                      <strong className="display-face">{edition.platformName}</strong>
                      <span className={`tag ${edition.owned ? 'tag-owned' : 'tag-missing'}`}>
                        {edition.owned ? 'en propiedad' : 'no la tengo'}
                      </span>
                    </div>
                    <p className="data-face mb-0 mt-1">
                      {[edition.releaseYear, edition.region, edition.format].filter(Boolean).join(' · ')}
                    </p>
                    {edition.portDeveloper && (
                      <p className="data-face mb-0">Conversión: {edition.portDeveloper}</p>
                    )}
                    {edition.notes && <p className="data-face mb-0">{edition.notes}</p>}
                  </div>
                ))}

                {(game.editions ?? []).length === 0 && (
                  <p className="data-face">No hay ediciones registradas.</p>
                )}
              </>
            )}
          </div>
        )}
      </div>

      {editing && (
        <GameFormModal
          game={game}
          onClose={() => setEditing(false)}
          onSaved={() => { setEditing(false); load(); }}
        />
      )}
    </article>
  );
}
