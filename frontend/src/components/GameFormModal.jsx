// Formulario de alta/edición. Si recibe `game` edita; si no, crea.
// Guarda el juego y todas sus ediciones en una sola petición.
// La cuenta demo ve el mismo formulario (menos "La tengo", que es de mi
// colección); lo que envía queda pendiente hasta que el admin lo revise.

import { useCallback, useEffect, useState } from 'react';
import {
  createGame, updateGame, fetchPlatforms, fetchGenres, fetchDemoQuota, notifyPendingChanged,
} from '../api/games';
import { useAuth } from '../auth/AuthContext';
import CoverPicker from './CoverPicker';
import TrailerPicker from './TrailerPicker';

const REGIONS = ['PAL', 'NTSC-U', 'NTSC-J'];
const FORMATS = ['cartucho', 'CD', 'DVD', 'Blu-ray', 'BR', 'BD', 'tarjeta', 'digital'];

// Mismos valores por defecto que add_game(): PAL y en propiedad
let nextKey = 0;

function emptyEdition() {
  return {
    key: `new-${nextKey++}`,
    id: null,
    platformId: '',
    releaseYear: '',
    region: 'PAL',
    format: '',
    owned: true,
    portDeveloper: '',
    notes: '',
  };
}

function toEditionForm(edition) {
  return {
    key: `e-${edition.id}`,
    id: edition.id,
    platformId: edition.platformId ?? '',
    releaseYear: edition.releaseYear ?? '',
    region: edition.region ?? '',
    format: edition.format ?? '',
    owned: Boolean(edition.owned),
    portDeveloper: edition.portDeveloper ?? '',
    notes: edition.notes ?? '',
  };
}

function toForm(game) {
  return {
    title: game?.title ?? '',
    releaseYear: game?.releaseYear ?? '',
    developer: game?.developer ?? '',
    publisher: game?.publisher ?? '',
    editionType: game?.editionType ?? 'original',
    coverUrl: game?.coverUrl ?? '',
    trailerId: game?.trailerId ?? '',
    synopsis: game?.synopsis ?? '',
    notes: game?.notes ?? '',
  };
}

