// Una función por endpoint, los componentes no llaman a la API directamente.

import { api } from './client';
import ps1Logo from '../assets/logos/playstation-wordmark.svg';
import ps2Logo from '../assets/logos/PlayStation2PS.jpg';
import ps3Logo from '../assets/logos/playstation3-wordmark.png';
import ps4Logo from '../assets/logos/PlayStation_4_logo_and_wordmark.svg';
import ps5Logo from '../assets/logos/ps5_console_logo.jpg';
import smsSpine from '../assets/logos/logo_master_system.jpg';
import mdSpine from '../assets/logos/lomo_juego_megadrive.jpg';
import neoGeoLogo from '../assets/logos/logo_neo_geo.jpg';
import jaguarLogo from '../assets/logos/logo_jaguar.jpg';
import saturnSpine from '../assets/logos/logo_sega_saturn.jpg';
import n64Spine from '../assets/logos/logo_nintendo_64.jpg';
import lynxSpine from '../assets/logos/logo_lynx.jpg';
import gameGearLogo from '../assets/logos/logo_game_gear.jpg';
import xboxOneSpine from '../assets/logos/logo_xbox_one.jpg';
import xboxSeriesLogo from '../assets/logos/logo_xbox_xs.jpg';
import pspSpine from '../assets/logos/logo_psp.jpg';
import psVitaSpine from '../assets/logos/logo_ps_vita.jpg';
import switch2Spine from '../assets/logos/logo_switch_2.jpg';
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

/** Juegos creados por cuentas demo que esperan revisión (admin y demo). */
export function fetchPendingGames() {
  return api.get('/api/games/pending');
}

/** Pasa un juego pendiente al catálogo (solo admin). */
export function approveGame(id) {
  return api.put(`/api/games/${id}/approve`);
}

// Aviso para que el contador de la pestaña PENDIENTES se actualice
// cuando cambia la lista (aprobar, descartar, editar o crear como demo)
const PENDING_CHANGED = 'cv:pending-changed';

export function notifyPendingChanged() {
  window.dispatchEvent(new Event(PENDING_CHANGED));
}

export function onPendingChanged(handler) {
  window.addEventListener(PENDING_CHANGED, handler);
  return () => window.removeEventListener(PENDING_CHANGED, handler);
}

/** { createdToday, limit, remaining }: juegos que les quedan hoy a las cuentas demo. */
export function fetchDemoQuota() {
  return api.get('/api/games/demo-quota');
}

/** Busca carátulas en IGDB a través del backend (admin y demo). */
export function searchIgdb(query) {
  return api.get(`/api/igdb/search?q=${encodeURIComponent(query)}`);
}

// El listado y el detalle devuelven los géneros en formato distinto
// (strings vs objetos); estos helpers absorben esa diferencia.

/** Devuelve siempre un array de strings, venga como venga. */
export function toNames(list) {
  if (!Array.isArray(list)) return [];
  return list.map((item) => (typeof item === 'string' ? item : item?.name ?? ''))
             .filter(Boolean);
}

/** Plataformas de un juego como { abbreviation, name, releaseYear }, sin
 *  repetir y en el orden en que llegan de la API (de la más antigua a la
 *  más reciente). Vale para el listado (platforms) y el detalle (editions). */
export function platformRefs(game) {
  let refs = [];
  if (Array.isArray(game.platforms) && game.platforms.length > 0) {
    refs = game.platforms.map((p) => (typeof p === 'string' ? { abbreviation: p } : p));
  } else if (Array.isArray(game.editions)) {
    refs = game.editions.map((e) => ({ abbreviation: e.platformAbbreviation, name: e.platformName }));
  }

  const seen = new Set();
  return refs.filter((p) => {
    if (!p?.abbreviation || seen.has(p.abbreviation)) return false;
    seen.add(p.abbreviation);
    return true;
  });
}

/** Abreviaturas de plataforma de un juego, tanto del listado como del detalle. */
export function platformCodes(game) {
  return platformRefs(game).map((p) => p.abbreviation);
}

/** Nombre completo de una plataforma: viene de la tabla platforms a través de
 *  la API. Si no lo trae (p. ej. un string suelto), se queda la abreviatura. */
export function platformName(platform) {
  if (!platform) return '';
  if (typeof platform === 'string') return platform;
  return platform.name || platform.abbreviation || '';
}

// Color del texto del lomo según lo claro u oscuro que sea el fondo
const INK_DARK = 'var(--ink-dark)';
const INK_LIGHT = 'var(--ink-light)';

/** El código visual de la app: el color te dice de qué consola es el juego.
 *  Cada consola tiene UN color (variable en index.css) que se usa en el lomo,
 *  en la barra del encabezado y en los bordes, y el color del texto del lomo.
 *  Las PlayStation conservan su formato propio: lomo con el color del logo
 *  y "shell" con el de la carcasa. */
