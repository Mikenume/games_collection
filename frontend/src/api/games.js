// Una función por endpoint, los componentes no llaman a la API directamente.

import { api } from './client';
import ps1Logo from '../assets/logos/playstation-wordmark.svg';
import ps2Logo from '../assets/logos/PlayStation2PS.jpg';
import ps3Logo from '../assets/logos/playstation3-wordmark.png';
import ps4Logo from '../assets/logos/PlayStation_4_logo_and_wordmark.svg';
import crashCover from '../assets/imgs/crash_bandicoot.webp';

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

/** Color de la carcasa de la consola, para las líneas de los encabezados
 *  y el borde de las tarjetas al girar. */
const SHELL_COLORS = {
  PS1: 'var(--ps1-shell)',
  PSX: 'var(--ps1-shell)',
  PS2: 'var(--ps2-shell)',
  PS3: 'var(--ps3-shell)',
  PS4: 'var(--ps4-shell)',
};

export function shellColor(code) {
  return SHELL_COLORS[String(code).toUpperCase()] || 'var(--line)';
}

/** Nombre completo de la consola, para los encabezados de la estantería. */
const PLATFORM_NAMES = {
  PS1: 'PlayStation',
  PSX: 'PlayStation',
  PS2: 'PlayStation 2',
  PS3: 'PlayStation 3',
  PS4: 'PlayStation 4',
  PS5: 'PlayStation 5',
};

export function platformName(code) {
  return PLATFORM_NAMES[String(code).toUpperCase()] || code;
}

/** Logotipo oficial para el lomo; las consolas sin logo siguen con la abreviatura.
 *  length: largo del logo en vertical, cada uno tiene sus proporciones.
 *  fullColor: la imagen ya trae sus colores y su fondo (p. ej. un JPG), así que
 *  se muestra tal cual en vez de usarla como máscara blanca. */
const PS1_SPINE = { src: ps1Logo, length: '88px', background: '#000' };

const SPINE_LOGOS = {
  PS1: PS1_SPINE,
  PSX: PS1_SPINE,
  PS2: { src: ps2Logo, length: '130px', background: '#191814', fullColor: true },
  PS3: { src: ps3Logo, length: '120px', background: '#000' },
  PS4: { src: ps4Logo, length: '72px', background: 'var(--ps4-brand)' },
};

export function spineLogo(code) {
  return SPINE_LOGOS[String(code).toUpperCase()] || null;
}

// Carátulas por título (en minúsculas). De momento es una prueba con
// imágenes locales; los juegos sin carátula devuelven null.
const COVERS = {
  'crash bandicoot': crashCover,
};

export function coverImage(game) {
  return COVERS[String(game.title ?? '').trim().toLowerCase()] || null;
}