export default function GameFormModal({ game, onClose, onSaved }) {
  const isEdit = Boolean(game);
  const { isDemo } = useAuth();
  const demoMode = isDemo && !isEdit;

  const [form, setForm] = useState(() => toForm(game));

  // En el detalle los géneros llegan como objetos { id, name }
  const [genreIds, setGenreIds] = useState(
    () => new Set((game?.genres ?? []).map((g) => g.id).filter(Boolean))
  );

  const [editions, setEditions] = useState(() =>
    isEdit ? (game.editions ?? []).map(toEditionForm) : [emptyEdition()]
  );

  // Solo demo: { createdToday, limit, remaining }
  const [quota, setQuota] = useState(null);

  const loadQuota = useCallback(() => {
    fetchDemoQuota()
      .then(setQuota)
      .catch(() => setQuota(null));
  }, []);

  useEffect(() => {
    if (demoMode) loadQuota();
  }, [demoMode, loadQuota]);

  const quotaExhausted = demoMode && quota !== null && quota.remaining <= 0;

  const [platforms, setPlatforms] = useState([]);
  const [genres, setGenres] = useState([]);
  // Para avisar de que el año lo ha puesto IGDB y no el usuario
  const [yearFromIgdb, setYearFromIgdb] = useState(false);
  // Vídeos de YouTube del último juego elegido en IGDB, para escoger el trailer
  const [igdbVideos, setIgdbVideos] = useState([]);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    Promise.all([fetchPlatforms(), fetchGenres()])
      .then(([p, g]) => {
        setPlatforms(p);
        setGenres([...g].sort((a, b) => a.name.localeCompare(b.name)));
      })
      .catch((err) => setError(err.message));
  }, []);

  // Como hace el JS de Bootstrap con sus modales: la página de detrás no hace
  // scroll mientras el modal está abierto (y su barra no estrecha el modal en el móvil)
  useEffect(() => {
    const previous = document.body.style.overflow;
    document.body.style.overflow = 'hidden';
    return () => {
      document.body.style.overflow = previous;
    };
  }, []);

  function set(field, value) {
    setForm((prev) => ({ ...prev, [field]: value }));
  }

  // Al elegir carátula: si el año está vacío se rellena con el de IGDB
  function selectCover(result) {
    const fillYear = String(form.releaseYear).trim() === '' && result.year != null;
    setForm((prev) => ({
      ...prev,
      coverUrl: result.coverUrl,
      releaseYear: fillYear ? result.year : prev.releaseYear,
    }));
    if (fillYear) setYearFromIgdb(true);
    setIgdbVideos(result.videos ?? []);
  }

  function toggleGenre(id) {
    setGenreIds((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  }

  function setEdition(key, field, value) {
    setEditions((prev) => prev.map((e) => (e.key === key ? { ...e, [field]: value } : e)));
  }

  function removeEdition(key) {
    setEditions((prev) => prev.filter((e) => e.key !== key));
  }

  async function handleSubmit(event) {
    event.preventDefault();

    if (editions.length === 0 && !isEdit) {
      setError('Añade al menos una edición.');
      return;
    }

    setSaving(true);
    setError(null);

    const payload = {
      title: form.title.trim(),
      releaseYear: form.releaseYear ? Number(form.releaseYear) : null,
      developer: form.developer.trim() || null,
      publisher: form.publisher.trim() || null,
      editionType: form.editionType,
      coverUrl: form.coverUrl.trim() || null,
      trailerId: form.trailerId || null,
      synopsis: form.synopsis.trim() || null,
      notes: form.notes.trim() || null,
      genreIds: [...genreIds],
      editions: editions.map((e) => ({
        id: e.id,
        platformId: Number(e.platformId),
        // Vacío = el año del juego (lo resuelve el backend)
        releaseYear: e.releaseYear ? Number(e.releaseYear) : null,
        region: e.region || null,
        format: e.format || null,
        owned: e.owned,
        portDeveloper: e.portDeveloper.trim() || null,
        notes: e.notes.trim() || null,
      })),
    };

    try {
      const saved = isEdit ? await updateGame(game.id, payload) : await createGame(payload);
      notifyPendingChanged();
      onSaved(saved);
    } catch (err) {
      setError(err.message);
      // Puede que haya fallado por el cupo: se refresca el contador
      if (demoMode) loadQuota();
    } finally {
      setSaving(false);
    }
  }

  return (
    <>
      <div className="modal d-block" tabIndex="-1" role="dialog">
        <div className="modal-dialog modal-lg modal-dialog-scrollable modal-fullscreen-sm-down">
          <div className="modal-content">
            {/* El form queda entre .modal-content y .modal-body: tiene que ser
                columna flex y encogerse para que el scroll del cuerpo funcione */}
            <form onSubmit={handleSubmit} className="d-flex flex-column overflow-hidden">
              <div className="modal-header">
                <h2 className="display-face h5 mb-0">
                  {isEdit ? 'Editar juego' : 'Añadir juego'}
                </h2>
                <button type="button" className="btn-close" onClick={onClose} aria-label="Cerrar" />
              </div>

              <div className="modal-body">
                {demoMode && quota && (
                  <p className="data-face mb-3">
                    Juegos disponibles hoy: {quota.remaining} / {quota.limit}
                  </p>
                )}
                {quotaExhausted && (
                  <div className="alert alert-warning">
                    Por hoy ya no se pueden añadir más juegos con la cuenta demo. Vuelve a intentarlo mañana.
                  </div>
                )}
                {error && <div className="alert alert-danger">{error}</div>}

                <p className="eyebrow mb-2">Juego</p>

                <div className="mb-3">
                  <label className="form-label" htmlFor="f-title">Título</label>
                  <input
                    id="f-title"
                    className="form-control"
                    value={form.title}
                    onChange={(e) => set('title', e.target.value)}
                    maxLength={200}
                    required
                  />
                </div>

                <div className="row g-3 mb-3">
                  <div className="col-sm-4">
                    <label className="form-label" htmlFor="f-year">Año</label>
                    <input
                      id="f-year"
                      type="number"
                      min="1950"
                      max="2100"
                      className="form-control"
                      value={form.releaseYear}
                      onChange={(e) => {
                        setYearFromIgdb(false);
                        set('releaseYear', e.target.value);
                      }}
                    />
                    <div className="form-text data-face">
                      {yearFromIgdb ? 'Rellenado con el año de IGDB.' : 'Primer lanzamiento mundial.'}
                    </div>
                  </div>
                  <div className="col-sm-8">
                      <label className="form-label" htmlFor="f-edition">Tipo de edición</label>
                      <select
                        id="f-edition"
                        className="form-select"
                        value={form.editionType}
                        onChange={(e) => set('editionType', e.target.value)}
                      >
                        <option value="original">original</option>
                        <option value="remake">remake</option>
                        <option value="remaster">remaster</option>
                        <option value="port">port</option>
                      </select>
                  </div>
                </div>

                <div className="row g-3 mb-3">
                  <div className="col-sm-6">
                    <label className="form-label" htmlFor="f-dev">Desarrolladora</label>
                    <input
                      id="f-dev"
                      className="form-control"
                      value={form.developer}
                      onChange={(e) => set('developer', e.target.value)}
                      maxLength={120}
                    />
                  </div>
                  <div className="col-sm-6">
                    <label className="form-label" htmlFor="f-pub">Distribuidora</label>
                    <input
                      id="f-pub"
                      className="form-control"
                      value={form.publisher}
                      onChange={(e) => set('publisher', e.target.value)}
                      maxLength={120}
                    />
                  </div>
                </div>

                <div className="mb-3">
                  <CoverPicker
                    title={form.title}
                    coverUrl={form.coverUrl}
                    onSelect={selectCover}
                    onManual={(url) => set('coverUrl', url)}
                    onClear={() => set('coverUrl', '')}
                  />
                </div>

                <div className="mb-3">
                  <TrailerPicker
                    trailerId={form.trailerId}
                    videos={igdbVideos}
                    onChange={(id) => set('trailerId', id)}
                  />
                </div>

                <div className="mb-3">
                  <span className="form-label d-block">Géneros</span>
                  <div className="d-flex flex-wrap gap-2">
                    {genres.map((g) => {
                      const active = genreIds.has(g.id);
                      return (
                        <button
                          key={g.id}
                          type="button"
                          className={`btn btn-sm ${active ? 'btn-primary' : 'btn-outline-light'}`}
                          aria-pressed={active}
                          onClick={() => toggleGenre(g.id)}
                        >
                          {g.name}
                        </button>
                      );
                    })}
                    {genres.length === 0 && <span className="data-face">Cargando géneros…</span>}
                  </div>
                </div>

                <div className="mb-3">
                  <label className="form-label" htmlFor="f-syn">Sinopsis</label>
                  <textarea
                    id="f-syn"
                    className="form-control"
                    rows="4"
                    value={form.synopsis}
                    onChange={(e) => set('synopsis', e.target.value)}
                  />
                </div>

                <div className="mb-4">
                  <label className="form-label" htmlFor="f-notes">Notas</label>
                  <textarea
                    id="f-notes"
                    className="form-control"
                    rows="2"
                    value={form.notes}
                    onChange={(e) => set('notes', e.target.value)}
                  />
                </div>

                <div className="d-flex justify-content-between align-items-center mb-2">
                  <p className="eyebrow mb-0">Ediciones</p>
                  <button
                    type="button"
                    className="btn btn-sm btn-outline-light"
                    onClick={() => setEditions((prev) => [...prev, emptyEdition()])}
                  >
                    + Añadir edición
                  </button>
                </div>

                {editions.length === 0 && (
                  <p className="data-face">
                    {isEdit
                      ? 'Sin ediciones: al guardar se borrarán todas las de este juego.'
                      : 'Añade al menos una edición.'}
                  </p>
                )}

                {editions.map((ed, index) => (
                  <fieldset key={ed.key} className="border rounded p-3 mb-3">
                    <div className="d-flex justify-content-between align-items-center mb-2">
                      <legend className="h6 mb-0 w-auto">Edición {index + 1}</legend>
                      <button
                        type="button"
                        className="btn btn-sm btn-outline-danger"
                        onClick={() => removeEdition(ed.key)}
                      >
                        Quitar
                      </button>
                    </div>

                    <div className="row g-3 mb-3">
                      <div className="col-sm-6">
                        <label className="form-label" htmlFor={`f-plat-${ed.key}`}>Plataforma</label>
                        <select
                          id={`f-plat-${ed.key}`}
                          className="form-select"
                          value={ed.platformId}
                          onChange={(e) => setEdition(ed.key, 'platformId', e.target.value)}
                          required
                        >
                          <option value="" disabled>Elige una…</option>
                          {platforms.map((p) => (
                            <option key={p.id} value={p.id}>
                              {p.name} ({p.abbreviation})
                            </option>
                          ))}
                        </select>
                      </div>
                      <div className="col-sm-6">
                        <label className="form-label" htmlFor={`f-eyear-${ed.key}`}>Año en esta plataforma</label>
                        <input
                          id={`f-eyear-${ed.key}`}
                          type="number"
                          min="1970"
                          max="2100"
                          className="form-control"
                          value={ed.releaseYear}
                          onChange={(e) => setEdition(ed.key, 'releaseYear', e.target.value)}
                          placeholder={form.releaseYear ? String(form.releaseYear) : ''}
                        />
                        <div className="form-text data-face">Vacío = el año del juego.</div>
                      </div>
                    </div>

                    <div className="row g-3 mb-3">
                      <div className={demoMode ? 'col-sm-6' : 'col-sm-4'}>
                        <label className="form-label" htmlFor={`f-reg-${ed.key}`}>Región</label>
                        <select
                          id={`f-reg-${ed.key}`}
                          className="form-select"
                          value={ed.region}
                          onChange={(e) => setEdition(ed.key, 'region', e.target.value)}
                        >
                          <option value="">—</option>
                          {REGIONS.map((r) => <option key={r} value={r}>{r}</option>)}
                        </select>
                      </div>
                      <div className={demoMode ? 'col-sm-6' : 'col-sm-4'}>
                        <label className="form-label" htmlFor={`f-fmt-${ed.key}`}>Formato</label>
                        <select
                          id={`f-fmt-${ed.key}`}
                          className="form-select"
                          value={ed.format}
                          onChange={(e) => setEdition(ed.key, 'format', e.target.value)}
                        >
                          <option value="">—</option>
                          {FORMATS.map((f) => <option key={f} value={f}>{f}</option>)}
                        </select>
                      </div>
                      {/* Lo que envía la demo nunca entra en mi colección */}
                      {!demoMode && (
                        <div className="col-sm-4 d-flex align-items-end">
                          <div className="form-check mb-2">
                            <input
                              id={`f-owned-${ed.key}`}
                              type="checkbox"
                              className="form-check-input"
                              checked={ed.owned}
                              onChange={(e) => setEdition(ed.key, 'owned', e.target.checked)}
                            />
                            <label className="form-check-label" htmlFor={`f-owned-${ed.key}`}>
                              La tengo
                            </label>
                          </div>
                        </div>
                      )}
                    </div>

                    <div className="mb-3">
                      <label className="form-label" htmlFor={`f-port-${ed.key}`}>Desarrolladora del port</label>
                      <input
                        id={`f-port-${ed.key}`}
                        className="form-control"
                        value={ed.portDeveloper}
                        onChange={(e) => setEdition(ed.key, 'portDeveloper', e.target.value)}
                        maxLength={120}
                      />
                      <div className="form-text data-face">Solo si la conversión la hizo otro estudio.</div>
                    </div>

                    <div>
                      <label className="form-label" htmlFor={`f-enotes-${ed.key}`}>Notas de la edición</label>
                      <input
                        id={`f-enotes-${ed.key}`}
                        className="form-control"
                        value={ed.notes}
                        onChange={(e) => setEdition(ed.key, 'notes', e.target.value)}
                      />
                    </div>
                  </fieldset>
                ))}
              </div>

              <div className="modal-footer">
                <button type="button" className="btn btn-outline-light" onClick={onClose}>
                  Cancelar
                </button>
                <button type="submit" className="btn btn-primary" disabled={saving || quotaExhausted}>
                  {saving ? 'Guardando…' : 'Guardar'}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
      <div className="modal-backdrop show" />
    </>
  );
}
