export default function Footer() {
  return (
    <footer className="footer">
      <div className="container py-3">
        <div className="d-flex align-items-center gap-3 mb-1">
          <span className="footer-name">Miguel Núñez</span>
          <a
            href="https://github.com/Mikenume/games_collection"
            target="_blank"
            rel="noopener noreferrer"
            aria-label="Repositorio en GitHub"
            className="footer-icon-link"
          >
            <svg width="18" height="18" viewBox="0 0 16 16" fill="currentColor" aria-hidden="true">
              <path d="M8 0C3.58 0 0 3.58 0 8c0 3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38
                0-.19-.01-.82-.01-1.49-2.01.37-2.53-.49-2.69-.94-.09-.23-.48-.94-.82-1.13
                -.28-.15-.68-.52-.01-.53.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66
                .07-.52.28-.87.51-1.07-1.78-.2-3.64-.89-3.64-3.95 0-.87.31-1.59.82-2.15
                -.08-.2-.36-1.02.08-2.12 0 0 .67-.21 2.2.82.64-.18 1.32-.27 2-.27.68 0
                1.36.09 2 .27 1.53-1.04 2.2-.82 2.2-.82.44 1.1.16 1.92.08 2.12.51.56.82
                1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95.29.25.54.73.54 1.48 0 1.07-.01
                1.93-.01 2.2 0 .21.15.46.55.38A8.01 8.01 0 0 0 16 8c0-4.42-3.58-8-8-8Z" />
            </svg>
          </a>
          <a
            href="https://www.linkedin.com/in/miguel-núñez-4960aaa9"
            target="_blank"
            rel="noopener noreferrer"
            aria-label="Perfil de LinkedIn"
            className="footer-icon-link"
          >
            <svg width="18" height="18" viewBox="0 0 16 16" fill="currentColor" aria-hidden="true">
              <path d="M0 1.15C0 .51.55 0 1.23 0h13.54C15.45 0 16 .51 16 1.15v13.7
                c0 .64-.55 1.15-1.23 1.15H1.23C.55 16 0 15.49 0 14.85V1.15Z
                M4.75 13.4V6.17H2.4v7.23h2.35ZM3.58 5.18c.82 0 1.33-.55 1.33-1.23
                -.02-.7-.51-1.23-1.31-1.23-.8 0-1.33.53-1.33 1.23 0 .68.51 1.23
                1.29 1.23h.02ZM6.1 13.4h2.35V9.37c0-.22.02-.43.08-.59.18-.43.58-.88
                1.25-.88.88 0 1.23.67 1.23 1.66v3.84h2.35V9.28c0-2.17-1.16-3.18
                -2.71-3.18-1.25 0-1.8.7-2.11 1.18v.02h-.02l.02-.02V6.17H6.1
                c.03.66 0 7.23 0 7.23Z" />
            </svg>
          </a>
          <a
            href="mailto:minunezme@gmail.com"
            aria-label="Enviar un correo"
            className="footer-icon-link"
          >
            <svg width="18" height="18" viewBox="0 0 16 16" fill="currentColor" aria-hidden="true">
              <path d="M1.75 3h12.5c.966 0 1.75.784 1.75 1.75v6.5A1.75 1.75 0 0 1 14.25 13H1.75A1.75 1.75 0 0 1
                0 11.25v-6.5C0 3.784.784 3 1.75 3Zm.2 1.5 5.4 3.9c.38.28.92.28 1.3
                0l5.4-3.9H1.95ZM1.5 5.59v5.66c0 .14.11.25.25.25h12.5a.25.25 0 0
                0 .25-.25V5.59l-5.66 4.09c-.79.57-1.86.57-2.65
                0L1.5 5.59Z" />
            </svg>
            <span className="data-face mb-0">minunezme@gmail.com</span>
          </a>
          <span className="data-face mb-0">· PostgreSQL - Spring Boot - React ·</span>
          <span className="data-face mb-0">Central Videogames 09/2026</span>

        </div>

      </div>
    </footer>
  );
}
