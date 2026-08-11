# Central Videogames — Frontend

Catálogo de colección de videojuegos. React + Vite + Bootstrap, consumiendo la API de Spring Boot.

---

## 1. Arrancarlo en local

```bash
cd frontend
npm install
npm run dev
```

Abre `http://localhost:5173`. Tu backend tiene que estar corriendo en `localhost:8080`.

La URL de la API se lee de `.env.development` (variable `VITE_API_URL`). Vite sólo expone al navegador las variables que empiezan por `VITE_`; el resto se quedan en el servidor de build.

---

## 2. Mapa de archivos

```
src/
├── main.jsx                     Arranque: monta React y envuelve la app
│                                en el router y el contexto de sesión.
├── App.jsx                      Tabla de rutas: qué URL pinta qué página.
├── index.css                    Tema visual (sobrescribe variables de Bootstrap).
│
├── api/
│   ├── client.js                ÚNICO sitio donde se llama a fetch().
│   │                            Pone la URL base, el token y traduce
│   │                            los errores HTTP a excepciones.
│   └── games.js                 Una función por endpoint + helpers que
│                                normalizan las dos formas del JSON.
│
├── auth/
│   └── AuthContext.jsx          La "sesión" global: usuario, token, isAdmin.
│
├── components/                  Piezas reutilizables, sin lógica de negocio.
│   ├── NavBar.jsx
│   ├── GameCase.jsx             La tarjeta-caja con lomo de color.
│   ├── Filters.jsx
│   └── GameFormModal.jsx        Alta y edición (mismo formulario para ambas).
│
└── pages/                       Una por ruta. Aquí sí hay lógica y estado.
    ├── GamesPage.jsx
    ├── GameDetailPage.jsx
    └── LoginPage.jsx
```

**La regla que ordena todo esto:** los componentes de `components/` reciben datos por props
y no piden nada a la API. Las páginas de `pages/` son las que cargan datos y guardan estado.
Si mañana cambias la API, tocas `api/` y como mucho las páginas; las piezas visuales ni se enteran.

---

## 3. Detalle importante del JSON

Tu API devuelve los géneros de dos formas distintas según el endpoint:

| Endpoint | `genres` | plataformas |
|---|---|---|
| `GET /api/games` | `["Sigilo", "Acción"]` (strings) | `platforms: ["PS3"]` |
| `GET /api/games/{id}` | `[{id: 14, name: "Sigilo"}]` (objetos) | `editions: [{platformAbbreviation: "PS1", ...}]` |

Los helpers `toNames()` y `platformCodes()` de `api/games.js` absorben esa diferencia,
así que los componentes funcionan igual con las dos formas. Si más adelante unificas
los DTOs en el backend, el frontend sigue funcionando sin tocar nada.

---

## 4. Lo que falta EN EL BACKEND

El frontend está escrito contra estos endpoints. Los de lectura ya los tienes.
**Los siguientes hay que crearlos** o los botones de admin darán 404:

```
POST   /api/auth/login     → { username, password }
                             responde { token, username, roles: ["ROLE_ADMIN"] }
POST   /api/games          → crea (requiere ROLE_ADMIN)
PUT    /api/games/{id}     → actualiza (requiere ROLE_ADMIN)
DELETE /api/games/{id}     → borra (requiere ROLE_ADMIN)
```

Además, en `SecurityFilterChain` hay que dejar los `GET /api/**` abiertos y exigir
`ROLE_ADMIN` para POST/PUT/DELETE. Y añadir el dominio de Render a la config de CORS,
que ahora sólo permite 5173 y 3000.

**Mientras eso no exista, la app funciona perfectamente en modo consulta pública.**
Puedes desplegar así y añadir el panel de admin después.

---

## 5. Despliegue en Render

### 5.1 El frontend (Static Site)

1. Sube este directorio a un repo de GitHub.
2. En Render: **New → Static Site**, conecta el repo.
3. Rellena:
   - **Root Directory:** `frontend` (si el repo tiene back y front juntos)
   - **Build Command:** `npm ci && npm run build`
   - **Publish Directory:** `dist`
4. **Environment → Add Environment Variable:**
   `VITE_API_URL` = `https://tu-backend.onrender.com`
5. **Redirects/Rewrites → Add Rule:**
   Source `/*` · Destination `/index.html` · Action **Rewrite**

El paso 5 es obligatorio. Sin él, entrar directo en `/juegos/3` da 404: el servidor
busca un fichero que no existe. En una SPA todas las rutas las resuelve React en el
navegador, así que el servidor tiene que devolver siempre el mismo `index.html`.
(El archivo `public/_redirects` incluido hace lo mismo, pero configúralo también en
el panel por si acaso.)

### 5.2 Cosas que te van a morder

- **La variable se congela en el build.** `VITE_API_URL` se sustituye por su valor
  cuando compilas, no cuando alguien visita la web. Si la cambias en Render, hay que
  hacer **Manual Deploy → Clear build cache & deploy**.
- **HTTPS con HTTP no se mezcla.** Si el front va por `https://` y apuntas a un
  backend `http://`, el navegador bloquea las peticiones. Los dos en HTTPS.
- **CORS otra vez.** El dominio del frontend en Render es nuevo y tu backend no lo
  conoce. Añádelo a `allowedOrigins` antes de desplegar o no cargará nada.
- **El plan gratuito duerme.** El backend gratuito de Render se apaga tras 15 min
  sin tráfico y tarda ~50 s en despertar. La primera visita parece rota. Si es para
  enseñar en entrevistas, avisa o considera el plan de pago.
- **La base de datos gratuita de Render caduca** a los 30 días. Guarda un dump.

---

## 6. Sobre la seguridad

El `isAdmin` de este frontend **sólo decide qué botones se ven**. Cualquiera puede
abrir las DevTools, forzar el valor y hacer aparecer el botón de borrar. Cuando lo
pulse, el backend le devolverá 403 y no pasará nada.

Esa es la separación correcta: el frontend gestiona la *experiencia*, el backend
gestiona los *permisos*. Nunca al revés.
