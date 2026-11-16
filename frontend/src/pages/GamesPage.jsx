import { useState, useEffect, useMemo } from 'react';
import { useSearchParams } from 'react-router-dom';
import {
  fetchGames, fetchPlatforms, groupPlatforms, toNames, platformCodes, platformRefs, platformName, shellColor,
} from '../api/games';
import { useAuth } from '../auth/AuthContext';
import GameFlipCard from '../components/GameFlipCard';
import Filters from '../components/Filters';
import GameFormModal from '../components/GameFormModal';

// Catálogo: todos los juegos aprobados, los tenga o no.
// Colección (collection, solo admin): solo los que tengo.
export default function GamesPage({ collection = false }) {
  const { isAdmin, isDemo } = useAuth();

  // Los filtros viven en la URL (?q=&consola=&genero=&orden=&tengo=1) para
  // que se conserven al entrar en la ficha de un juego y volver atrás.
  const [params, setParams] = useSearchParams();
  const filters = useMemo(() => ({
    search: params.get('q') ?? '',
    platform: params.get('consola') ?? '',
    genre: params.get('genero') ?? '',
    sort: params.get('orden') || 'estanteria',
    onlyOwned: params.get('tengo') === '1',
  }), [params]);

  // Solo se escriben los valores no vacíos; replace evita meter una entrada
  // en el historial por cada tecla del buscador.
  const setFilters = (next) => {
    const p = new URLSearchParams();
    if (next.search) p.set('q', next.search);
    if (next.platform) p.set('consola', next.platform);
    if (next.genre) p.set('genero', next.genre);
    if (next.sort && next.sort !== 'estanteria') p.set('orden', next.sort);
    if (next.onlyOwned) p.set('tengo', '1');
    setParams(p, { replace: true });
  };

  const [games, setGames] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showForm, setShowForm] = useState(false);
  // Aviso tras enviar un juego como demo (no sale en el catálogo hasta aprobarlo)
  const [notice, setNotice] = useState(null);
  const [reloadKey, setReloadKey] = useState(0);
  // Catálogo de plataformas (fabricante y año) para agrupar el filtro
  const [allPlatforms, setAllPlatforms] = useState([]);

  useEffect(() => {
    fetchPlatforms()
      .then((p) => setAllPlatforms(Array.isArray(p) ? p : []))
      .catch(() => {}); // sin catálogo el filtro sigue funcionando, todo en "Otros"
  }, []);

  // Búsqueda por título contra el backend, esperando un poco desde la
  // última tecla para no disparar una petición por cada letra.
  // Arranca con la búsqueda de la URL para no pedir la lista sin filtrar al volver.
  const [debouncedSearch, setDebouncedSearch] = useState(filters.search);

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
  // Agrupadas por fabricante con los datos de /api/platforms; una consola que
  // no esté ahí (o si falla la petición) cae en "Otros".
  const platformGroups = useMemo(() => {
    const inGames = new Map();
    baseGames.forEach((g) => platformRefs(g).forEach((ref) => inGames.set(ref.abbreviation, ref)));
    const byCode = new Map(allPlatforms.map((p) => [p.abbreviation.toUpperCase(), p]));
    const present = [...inGames.values()].map((ref) => {
      const p = byCode.get(ref.abbreviation.toUpperCase());
      return {
        id: ref.abbreviation,
        name: ref.abbreviation,
        manufacturer: p?.manufacturer,
        releaseYear: p?.releaseYear ?? ref.releaseYear,
      };
    });
    return groupPlatforms(present);
  }, [baseGames, allPlatforms]);

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

    // 'estanteria' es el orden por defecto: por fabricante (alfabético), por
    // año de la consola dentro de cada uno y luego por título, tal y como lo
    // devuelve ya la API. No se reordena.
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

  // En el orden de estantería los juegos ya llegan agrupados por fabricante
  // y consola (la primera consola de cada juego es la que lo coloca): se
  // parten en tramos consecutivos de fabricante y, dentro, de consola, cada
  // uno con su encabezado. Con cualquier otro orden las consolas se mezclan,
  // así que va todo junto, sin encabezados.
  const makers = useMemo(() => {
    if (filters.sort !== 'estanteria') {
      return [{ name: null, shelves: [{ code: null, games: visible }] }];
    }

    const groups = [];
    visible.forEach((game) => {
      const platform = platformRefs(game)[0];
      const code = platform?.abbreviation || '—';
      const makerName = platform?.manufacturer || 'Otros';

      let maker = groups[groups.length - 1];
      if (!maker || maker.name !== makerName) {
        maker = { name: makerName, shelves: [] };
        groups.push(maker);
      }

      const last = maker.shelves[maker.shelves.length - 1];
      if (last && last.code === code) last.games.push(game);
      else maker.shelves.push({ code, name: platform ? platformName(platform) : code, games: [game] });
    });
    return groups;
  }, [visible, filters.sort]);

  return (
    <div className="row g-4">
      <div className="col-lg-3">
        <Filters
          value={filters}
          onChange={setFilters}
          platformGroups={platformGroups}
          genres={genres}
          total={baseGames.length}
          shown={visible.length}
          showOwned={isAdmin && !collection}
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
            <p className="data-face mb-0">Revisa que la API responda en {import.meta.env.VITE_API_URL || window.location.origin}/api</p>
          </div>
        )}

        {!loading && !error && visible.length === 0 && (
          <div className="notice">
            No hay ningún juego que encaje con estos filtros. Prueba a quitar alguno.
          </div>
        )}

        {!loading && !error && visible.length > 0 && makers.map((maker, makerIndex) => (
          <section key={`${maker.name ?? 'todos'}-${makerIndex}`} className="maker">
            {maker.name && <h2 className="maker-heading">{maker.name}</h2>}

            {maker.shelves.map((shelf, index) => (
              <section key={`${shelf.code ?? 'todos'}-${index}`} className="shelf">
                {shelf.code && (
                  <h3 className="shelf-heading" style={{ '--shell': shellColor(shelf.code) }}>
                    {shelf.name}
                  </h3>
                )}

                <div className="row g-3">
                  {shelf.games.map((game) => (
                    <div className="col-6 col-md-4 col-lg-3 rise" key={game.id}>
                      <GameFlipCard game={game} showOwned={isAdmin} />
                    </div>
                  ))}
                </div>
              </section>
            ))}
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
