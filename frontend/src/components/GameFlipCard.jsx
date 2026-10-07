// Tarjeta del catálogo que se da la vuelta: delante el lomo de la consola
// y la carátula, detrás el título y los datos. El giro es solo CSS
// (:hover / :focus-within); aquí solo se decide cuándo navegar a la ficha.

import { useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { toNames, platformCodes, spineColor, spineLogo, shellColor, coverImage } from '../api/games';
import './GameFlipCard.css';

// showOwned: el aviso de "no la tengo" solo tiene sentido para el admin.
export default function GameFlipCard({ game, showOwned = false }) {
  const navigate = useNavigate();
  const pointerType = useRef('mouse');
  const [flipped, setFlipped] = useState(false);

  const codes = platformCodes(game);
  const mainCode = codes[0] || '—';
  const logo = spineLogo(mainCode);
  const genres = toNames(game.genres);
  const cover = game.coverUrl || coverImage(game);
  const detailUrl = `/juegos/${game.id}`;

  // Con ratón la tarjeta ya está girada por el :hover, así que el clic
  // navega. Con el dedo, el primer toque la gira y el segundo navega.
  function handleClick() {
    if (pointerType.current !== 'mouse' && !flipped) {
      setFlipped(true);
      return;
    }
    navigate(detailUrl);
  }

  function handleKeyDown(event) {
    if (event.key === 'Enter' || event.key === ' ') {
      event.preventDefault();
      navigate(detailUrl);
    }
  }

  return (
    <div
      className={flipped ? 'flip-card is-flipped' : 'flip-card'}
      style={{ '--spine': spineColor(mainCode), '--shell': shellColor(mainCode) }}
      role="link"
      tabIndex={0}
      aria-label={`${game.title} (${mainCode}), ver ficha`}
      onPointerDown={(event) => { pointerType.current = event.pointerType; }}
      onClick={handleClick}
      onKeyDown={handleKeyDown}
      onBlur={() => setFlipped(false)}
    >
      <div className="flip-card-inner">
        <div className="flip-card-face flip-card-front">
          <div
            className="flip-card-spine"
            style={logo ? { '--spine-bg': logo.background } : undefined}
          >
            {logo ? (
              <div
                className={logo.fullColor ? 'flip-card-logo flip-card-logo-color' : 'flip-card-logo'}
                style={{ '--logo': `url(${logo.src})`, '--logo-length': logo.length }}
              />
            ) : (
              <span>{mainCode}</span>
            )}
          </div>

          {cover ? (
            <img className="flip-card-cover" src={cover} alt={game.title} loading="lazy" />
          ) : (
            <div className="flip-card-cover flip-card-placeholder">
              <span>{game.title}</span>
            </div>
          )}
        </div>

        <div className="flip-card-face flip-card-back" aria-hidden="true">
          <div className="flip-card-body">
            <h3 className="flip-card-title">{game.title}</h3>

            <p className="data-face flip-card-meta">
              {game.releaseYear} · {game.developer}
            </p>

            <div>
              {genres.slice(0, 3).map((genre) => (
                <span key={genre} className="tag">{genre}</span>
              ))}
              {showOwned && game.owned === false &&<span className="tag tag-missing">no la tengo</span>}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