const PLATFORM_COLORS = {
  PS1: { color: 'var(--ps1)', shell: 'var(--ps1-shell)', ink: INK_DARK },
  PSX: { color: 'var(--ps1)', shell: 'var(--ps1-shell)', ink: INK_DARK },
  PS2: { color: 'var(--ps2)', shell: 'var(--ps2-shell)', ink: INK_DARK },
  PS3: { color: 'var(--ps3)', shell: 'var(--ps3-shell)', ink: INK_DARK },
  PS4: { color: 'var(--ps4)', shell: 'var(--ps4-shell)', ink: INK_DARK },
  PS5: { color: 'var(--ps5)', ink: INK_DARK },

  NES:   { color: 'var(--nes)',   ink: INK_DARK },
  SMS:   { color: 'var(--sms)',   ink: INK_LIGHT },
  7800:  { color: 'var(--a7800)', ink: INK_LIGHT },
  PCE:   { color: 'var(--pce)',   ink: INK_DARK },
  MD:    { color: 'var(--md)',    ink: INK_LIGHT },
  GB:    { color: 'var(--gb)',    ink: INK_DARK },
  LYNX:  { color: 'var(--lynx)',  ink: INK_LIGHT },
  SNES:  { color: 'var(--snes)',  ink: INK_DARK },
  AES:   { color: 'var(--aes)',   ink: INK_LIGHT },
  GG:    { color: 'var(--gg)',    ink: INK_LIGHT },
  TE:    { color: 'var(--te)',    ink: INK_LIGHT },
  CDI:   { color: 'var(--cdi)',   ink: INK_LIGHT },
  '3DO': { color: 'var(--3do)',   ink: INK_LIGHT },
  JAG:   { color: 'var(--jag)',   ink: INK_LIGHT },
  CD32:  { color: 'var(--cd32)',  ink: INK_LIGHT },
  SAT:   { color: 'var(--sat)',   ink: INK_LIGHT },
  NGCD:  { color: 'var(--aes)',   ink: INK_LIGHT },
  PCFX:  { color: 'var(--pcfx)',  ink: INK_DARK },
  VB:    { color: 'var(--vb)',    ink: INK_LIGHT },
  N64:   { color: 'var(--n64)',   ink: INK_LIGHT },
  GCOM:  { color: 'var(--gcom)',  ink: INK_DARK },
  GBC:   { color: 'var(--gbc)',   ink: INK_LIGHT },
  DC:    { color: 'var(--dc)',    ink: INK_DARK },
  NGP:   { color: 'var(--ngp)',   ink: INK_DARK },
  NGPC:  { color: 'var(--ngpc)',  ink: INK_LIGHT },
  WS:    { color: 'var(--ws)',    ink: INK_DARK },
  WSC:   { color: 'var(--wsc)',   ink: INK_DARK },
  GBA:   { color: 'var(--gba)',   ink: INK_LIGHT },
  NGC:   { color: 'var(--ngc)',   ink: INK_LIGHT },
  XBOX:  { color: 'var(--xbox)',  ink: INK_LIGHT },
  NGAGE: { color: 'var(--ngage)', ink: INK_DARK },
  NDS:   { color: 'var(--nds)',   ink: INK_DARK },
  PSP:   { color: 'var(--psp)',   ink: INK_LIGHT },
  X360:  { color: 'var(--x360)',  ink: INK_DARK },
  WII:   { color: 'var(--wii)',   ink: INK_DARK },
  '3DS': { color: 'var(--3ds)',   ink: INK_DARK },
  PSV:   { color: 'var(--psv)',   ink: INK_LIGHT },
  WIIU:  { color: 'var(--wiiu)',  ink: INK_DARK },
  XONE:  { color: 'var(--xone)',  ink: INK_LIGHT },
  NSW:   { color: 'var(--nsw)',   ink: INK_LIGHT },
  XSX:   { color: 'var(--xsx)',   ink: INK_LIGHT },
  NS2:   { color: 'var(--ns2)',   ink: INK_LIGHT },
  PC:    { color: 'var(--pc)',    ink: INK_LIGHT },
};

// Consola nueva sin color asignado: el gris lavanda genérico
const FALLBACK_COLORS = { color: 'var(--other)', ink: INK_DARK };

function platformColors(code) {
  return PLATFORM_COLORS[String(code).toUpperCase()] || FALLBACK_COLORS;
}

/** Color del lomo de la tarjeta. */
export function spineColor(code) {
  return platformColors(code).color;
}

/** Color de la barra del encabezado y de los bordes. Es el mismo que el del
 *  lomo salvo en las PlayStation, que usan el de la carcasa. */
export function shellColor(code) {
  const c = platformColors(code);
  return c.shell || c.color;
}

