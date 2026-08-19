// No guarda estado propio: recibe los valores y una función para cambiarlos.
// El estado real vive en GamesPage.

export default function Filters({ value, onChange, platforms, genres, total, shown }) {
  function set(field, fieldValue) {
    onChange({ ...value, [field]: fieldValue });
  }

  const isFiltered = shown !== total;

  return (
    <aside className="rail">
      <h2>Buscar</h2>
      <input
        type="search"
        className="form-control mb-4"
        placeholder="Título del juego"
        value={value.search}
        onChange={(e) => set('search', e.target.value)}
        aria-label="Buscar por título"
      />

      <h2>Consola</h2>
      <select
        className="form-select mb-4"
        value={value.platform}
        onChange={(e) => set('platform', e.target.value)}
        aria-label="Filtrar por consola"
      >
        <option value="">Todas</option>
        {platforms.map((p) => (
          <option key={p} value={p}>{p}</option>
        ))}
      </select>

      <h2>Género</h2>
      <select
        className="form-select mb-4"
        value={value.genre}
        onChange={(e) => set('genre', e.target.value)}
        aria-label="Filtrar por género"
      >
        <option value="">Todos</option>
        {genres.map((g) => (
          <option key={g} value={g}>{g}</option>
        ))}
      </select>

      <h2>Orden</h2>
      <select
        className="form-select mb-4"
        value={value.sort}
        onChange={(e) => set('sort', e.target.value)}
        aria-label="Ordenar la lista"
      >
        <option value="title">Título (A–Z)</option>
        <option value="year-asc">Año (más antiguo)</option>
        <option value="year-desc">Año (más reciente)</option>
      </select>

      <div className="form-check mb-4">
        <input
          className="form-check-input"
          type="checkbox"
          id="only-owned"
          checked={value.onlyOwned}
          onChange={(e) => set('onlyOwned', e.target.checked)}
        />
        <label className="form-check-label" htmlFor="only-owned">
          Sólo los que tengo
        </label>
      </div>

      <p className="data-face mb-0">
        {isFiltered ? `${shown} de ${total} juegos` : `${total} juegos`}
      </p>

      {isFiltered && (
        <button
          className="btn btn-sm btn-outline-light mt-3 w-100"
          onClick={() => onChange({ search: '', platform: '', genre: '', sort: 'title', onlyOwned: false })}
        >
          Quitar filtros
        </button>
      )}
    </aside>
  );
}
