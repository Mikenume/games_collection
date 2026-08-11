/* =========================================================
   Una función por endpoint. Los componentes no saben qué URL
   hay detrás: piden "dame los juegos" y ya está.
   ========================================================= */

import { api } from './client';

export function fetchGames(title) {
  const query = title ? `?title=${encodeURIComponent(title)}` : '';
  return api.get(`/api/games${query}`);
}

export function fetchGame(id) {
  return api.get(`/api/games/${id}`);
}

export function fetchPlatforms() {
  return api.get('/api/platforms');
}

export function fetchGenres() {
  return api.get('/api/genres');
}

/* --- Operaciones de administrador ---
   OJO: estas rutas asumen que existen en tu backend.
   Si aún no las has creado, la app funciona igual en modo
   consulta; sólo fallarán los botones de admin. */

export function createGame(game) {
  return api.post('/api/games', game);
}

export function updateGame(id, game) {
  return api.put(`/api/games/${id}`, game);
}

export function deleteGame(id) {
  return api.delete(`/api/games/${id}`);
}

/* =========================================================
   Helpers de forma del JSON.

   Tu API devuelve los géneros de DOS maneras distintas:
     - /api/games      -> ["Acción-aventura", "Sigilo"]        (strings)
     - /api/games/{id} -> [{ id: 14, name: "Sigilo" }]         (objetos)

   Estos helpers absorben esa diferencia para que los
   componentes no tengan que preocuparse.
   ========================================================= */

/** Devuelve siempre un array de strings, venga como venga. */
export function toNames(list) {
  if (!Array.isArray(list)) return [];
  return list.map((item) => (typeof item === 'string' ? item : item?.name ?? ''))
             .filter(Boolean);
}

/** Abreviaturas de plataforma de un juego, tanto del listado como del detalle. */
export function platformCodes(game) {
  if (Array.isArray(game.platforms) && game.platforms.length > 0) {
    return game.platforms.map((p) => (typeof p === 'string' ? p : p?.abbreviation ?? p?.name ?? ''));
  }
  if (Array.isArray(game.editions)) {
    return game.editions.map((e) => e.platformAbbreviation || e.platformName || '');
  }
  return [];
}

/** Color del lomo según la consola. Es el código visual de la app. */
const SPINE_COLORS = {
  PS1: 'var(--ps1)',
  PSX: 'var(--ps1)',
  PS2: 'var(--ps2)',
  PS3: 'var(--ps3)',
  PS4: 'var(--ps4)',
  PS5: 'var(--ps4)',
};

export function spineColor(code) {
  return SPINE_COLORS[String(code).toUpperCase()] || 'var(--other)';
}