/** Color del texto (la abreviatura) sobre el lomo. */
export function spineInk(code) {
  return platformColors(code).ink;
}

// Fabricantes que van primero en los desplegables; el resto, alfabéticamente.
const MANUFACTURER_ORDER = ['Nintendo', 'Sony', 'Sega', 'Microsoft'];

const OTHERS = 'Otros';

// Los principales primero, luego el resto y "Otros" (sin fabricante) al final.
function manufacturerRank(name) {
  if (name === OTHERS) return MANUFACTURER_ORDER.length + 1;
  const i = MANUFACTURER_ORDER.indexOf(name);
  return i === -1 ? MANUFACTURER_ORDER.length : i;
}

// Agrupa las plataformas por fabricante para pintar <optgroup>.
// Dentro de cada grupo, por año de lanzamiento (las sin año al final).
export function groupPlatforms(platforms) {
  const groups = new Map();
  platforms.forEach((p) => {
    const key = p.manufacturer || OTHERS;
    if (!groups.has(key)) groups.set(key, []);
    groups.get(key).push(p);
  });

  return [...groups.entries()]
    .sort(([a], [b]) => manufacturerRank(a) - manufacturerRank(b) || a.localeCompare(b))
    .map(([manufacturer, list]) => ({
      manufacturer,
      platforms: [...list].sort((a, b) => {
        const ya = a.releaseYear ?? Infinity;
        const yb = b.releaseYear ?? Infinity;
        return ya - yb || a.name.localeCompare(b.name);
      }),
    }));
}

/** Logotipo oficial para el lomo; las consolas sin logo siguen con la abreviatura.
 *  length: largo del logo en vertical, cada uno tiene sus proporciones.
 *  fullColor: la imagen ya trae sus colores y su fondo (p. ej. un JPG), así que
 *  se muestra tal cual en vez de usarla como máscara blanca. */
const PS1_SPINE = { src: ps1Logo, length: '88px', background: '#000' };

// Las dos Neo Geo (AES y CD) comparten lomo
const NEO_GEO_SPINE = { src: neoGeoLogo, strip: true, fit: 'auto 240%', position: 'left center', background: '#000' };

const SPINE_LOGOS = {
  PS1: PS1_SPINE,
  PSX: PS1_SPINE,
  PS2: { src: ps2Logo, length: '130px', background: '#191814', fullColor: true },
  PS3: { src: ps3Logo, length: '120px', background: '#000' },
  PS4: { src: ps4Logo, length: '72px', background: 'var(--ps4-brand)' },

  // strip: la imagen ocupa el lomo entero (girada) en vez de ir centrada.
  // fit es su background-size: 'cover' para las que ya son tiras de lomo,
  // 'contain' o un tamaño a medida para las que tienen otra proporción.
  // background rellena lo que la imagen no cubre, con el color de sus bordes.
  PS5:   { src: ps5Logo, strip: true, fit: 'auto 205%', background: 'var(--ps5)', blend: 'multiply' },
  SMS:   { src: smsSpine, strip: true, fit: 'cover', background: '#fff' },
  MD:    { src: mdSpine, strip: true, fit: 'cover', background: '#1d2124' },
  AES:   NEO_GEO_SPINE,
  NGCD:  NEO_GEO_SPINE,
  JAG:   { src: jaguarLogo, strip: true, fit: 'auto 205%', background: '#000' },
  SAT:   { src: saturnSpine, strip: true, fit: 'cover', background: '#010101' },
  N64:   { src: n64Spine, strip: true, fit: 'cover', background: '#040202' },
  LYNX:  { src: lynxSpine, strip: true, fit: 'contain', background: '#2b2c2f' },
  // La rejilla de Game Gear se prolonga con CSS donde no llega la imagen
  GG: {
    src: gameGearLogo, strip: true, fit: 'auto 150%',
    background: 'linear-gradient(90deg, #1c2233 1px, transparent 1px) 0 0 / 6.4px 6.4px, '
      + 'linear-gradient(#1c2233 1px, transparent 1px) 0 0 / 6.4px 6.4px, #b8bdca',
  },
  XONE:  { src: xboxOneSpine, strip: true, fit: 'cover', background: '#117a11' },
  // Abajo verde y arriba blanco, como los dos extremos de la imagen
  XSX:   { src: xboxSeriesLogo, strip: true, fit: 'auto 180%', background: 'linear-gradient(to top, #047902 50%, #fefefe 50%)' },
  PSP:   { src: pspSpine, strip: true, fit: 'contain', background: '#000' },
  PSV:   { src: psVitaSpine, strip: true, fit: 'contain', background: 'linear-gradient(to top, #0153a7, #028fd7)' },
  NS2:   { src: switch2Spine, strip: true, fit: 'auto 150%', background: '#e60012' },
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
