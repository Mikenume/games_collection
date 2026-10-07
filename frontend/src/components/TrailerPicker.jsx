// Trailer de YouTube para el formulario de juego. Los vídeos sugeridos
// llegan de IGDB al elegir la carátula; si no vale ninguno, se pega el enlace.
// No guarda nada por sí mismo: avisa al formulario con onChange.

import { useState } from 'react';
import { parseYoutubeId, youtubeThumb, youtubeWatchUrl } from '../api/youtube';
import './CoverPicker.css';
import './TrailerPicker.css';

export default function TrailerPicker({ trailerId, videos, onChange }) {
  const [manualUrl, setManualUrl] = useState('');
  const [manualError, setManualError] = useState(null);

  function applyManualUrl() {
    const id = parseYoutubeId(manualUrl);
    if (!id) {
      setManualError('Pega un enlace de YouTube (youtube.com/watch?v=… o youtu.be/…).');
      return;
    }
    setManualError(null);
    setManualUrl('');
    onChange(id);
  }

  function handleManualKeyDown(event) {
    // Enter usa el enlace; sin esto enviaría el formulario y guardaría el juego
    if (event.key === 'Enter') {
      event.preventDefault();
      applyManualUrl();
    }
  }

  return (
    <div className="trailer-picker">
      <span className="form-label d-block">Trailer</span>

      <div className="trailer-preview mb-3">
        {trailerId ? (
          <img src={youtubeThumb(trailerId)} alt="Trailer seleccionado" />
        ) : (
          <div className="trailer-preview-empty data-face">Sin trailer</div>
        )}
        <div className="d-flex flex-column gap-2 min-w-0">
          <p className="data-face mb-0">
            {trailerId
              ? 'Este es el trailer que se guardará.'
              : 'Opcional. Elige uno de IGDB o pega el enlace de YouTube.'}
          </p>
          {trailerId && (
            <div className="d-flex flex-wrap gap-2">
              {/* Para comprobar que es el vídeo correcto antes de guardar */}
              <a
                className="btn btn-sm btn-outline-light"
                href={youtubeWatchUrl(trailerId)}
                target="_blank"
                rel="noopener noreferrer"
              >
                Ver en YouTube
              </a>
              <button type="button" className="btn btn-sm btn-outline-danger" onClick={() => onChange('')}>
                Quitar trailer
              </button>
            </div>
          )}
        </div>
      </div>

      {videos.length > 0 ? (
        <div className="trailer-grid mb-3">
          {videos.map((v) => (
            <button
              key={v.youtubeId}
              type="button"
              className="cover-option"
              aria-pressed={v.youtubeId === trailerId}
              title={v.name || 'Vídeo'}
              onClick={() => onChange(v.youtubeId)}
            >
              <img className="trailer-thumb" src={youtubeThumb(v.youtubeId)} alt="" loading="lazy" />
              <span className="cover-name">{v.name || 'Vídeo'}</span>
            </button>
          ))}
        </div>
      ) : (
        <p className="data-face mb-3">
          Al elegir una carátula de IGDB aparecen aquí los vídeos que tenga el juego.
        </p>
      )}

      <label className="form-label" htmlFor="f-trailer-manual">Enlace de YouTube</label>
      <div className="cover-search">
        <input
          id="f-trailer-manual"
          type="url"
          className={`form-control ${manualError ? 'is-invalid' : ''}`}
          value={manualUrl}
          onChange={(e) => {
            setManualUrl(e.target.value);
            setManualError(null);
          }}
          onKeyDown={handleManualKeyDown}
          maxLength={300}
          placeholder="https://www.youtube.com/watch?v=…"
        />
        <button type="button" className="btn btn-outline-light text-nowrap" onClick={applyManualUrl}>
          Usar este vídeo
        </button>
      </div>
      {manualError && <div className="invalid-feedback d-block">{manualError}</div>}
    </div>
  );
}
