// Una función por endpoint, los componentes no llaman a la API directamente.

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

export function createGame(game) {
  return api.post('/api/games', game);
}

export function updateGame(id, game) {
  return api.put(`/api/games/${id}`, game);
}

export function deleteGame(id) {
  return api.delete(`/api/games/${id}`);
}

// El listado y el detalle devuelven los géneros en formato distinto
// (strings vs objetos); estos helpers absorben esa diferencia.

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
