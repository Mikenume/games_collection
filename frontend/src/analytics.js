// src/analytics.js
// Gestiona el consentimiento de cookies y la carga de Google Analytics.
// GA solo se descarga si el usuario ha aceptado.

const STORAGE_KEY = 'cookie_consent'; // valores: 'granted' | 'denied'
let gaLoaded = false;

export function getConsent() {
    try {
        return localStorage.getItem(STORAGE_KEY); // null si aún no ha elegido
    } catch {
        return null;
    }
}

// Concede el consentimiento y descarga gtag.js (una sola vez)
export function initAnalytics() {
    window.gtag?.('consent', 'update', { analytics_storage: 'granted' });
    if (gaLoaded) return;
    gaLoaded = true;
    const script = document.createElement('script');
    script.async = true;
    script.src = `https://www.googletagmanager.com/gtag/js?id=G-${import.meta.env.VITE_GA_ID}`;
    document.head.appendChild(script);
}

// Envía una visita solo si hay consentimiento
export function trackPageView(path) {
    if (getConsent() !== 'granted') return;
    window.gtag?.('event', 'page_view', {
        page_path: path,
        page_title: document.title,
    });
}

// Guarda la elección del usuario y actúa en consecuencia
export function setConsent(value) {
    try {
        localStorage.setItem(STORAGE_KEY, value);
    } catch {
        /* modo privado o almacenamiento bloqueado: se ignora */
    }

    if (value === 'granted') {
        initAnalytics();
        trackPageView(window.location.pathname + window.location.search);
    } else {
        window.gtag?.('consent', 'update', { analytics_storage: 'denied' });
        deleteGaCookies();
    }
}

// Abre de nuevo el banner (para el enlace "Configurar cookies" del footer)
export function openCookieSettings() {
    window.dispatchEvent(new Event('open-cookie-banner'));
}

// Borra las cookies _ga* si el usuario retira el consentimiento
function deleteGaCookies() {
    const host = window.location.hostname;
    document.cookie
        .split(';')
        .map((c) => c.split('=')[0].trim())
        .filter((name) => name.startsWith('_ga'))
        .forEach((name) => {
            const expired = `${name}=; expires=Thu, 01 Jan 1970 00:00:00 GMT; path=/`;
            document.cookie = expired;
            document.cookie = `${expired}; domain=${host}`;
            document.cookie = `${expired}; domain=.${host}`;
        });
}