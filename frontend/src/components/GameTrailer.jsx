// Trailer en la ficha del juego. Hasta que se pulsa play solo se ve la
// miniatura: así la página no carga el reproductor de YouTube para nada.

import { useState } from 'react';
import { youtubeEmbedUrl, youtubeThumb } from '../api/youtube';
import './GameTrailer.css';

export default function GameTrailer({ youtubeId, title }) {
  const [playing, setPlaying] = useState(false);

  return (
    <div className="game-trailer">
      {playing ? (
        <iframe
          src={youtubeEmbedUrl(youtubeId)}
          title={`Trailer de ${title}`}
          allow="autoplay; encrypted-media; picture-in-picture; fullscreen"
          allowFullScreen
          referrerPolicy="strict-origin-when-cross-origin"
        />
      ) : (
        <button
          type="button"
          className="game-trailer-poster"
          onClick={() => setPlaying(true)}
          aria-label={`Reproducir el trailer de ${title}`}
        >
          <img src={youtubeThumb(youtubeId)} alt="" loading="lazy" />
          <span className="game-trailer-play" aria-hidden="true" />
        </button>
      )}
    </div>
  );
}
