// La "caja" de un juego: lomo de color con la consola + portada con
// el título y los datos. Componente de presentación, recibe el juego por props.

import { Link } from 'react-router-dom';
import { toNames, platformCodes, spineColor, spineLogo } from '../api/games';

export default function GameCase({ game }) {
  const codes = platformCodes(game);
  const mainCode = codes[0] || '—';
  const logo = spineLogo(mainCode);
  const genres = toNames(game.genres);

  return (
    <Link
      to={`/juegos/${game.id}`}
      className="case rise"
      style={{ '--spine': spineColor(mainCode) }}
    >
      <div
        className={logo ? 'case-spine case-spine-logo' : 'case-spine'}
        style={logo ? { '--spine-bg': logo.background } : undefined}
      >
        {logo ? (
          <div
            className={logo.fullColor ? 'spine-logo spine-logo-color' : 'spine-logo'}
            role="img"
            aria-label={mainCode}
            style={{ '--logo': `url(${logo.src})`, '--logo-length': logo.length }}
          />
        ) : (
          <span>{mainCode}</span>
        )}
      </div>

      <div className="case-body">
        <h3 className="case-title">{game.title}</h3>

        <p className="data-face mb-2">
          {game.releaseYear} · {game.developer}
        </p>

        <div>
          {genres.slice(0, 3).map((genre) => (
            <span key={genre} className="tag">{genre}</span>
          ))}
          {game.owned === false && <span className="tag tag-missing">no la tengo</span>}
        </div>
      </div>
    </Link>
  );
}
