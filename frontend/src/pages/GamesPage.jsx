import { useState, useEffect, useMemo } from 'react';
import { fetchGames, toNames, platformCodes, platformName, shellColor } from '../api/games';
import { useAuth } from '../auth/AuthContext';
import GameFlipCard from '../components/GameFlipCard';
import Filters from '../components/Filters';
import GameFormModal from '../components/GameFormModal';

const EMPTY_FILTERS = {
  search: '',
  platform: '',
  genre: '',
  sort: 'estanteria',
  onlyOwned: false,
};

// Catálogo: todos los juegos aprobados, los tenga o no.
// Colección (collection, solo admin): solo los que tengo.
export default function GamesPage({ collection = false }) {
  const { isAdmin, isDemo } = useAuth();

  const [filters, setFilters] = useState(EMPTY_FILTERS);
  const [games, setGames] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showForm, setShowForm] = useState(false);
  // Aviso tras enviar un juego como demo (no sale en el catálogo hasta aprobarlo)
  const [notice, setNotice] = useState(null);
  const [reloadKey, setReloadKey] = useState(0);

  // Búsqueda por título contra el backend, esperando un poco desde la
  // última tecla para no disparar una petición por cada letra.
  const [debouncedSearch, setDebouncedSearch] = useState('');

  useEffect(() => {
    const timer = setTimeout(() => setDebouncedSearch(filters.search), 300);
    return () => clearTimeout(timer);
  }, [filters.search]);

  useEffect(() => {
    let cancelled = false;

    setLoading(true);
    setError(null);

    fetchGames(debouncedSearch)
      .then((data) => {
        if (!cancelled) setGames(Array.isArray(data) ? data : []);
      })
      .catch((err) => {
        if (!cancelled) setError(err.message);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => { cancelled = true; };
  }, [debouncedSearch, reloadKey]);

  // En COLECCIÓN se parte solo de los que tengo; en el catálogo, de todos
  const baseGames = useMemo(
    () => (collection ? games.filter((g) => g.owned) : games),
    [games, collection],
  );

  // Opciones de los desplegables sacadas de los propios datos, así solo
  // aparecen las consolas y géneros que hay realmente en la lista.
  const platforms = useMemo(() => {
    const set = new Set();
    baseGames.forEach((g) => platformCodes(g).forEach((c) => c && set.add(c)));
    return [...set].sort();
  }, [baseGames]);

  const genres = useMemo(() => {
    const set = new Set();
    baseGames.forEach((g) => toNames(g.genres).forEach((n) => set.add(n)));
    return [...set].sort((a, b) => a.localeCompare(b, 'es'));
  }, [baseGames]);

  const visible = useMemo(() => {
    let result = baseGames;

    if (filters.platform) {
      result = result.filter((g) => platformCodes(g).includes(filters.platform));
    }
    if (filters.genre) {
      result = result.filter((g) => toNames(g.genres).includes(filters.genre));
    }
    if (filters.onlyOwned) {
      result = result.filter((g) => g.owned);
    }

    // 'estanteria' es el orden por defecto: consola, año de la consola y
    // título alfabético, tal y como lo devuelve ya la API. No se reordena.
    const sorted = [...result];
    if (filters.sort === 'title') {
      sorted.sort((a, b) => a.title.localeCompare(b.title, 'es'));
    } else if (filters.sort === 'year-asc') {
      sorted.sort((a, b) => (a.releaseYear ?? 0) - (b.releaseYear ?? 0));
    } else if (filters.sort === 'year-desc') {
      sorted.sort((a, b) => (b.releaseYear ?? 0) - (a.releaseYear ?? 0));
    }
    return sorted;
  }, [baseGames, filters]);

  // En el orden de estantería los juegos ya llegan agrupados por consola:
  // se parten en tramos consecutivos, cada uno con su encabezado. Con
  // cualquier otro orden las consolas se mezclan, así que va todo junto.
  const shelves = useMemo(() => {
    if (filters.sort !== 'estanteria') return [{ code: null, games: visible }];

    const groups = [];
    visible.forEach((game) => {
      const code = platformCodes(game)[0] || '—';
      const last = groups[groups.length - 1];
      if (last && last.code === code) last.games.push(game);
      else groups.push({ code, games: [game] });
    });
    return groups;
  }, [visible, filters.sort]);

  return (
    <div className="row g-4">
      <div className="col-lg-3">
        <Filters
          value={filters}
          onChange={setFilters}
          platforms={platforms}
          genres={genres}
          total={baseGames.length}
          shown={visible.length}
          showOwned={!collection}
        />
      </div>

      <div className="col-lg-9">
        <div className="d-flex align-items-center justify-content-between mb-3">
          <h1 className="display-face h3 mb-0">{collection ? 'Colección' : 'Catálogo'}</h1>

          {/* La cuenta demo también puede añadir: sus juegos quedan pendientes */}
          {(isAdmin || isDemo) && (
            <button className="btn btn-primary btn-sm" onClick={() => { setNotice(null); setShowForm(true); }}>
              Añadir juego
            </button>
          )}
        </div>

        {notice && (
          <div className="alert alert-success alert-dismissible" role="status">
            {notice}
            <button type="button" className="btn-close" aria-label="Cerrar" onClick={() => setNotice(null)} />
          </div>
        )}

        {loading && <div className="notice">{collection ? 'Cargando la colección…' : 'Cargando el catálogo…'}</div>}

        {error && !loading && (
          <div className="notice">
            <p className="mb-2">{error}</p>
            <p className="data-face mb-0">Revisa que la API responda en {import.meta.env.VITE_API_URL}</p>
          </div>
        )}

        {!loading && !error && visible.length === 0 && (
          <div className="notice">
            No hay ningún juego que encaje con estos filtros. Prueba a quitar alguno.
          </div>
        )}

        {!loading && !error && visible.length > 0 && shelves.map((shelf, index) => (
          <section key={`${shelf.code ?? 'todos'}-${index}`} className="shelf">
            {shelf.code && (
              <h2 className="shelf-heading" style={{ '--shell': shellColor(shelf.code) }}>
                {platformName(shelf.code)}
              </h2>
            )}

            <div className="row g-3">
              {shelf.games.map((game) => (
                <div className="col-6 col-md-4 col-lg-3 rise" key={game.id}>
                  <GameFlipCard game={game} />
                </div>
              ))}
            </div>
          </section>
        ))}
      </div>

      {showForm && (
        <GameFormModal
          onClose={() => setShowForm(false)}
          onSaved={(saved) => {
            setShowForm(false);
            if (isDemo) {
              setNotice(`«${saved.title}» enviado. Queda pendiente de publicar hasta que el administrador lo revise.`);
            } else {
              setReloadKey((k) => k + 1); // vuelve a pedir la lista a la API
            }
          }}
        />
      )}
    </div>
  );
}
