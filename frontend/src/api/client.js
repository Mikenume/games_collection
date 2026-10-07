// Cliente HTTP único: todo el resto del código pasa por aquí en vez de
// llamar a fetch() directamente.

const BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

/** Error con el código HTTP dentro, para poder decidir arriba qué hacer. */
export class ApiError extends Error {
  constructor(status, message) {
    super(message);
    this.status = status;
  }
}

// Lo registra AuthContext para enterarse cuando el backend rechaza una
// petición por falta de sesión (la sesión ha caducado).
let onUnauthorized = null;

export function setUnauthorizedHandler(handler) {
  onUnauthorized = handler;
}

async function request(path, options = {}) {
  const headers = { ...(options.headers || {}) };

  if (options.body !== undefined) {
    headers['Content-Type'] = 'application/json';
  }

  let response;
  try {
    response = await fetch(`${BASE_URL}${path}`, {
      ...options,
      headers,
      credentials: 'include',
      body: options.body !== undefined ? JSON.stringify(options.body) : undefined,
    });
  } catch (networkError) {
    throw new ApiError(0, 'No hay conexión con la API. Comprueba que el backend está arrancado.');
  }

  // 401 = no hay sesión. En las rutas de /api/auth es una respuesta normal
  // (credenciales malas, o nadie ha entrado todavía); en el resto, la
  // sesión ha caducado y se manda a login.
  if (response.status === 401) {
    const isAuthPath = path.startsWith('/api/auth/');
    if (!isAuthPath) {
      onUnauthorized?.();
    }
    throw new ApiError(
      401,
      path === '/api/auth/login' ? 'Usuario o contraseña incorrectos.' : 'Tu sesión ha caducado. Vuelve a entrar.'
    );
  }

  // 403 = hay sesión, pero ese usuario no puede hacer eso (p. ej. la cuenta demo)
  if (response.status === 403) {
    throw new ApiError(403, 'No tienes permiso para hacer esto.');
  }

  if (response.status === 204) {
    return null;
  }

  if (!response.ok) {
    let message = `Error ${response.status}`;
    try {
      const data = await response.json();
      message = data.message || data.error || message;
    } catch {
      /* respuesta sin cuerpo JSON */
    }
    throw new ApiError(response.status, message);
  }

  return response.json();
}

export const api = {
  get: (path) => request(path),
  post: (path, body) => request(path, { method: 'POST', body }),
  put: (path, body) => request(path, { method: 'PUT', body }),
  delete: (path) => request(path, { method: 'DELETE' }),
};
