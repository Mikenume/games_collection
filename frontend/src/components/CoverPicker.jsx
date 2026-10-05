// Buscador de carátulas en IGDB para el formulario de juego.
// No guarda nada por sí mismo: avisa al formulario con onSelect / onClear.

import { useRef, useState } from 'react';
import { searchIgdb } from '../api/games';
import './CoverPicker.css';

// Igual que la validación de GameRequest en el backend
const HTTPS_URL = /^https:\/\/[^\s/]+\.[^\s/]+\/\S*$/;
const MAX_URL_LENGTH = 255;

export default function CoverPicker({ title, coverUrl, onSelect, onManual, onClear }) {
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

  // URL a mano, para cuando IGDB no tiene la carátula buscada (p. ej. la PAL)
  const [manualOpen, setManualOpen] = useState(false);
  const [manualUrl, setManualUrl] = useState('');
  const [manualError, setManualError] = useState(null);

  // Una URL pegada a mano puede no cargar (enlace roto o web que no deja enlazarla)
  const [brokenUrl, setBrokenUrl] = useState(null);

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

  function applyManualUrl() {
    const url = manualUrl.trim();
    if (!HTTPS_URL.test(url)) {
      setManualError('La URL tiene que empezar por https:// y no puede llevar espacios.');
      return;
    }
    if (url.length > MAX_URL_LENGTH) {
      setManualError(`La URL no puede superar los ${MAX_URL_LENGTH} caracteres.`);
      return;
    }
    setManualError(null);
    setManualUrl('');
    setManualOpen(false);
    onManual(url);
  }

  function handleManualKeyDown(event) {
    if (event.key === 'Enter') {
      event.preventDefault();
      applyManualUrl();
    }
  }

  const loading = status === 'loading';
  const previewBroken = Boolean(coverUrl) && brokenUrl === coverUrl;

  return (
    <div className="cover-picker">
      <span className="form-label d-block">Carátula</span>

      <div className="cover-preview mb-3">
        {coverUrl && !previewBroken ? (
          <img src={coverUrl} alt="Carátula seleccionada" onError={() => setBrokenUrl(coverUrl)} />
        ) : (
          <div className="cover-preview-empty data-face">
            {previewBroken ? 'No se puede cargar la imagen' : 'Sin carátula'}
          </div>
        )}
        <div className="d-flex flex-column gap-2 min-w-0">
          <p className="data-face mb-0">
            {previewBroken
              ? 'La URL no carga: comprueba el enlace o quita la carátula.'
              : coverUrl
                ? 'Esta es la carátula que se guardará.'
                : 'Busca el juego en IGDB y elige su carátula.'}
          </p>
          {coverUrl && !coverUrl.startsWith('https://images.igdb.com/') && (
            <p className="cover-url data-face mb-0" title={coverUrl}>{coverUrl}</p>
          )}
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

      {manualOpen ? (
        <div className="cover-manual">
          <label className="form-label" htmlFor="f-cover-manual">URL de la imagen</label>
          <div className="cover-search">
            <input
              id="f-cover-manual"
              type="url"
              className={`form-control ${manualError ? 'is-invalid' : ''}`}
              value={manualUrl}
              onChange={(e) => {
                setManualUrl(e.target.value);
                setManualError(null);
              }}
              onKeyDown={handleManualKeyDown}
              maxLength={MAX_URL_LENGTH}
              placeholder="https://…/caratula.jpg"
              aria-describedby="f-cover-manual-help"
              autoFocus
            />
            <button type="button" className="btn btn-outline-light text-nowrap" onClick={applyManualUrl}>
              Usar esta URL
            </button>
            <button
              type="button"
              className="btn btn-link text-nowrap"
              onClick={() => {
                setManualOpen(false);
                setManualError(null);
              }}
            >
              Cancelar
            </button>
          </div>
          {manualError && <div className="invalid-feedback d-block">{manualError}</div>}
          <div id="f-cover-manual-help" className="form-text data-face">
            Enlace directo a la imagen (clic derecho → «Copiar dirección de imagen»).
          </div>
        </div>
      ) : (
        <button type="button" className="btn btn-link btn-sm p-0" onClick={() => setManualOpen(true)}>
          ¿No está la que buscas? Pega la URL de la imagen a mano
        </button>
      )}
    </div>
  );
}
