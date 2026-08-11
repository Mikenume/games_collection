import { useState, useEffect, useMemo } from 'react';
import { fetchGames, toNames, platformCodes } from '../api/games';
import { useAuth } from '../auth/AuthContext';
import GameCase from '../components/GameCase';
import Filters from '../components/Filters';
import GameFormModal from '../components/GameFormModal';

const EMPTY_FILTERS = {
  search: '',
  platform: '',
  genre: '',
  sort: 'title',
  onlyOwned: false,
};

export default function GamesPage() {
  const { isAdmin } = useAuth();

  const [filters, setFilters] = useState(EMPTY_FILTERS);
  const [games, setGames] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showForm, setShowForm] = useState(false);
  const [reloadKey, setReloadKey] = useState(0); // cambiarlo obliga a recargar

  /* --- Búsqueda por título: la hace el BACKEND (?title=) ---
     Esperamos 300 ms desde la última tecla antes de llamar. Sin
     esto, escribir "Metal" dispararía 5 peticiones. Se llama
     "debounce" y es el patrón estándar para buscadores. */
  const [debouncedSearch, setDebouncedSearch] = useState('');

  useEffect(() => {
    const timer = setTimeout(() => setDebouncedSearch(filters.search), 300);
    return () => clearTimeout(timer); // se cancela si el usuario sigue escribiendo
  }, [filters.search]);

  /* --- Carga de datos ---
     useEffect con [debouncedSearch] significa: "ejecuta esto al
     montar el componente y cada vez que cambie debouncedSearch". */
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

    // Si el componente se desmonta antes de que llegue la respuesta,
    // esta bandera evita actualizar un estado que ya no existe.
    return () => { cancelled = true; };
  }, [debouncedSearch, reloadKey]);

  /* --- Opciones de los desplegables ---
     Las sacamos de los propios datos en vez de pedir /api/platforms
     y /api/genres: así sólo aparecen las consolas y géneros que
     realmente hay en la colección, sin opciones que no dan resultados. */
  const platforms = useMemo(() => {
    const set = new Set();
    games.forEach((g) => platformCodes(g).forEach((c) => c && set.add(c)));
    return [...set].sort();
  }, [games]);

  const genres = useMemo(() => {
    const set = new Set();
    games.forEach((g) => toNames(g.genres).forEach((n) => set.add(n)));
    return [...set].sort((a, b) => a.localeCompare(b, 'es'));
  }, [games]);

  /* --- Filtrado y orden en el navegador ---
     Con una colección de este tamaño es instantáneo y sin latencia
     de red. Si algún día pasas de unos cientos de juegos, esto se
     mueve al backend con parámetros en la query. */
  const visible = useMemo(() => {
    let result = games;

    if (filters.platform) {
      result = result.filter((g) => platformCodes(g).includes(filters.platform));
    }
    if (filters.genre) {
      result = result.filter((g) => toNames(g.genres).includes(filters.genre));
    }
    if (filters.onlyOwned) {
      result = result.filter((g) => g.owned);
    }

    const sorted = [...result];
    if (filters.sort === 'title') {
      sorted.sort((a, b) => a.title.localeCompare(b.title, 'es'));
    } else if (filters.sort === 'year-asc') {
      sorted.sort((a, b) => (a.releaseYear ?? 0) - (b.releaseYear ?? 0));
    } else {
      sorted.sort((a, b) => (b.releaseYear ?? 0) - (a.releaseYear ?? 0));
    }
    return sorted;
  }, [games, filters]);

  return (
    <div className="row g-4">
      <div className="col-lg-3">
        <Filters
          value={filters}
          onChange={setFilters}
          platforms={platforms}
          genres={genres}
          total={games.length}
          shown={visible.length}
        />
      </div>

      <div className="col-lg-9">
        <div className="d-flex align-items-center justify-content-between mb-3">
          <div>
            <p className="eyebrow mb-1">Colección personal</p>
            <h1 className="display-face h3 mb-0">Estantería</h1>
          </div>

          {isAdmin && (
            <button className="btn btn-primary btn-sm" onClick={() => setShowForm(true)}>
              Añadir juego
            </button>
          )}
        </div>

        {loading && <div className="notice">Cargando la colección…</div>}

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

        {!loading && !error && visible.length > 0 && (
          <div className="row g-3">
            {visible.map((game) => (
              <div className="col-md-6 col-xl-4" key={game.id}>
                <GameCase game={game} />
              </div>
            ))}
          </div>
        )}
      </div>

      {showForm && (
        <GameFormModal
          onClose={() => setShowForm(false)}
          onSaved={() => {
            setShowForm(false);
            setReloadKey((k) => k + 1); // vuelve a pedir la lista a la API
          }}
        />
      )}
    </div>
  );
}
