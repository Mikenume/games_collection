// Buscador de carátulas en IGDB para el formulario de juego.
// No guarda nada por sí mismo: avisa al formulario con onSelect / onClear.

import { useRef, useState } from 'react';
import { searchIgdb } from '../api/games';
import './CoverPicker.css';

export default function CoverPicker({ title, coverUrl, onSelect, onClear }) {
  // La búsqueda sigue al título del juego hasta que se escribe en ella a mano
  const [query, setQuery] = useState('');
  const [touched, setTouched] = useState(false);
  const searchText = touched ? query : title;

  const [status, setStatus] = useState('idle'); // idle | loading | done | error
  const [results, setResults] = useState([]);
  const [error, setError] = useState(null);
  const [searched, setSearched] = useState('');

  // Si se lanzan dos búsquedas seguidas, solo cuenta la última
  const lastRequest = useRef(0);

  async function search() {
    const q = searchText.trim();
    if (!q) {
      setStatus('error');
      setError('Escribe el nombre del juego que quieres buscar.');
      return;
    }

    const requestId = ++lastRequest.current;
    setStatus('loading');
    setError(null);

    try {
      const data = await searchIgdb(q);
      if (requestId !== lastRequest.current) return;
      setResults(data ?? []);
      setSearched(q);
      setStatus('done');
    } catch (err) {
      if (requestId !== lastRequest.current) return;
      setError(err.message);
      setStatus('error');
    }
  }

  function handleKeyDown(event) {
    // Enter busca; sin esto enviaría el formulario y guardaría el juego
    if (event.key === 'Enter') {
      event.preventDefault();
      search();
    }
  }

  const loading = status === 'loading';

  return (
    <div className="cover-picker">
      <span className="form-label d-block">Carátula</span>

      <div className="cover-preview mb-3">
        {coverUrl ? (
          <img src={coverUrl} alt="Carátula seleccionada" />
        ) : (
          <div className="cover-preview-empty data-face">Sin carátula</div>
        )}
        <div className="d-flex flex-column gap-2">
          <p className="data-face mb-0">
            {coverUrl
              ? 'Esta es la carátula que se guardará.'
              : 'Busca el juego en IGDB y elige su carátula.'}
          </p>
          {coverUrl && (
            <button type="button" className="btn btn-sm btn-outline-danger align-self-start" onClick={onClear}>
              Quitar carátula
            </button>
          )}
        </div>
      </div>

      <div className="cover-search mb-3">
        <input
          type="search"
          className="form-control"
          value={searchText}
          onChange={(e) => {
            setTouched(true);
            setQuery(e.target.value);
          }}
          onKeyDown={handleKeyDown}
          maxLength={100}
          placeholder="Nombre del juego en IGDB"
          aria-label="Buscar carátula en IGDB"
        />
        <button
          type="button"
          className="btn btn-outline-light text-nowrap"
          onClick={search}
          disabled={loading}
        >
          {loading && <span className="spinner-border spinner-border-sm me-2" aria-hidden="true" />}
          {loading ? 'Buscando…' : 'Buscar carátula'}
        </button>
      </div>

      <div aria-live="polite">
        {status === 'error' && <div className="alert alert-warning py-2 mb-3">{error}</div>}

        {status === 'done' && results.length === 0 && (
          <p className="data-face mb-3">Sin resultados para «{searched}».</p>
        )}
      </div>

      {status === 'done' && results.length > 0 && (
        <div className="cover-grid mb-3">
          {results.map((r) => {
            const hasCover = Boolean(r.coverUrl);
            const selected = hasCover && r.coverUrl === coverUrl;
            const meta = [r.year, (r.platforms ?? []).join(', ')].filter(Boolean).join(' · ');
            return (
              <button
                key={r.igdbId}
                type="button"
                className="cover-option"
                aria-pressed={selected}
                disabled={!hasCover}
                title={hasCover ? r.name : `${r.name} (sin carátula en IGDB)`}
                onClick={() => onSelect(r)}
              >
                {hasCover ? (
                  <img
                    className="cover-thumb"
                    src={r.thumbUrl}
                    // La miniatura de IGDB mide 90 px: en pantallas densas se usa la grande
                    srcSet={`${r.thumbUrl} 90w, ${r.coverUrl} 264w`}
                    sizes="120px"
                    alt=""
                    loading="lazy"
                  />
                ) : (
                  <span className="cover-thumb cover-thumb-empty">Sin carátula</span>
                )}
                <span className="cover-name">{r.name}</span>
                {meta && <span className="cover-meta">{meta}</span>}
              </button>
            );
          })}
        </div>
      )}
    </div>
  );
}
