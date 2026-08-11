/* =========================================================
   Cliente HTTP único de la aplicación.

   Todo el resto del código llama aquí, nunca a fetch() directo.
   Así el token, la URL base y el tratamiento de errores viven
   en un solo sitio. Es el equivalente a tu clase de conexión
   en PHP: se toca una vez y afecta a toda la app.
   ========================================================= */

const BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

const TOKEN_KEY = 'cv_token';

export function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

export function setToken(token) {
  if (token) localStorage.setItem(TOKEN_KEY, token);
  else localStorage.removeItem(TOKEN_KEY);
}

/** Error con el código HTTP dentro, para poder decidir arriba qué hacer. */
export class ApiError extends Error {
  constructor(status, message) {
    super(message);
    this.status = status;
  }
}

async function request(path, options = {}) {
  const headers = { ...(options.headers || {}) };

  if (options.body !== undefined) {
    headers['Content-Type'] = 'application/json';
  }

  const token = getToken();
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  let response;
  try {
    response = await fetch(`${BASE_URL}${path}`, {
      ...options,
      headers,
      body: options.body !== undefined ? JSON.stringify(options.body) : undefined,
    });
  } catch (networkError) {
    // fetch() sólo lanza si no hubo respuesta: servidor caído, CORS, DNS.
    throw new ApiError(0, 'No hay conexión con la API. Comprueba que el backend está arrancado.');
  }

  if (response.status === 401) {
    setToken(null);
    throw new ApiError(401, 'La sesión ha caducado. Vuelve a entrar.');
  }

  if (response.status === 403) {
    throw new ApiError(403, 'No tienes permiso para hacer esto.');
  }

  if (response.status === 204) {
    return null;
  }

  if (!response.ok) {
    // Spring devuelve JSON de error; si no, nos quedamos con el texto.
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
