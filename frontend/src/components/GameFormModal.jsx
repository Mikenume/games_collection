/* Formulario de alta/edición. Si recibe `game` edita; si no, crea.

   Ojo con una regla de React: NUNCA uses <form> con envío nativo
   dentro de una SPA, porque recargaría la página entera. Aquí el
   <form> lleva onSubmit con preventDefault(), que es lo correcto. */

import { useState } from 'react';
import { createGame, updateGame, toNames } from '../api/games';

export default function GameFormModal({ game, onClose, onSaved }) {
  const isEdit = Boolean(game);

  const [form, setForm] = useState({
    title: game?.title ?? '',
    releaseYear: game?.releaseYear ?? '',
    developer: game?.developer ?? '',
    publisher: game?.publisher ?? '',
    editionType: game?.editionType ?? 'original',
    synopsis: game?.synopsis ?? '',
    notes: game?.notes ?? '',
    genres: toNames(game?.genres).join(', '),
  });

  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);

  function set(field, value) {
    setForm((prev) => ({ ...prev, [field]: value }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setSaving(true);
    setError(null);

    // Lo que se manda al backend. Ajusta los nombres si tu DTO de
    // entrada difiere del de salida (es lo habitual: el de entrada
    // suele recibir ids de género en vez de nombres).
    const payload = {
      title: form.title.trim(),
      releaseYear: form.releaseYear ? Number(form.releaseYear) : null,
      developer: form.developer.trim() || null,
      publisher: form.publisher.trim() || null,
      editionType: form.editionType,
      synopsis: form.synopsis.trim() || null,
      notes: form.notes.trim() || null,
      genres: form.genres.split(',').map((g) => g.trim()).filter(Boolean),
    };

    try {
      if (isEdit) await updateGame(game.id, payload);
      else await createGame(payload);
      onSaved();
    } catch (err) {
      setError(err.message);
    } finally {
      setSaving(false);
    }
  }

  return (
    <>
      <div className="modal d-block" tabIndex="-1" role="dialog">
        <div className="modal-dialog modal-lg modal-dialog-scrollable">
          <div className="modal-content">
            <form onSubmit={handleSubmit}>
              <div className="modal-header">
                <h2 className="display-face h5 mb-0">
                  {isEdit ? 'Editar juego' : 'Añadir juego'}
                </h2>
                <button type="button" className="btn-close" onClick={onClose} aria-label="Cerrar" />
              </div>

              <div className="modal-body">
                {error && <div className="alert alert-danger">{error}</div>}

                <div className="mb-3">
                  <label className="form-label" htmlFor="f-title">Título</label>
                  <input
                    id="f-title"
                    className="form-control"
                    value={form.title}
                    onChange={(e) => set('title', e.target.value)}
                    required
                  />
                </div>

                <div className="row g-3 mb-3">
                  <div className="col-sm-4">
                    <label className="form-label" htmlFor="f-year">Año</label>
                    <input
                      id="f-year"
                      type="number"
                      min="1970"
                      max="2100"
                      className="form-control"
                      value={form.releaseYear}
                      onChange={(e) => set('releaseYear', e.target.value)}
                    />
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
                    />
                  </div>
                  <div className="col-sm-6">
                    <label className="form-label" htmlFor="f-pub">Distribuidora</label>
                    <input
                      id="f-pub"
                      className="form-control"
                      value={form.publisher}
                      onChange={(e) => set('publisher', e.target.value)}
                    />
                  </div>
                </div>

                <div className="mb-3">
                  <label className="form-label" htmlFor="f-genres">Géneros</label>
                  <input
                    id="f-genres"
                    className="form-control"
                    value={form.genres}
                    onChange={(e) => set('genres', e.target.value)}
                    placeholder="Acción-aventura, Sigilo"
                  />
                  <div className="form-text data-face">Sepáralos con comas.</div>
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

                <div>
                  <label className="form-label" htmlFor="f-notes">Notas</label>
                  <textarea
                    id="f-notes"
                    className="form-control"
                    rows="2"
                    value={form.notes}
                    onChange={(e) => set('notes', e.target.value)}
                  />
                </div>
              </div>

              <div className="modal-footer">
                <button type="button" className="btn btn-outline-light" onClick={onClose}>
                  Cancelar
                </button>
                <button type="submit" className="btn btn-primary" disabled={saving}>
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
