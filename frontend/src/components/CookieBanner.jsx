// src/components/CookieBanner.jsx
import { useEffect, useState } from 'react';
import { getConsent, setConsent } from '../analytics';

export default function CookieBanner() {
    // Se muestra si el usuario todavía no ha elegido
    const [visible, setVisible] = useState(() => getConsent() === null);

    // Permite reabrirlo desde el footer
    useEffect(() => {
        const open = () => setVisible(true);
        window.addEventListener('open-cookie-banner', open);
        return () => window.removeEventListener('open-cookie-banner', open);
    }, []);

    if (!visible) return null;

    const choose = (value) => {
        setConsent(value);
        setVisible(false);
    };

    return (
        <div
            className="position-fixed bottom-0 start-0 end-0 bg-dark text-light p-3 shadow-lg"
            style={{ zIndex: 1080 }}
            role="dialog"
            aria-live="polite"
            aria-label="Aviso de cookies"
        >
            <div className="container d-flex flex-column flex-md-row align-items-md-center gap-3">
                <p className="mb-0 flex-grow-1 small">
                    Esta web usa cookies de Google Analytics para contar visitas y saber qué
                    páginas se consultan. No se usan para publicidad. Puedes cambiar tu
                    elección en cualquier momento desde el pie de página.
                </p>
                <div className="d-flex gap-2 flex-shrink-0">
                    {/* Mismo estilo en ambos botones: rechazar debe ser igual de fácil que aceptar */}
                    <button type="button" className="btn btn-light btn-sm" onClick={() => choose('denied')}>
                        Rechazar
                    </button>
                    <button type="button" className="btn btn-light btn-sm" onClick={() => choose('granted')}>
                        Aceptar
                    </button>
                </div>
            </div>
        </div>
    );
}
