// Trailers de YouTube: en la BBDD solo se guarda el ID del vídeo (11 caracteres).

const ID = /^[A-Za-z0-9_-]{11}$/;

// Igual que YoutubeId.java: watch?v=, youtu.be/, embed/, shorts/, live/
const URL = /^(?:https?:\/\/)?(?:www\.|m\.)?(?:youtu\.be\/|youtube(?:-nocookie)?\.com\/(?:watch\?(?:\S*&)?v=|embed\/|shorts\/|live\/|v\/))([A-Za-z0-9_-]{11})(?:[?&#/]\S*)?$/;

/** ID del vídeo a partir de una URL de YouTube o del propio ID; null si no lo es. */
export function parseYoutubeId(value) {
  const text = String(value ?? '').trim();
  if (ID.test(text)) return text;
  const match = text.match(URL);
  return match ? match[1] : null;
}

export function youtubeThumb(id) {
  return `https://i.ytimg.com/vi/${id}/hqdefault.jpg`;
}

export function youtubeWatchUrl(id) {
  return `https://www.youtube.com/watch?v=${id}`;
}

// youtube-nocookie no deja cookies hasta que se le da al play
export function youtubeEmbedUrl(id) {
  return `https://www.youtube-nocookie.com/embed/${id}?autoplay=1&rel=0`;
}
