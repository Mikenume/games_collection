# Central Videogames

Catálogo web de mi colección personal de videojuegos. Backend en Spring Boot + PostgreSQL, frontend en React. Es el proyecto que hice como pieza de portfolio al terminar el ciclo de DAW.

**Demo:** https://central-videogames.onrender.com
**API:** https://central-videogames-api.onrender.com/api/games

![Captura de la aplicación](docs/captura_index.png)

> El backend está en Render (plan gratuito), así que la primera petición puede tardar unos segundos en despertar.

---

## Qué hace

- Catálogo de juegos con portada, plataformas y géneros.
- Búsqueda por título y filtros por plataforma/género.
- Ficha de detalle por juego, con su carátula y su trailer de YouTube.
- Panel de administración (login + alta/edición/borrado de juegos) para el usuario admin. El juego y todas sus ediciones se guardan juntos, en una sola transacción. Qué ediciones tengo y cuáles no solo lo ve el admin.
- Cuenta **DEMO** (botón en la barra de navegación) para probar la app sin credenciales: puede proponer juegos, que quedan pendientes hasta que el admin los revisa y los aprueba desde la pestaña Pendientes.
- Buscador de carátulas en [IGDB](https://www.igdb.com/) dentro del formulario de juego. Si IGDB no tiene la que buscas (por ejemplo, la edición europea), se puede pegar la URL de la imagen a mano. Al elegir el juego, IGDB sugiere también sus vídeos para escoger el trailer, o se pega el enlace de YouTube.

La idea central del modelo es separar **juego** de **edición**: un juego es la obra (Resident Evil 4), y una edición es el ejemplar concreto en una plataforma (la de GameCube, la de PS2...). Así se pueden representar ports y multiplataforma sin repetir datos.

---

## Stack

- **Base de datos:** PostgreSQL
- **Backend:** Java 21, Spring Boot, Spring Data JPA, Spring Security, Maven
- **Frontend:** React, Vite, React Router, Bootstrap
- **Despliegue:** Docker (backend) + Render
- **Carátulas:** API de IGDB (a través de Twitch)

---

## Estructura

```
games_collection/
├── backend/          API REST en Spring Boot
│   └── src/main/java/com/miguel/gamescollection/
│       ├── model/         Entidades JPA
│       ├── repository/    Interfaces de Spring Data
│       ├── service/       Lógica de negocio
│       ├── dto/           Records de entrada/salida
│       ├── controller/    Endpoints REST
│       ├── security/      Login y configuración de Spring Security
│       ├── config/        CORS y cliente de IGDB
│       └── exception/     Manejo de errores
│
├── frontend/         SPA en React
│   └── src/
│       ├── api/            Cliente HTTP y funciones por endpoint
│       ├── auth/           Contexto de sesión
│       ├── components/     Piezas de presentación
│       └── pages/          Una por ruta
│
├── db/               Esquema SQL y datos de ejemplo
└── docs/FASES.md     Cómo fue el desarrollo, fase a fase
```

---

## Modelo de datos

| Tabla | Contenido |
|---|---|
| `platforms` | Consolas: nombre, abreviatura, fabricante, año |
| `games` | La obra: título, año, desarrolladora, distribuidora, sinopsis, URL de la portada, ID del trailer en YouTube, estado (pendiente/aprobado) |
| `editions` | Ejemplar en una plataforma: región, formato, si se posee (solo lo ve el admin) |
| `genres` | Catálogo de géneros |
| `game_genres` | Tabla puente N:M |
| `users` | Usuarios de la API (login) |

```
platforms 1───N editions N───1 games N───N genres
```

---

## API

Base: `/api`.

### Lectura (pública)

```
GET  /api/games                 Lista de juegos
GET  /api/games?title=zelda     Búsqueda por título
GET  /api/games/{id}            Ficha con ediciones

GET  /api/platforms
GET  /api/genres
GET  /api/editions?platformId=  ?gameId=  ?owned=true
```

Solo devuelve juegos aprobados (el admin puede abrir también la ficha de un pendiente). El campo `owned` (si tengo el juego o la edición) solo se rellena para el admin: para el resto llega a `null`, y el filtro `?owned=true` se ignora.

### Login

```
POST /api/auth/login    { username, password }
```

Si las credenciales son correctas, el backend abre una sesión (cookie) que el navegador manda automáticamente en las siguientes peticiones. No hace falta guardar ni mandar ningún token a mano.

### Escritura (solo admin)

```
POST/PUT/DELETE sobre /api/games, /api/platforms, /api/genres, /api/editions
PUT  /api/games/{id}/approve     Publica un juego pendiente
```

Solo `games` tiene panel en el frontend por ahora. `POST`/`PUT /api/games` aceptan las ediciones del juego en el mismo cuerpo, y la portada (`coverUrl`) tiene que ser una URL `https://`. El trailer (`trailerId`) admite el enlace de YouTube tal cual (`watch?v=`, `youtu.be/`, `shorts/`...) y se guarda solo el ID del vídeo.

### Cuenta DEMO

```
POST /api/auth/demo            Entra como DEMO, sin credenciales
POST /api/games                Propone un juego (queda pendiente; con cupo diario)
GET  /api/games/pending        Juegos pendientes de revisar (admin y DEMO)
GET  /api/games/demo-quota     Juegos que le quedan hoy a la cuenta DEMO
```

### Carátulas y trailers (admin y DEMO)

```
GET  /api/igdb/search?q=gran turismo     Hasta 10 resultados de IGDB
```

Devuelve, por cada juego, `igdbId`, `name`, `year`, `platforms`, `coverUrl`, `thumbUrl` y `videos` (nombre e ID de YouTube de cada vídeo). Aunque es un `GET`, exige sesión de admin o DEMO, porque cada búsqueda gasta cuota de la API de IGDB. El token de Twitch se guarda en memoria y se renueva antes de caducar.

### Códigos de respuesta

| Código | Significado |
|---|---|
| `200` / `201` / `204` | OK / creado / borrado |
| `400` | Validación fallida |
| `401` / `403` | Sin sesión / sin permiso |
| `404` | No existe |
| `409` | Conflicto con la base de datos (duplicado, FK en uso...) |
| `502` | IGDB no responde o falla (solo en la búsqueda de carátulas) |

---

## Poner en marcha en local

### Requisitos

- JDK 21
- Node.js 20+
- PostgreSQL

### Base de datos

```bash
createdb central_videogames
psql -d central_videogames -f db/schema.sql
psql -d central_videogames -f db/users.sql
```

`db/schema.sql` trae también el catálogo de ejemplo (los mismos juegos que se ven en la demo). `db/users.sql` crea un usuario `admin` con contraseña `changeme123`, cuya única función es rellenar el campo de contraseña al inicio, y que se recomienda cambiar (desde la propia app, en Ajustes).

### Backend

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/central_videogames
export SPRING_DATASOURCE_USERNAME=tu_usuario
export SPRING_DATASOURCE_PASSWORD=tu_contraseña

cd backend
./mvnw spring-boot:run
```

API en `http://localhost:8080`.

### Frontend

```bash
cd frontend
npm install
npm run dev
```

En `http://localhost:5173`. Ya trae un `.env.development` con `VITE_API_URL=http://localhost:8080`; solo hay que tocarlo si el backend corre en otro sitio.

### Variables de entorno

| Variable | Dónde | Para qué |
|---|---|---|
| `SPRING_DATASOURCE_URL/USERNAME/PASSWORD` | backend | Conexión a PostgreSQL |
| `APP_CORS_ALLOWED_ORIGINS` | backend | Orígenes permitidos por CORS |
| `SPRING_PROFILES_ACTIVE` | backend | `prod` en despliegue |
| `IGDB_CLIENT_ID` / `IGDB_CLIENT_SECRET` | backend | Credenciales de la app de Twitch para IGDB (opcionales: sin ellas todo funciona menos el buscador de carátulas) |
| `VITE_API_URL` | frontend | URL de la API |

---

## Despliegue

Tres servicios en Render: PostgreSQL gestionado, el backend como Web Service con Docker (perfil `prod`), y el frontend como Static Site.

Cosas problemáticas al principio:

- Render da la URL de conexión como `postgres://usuario:contraseña@host/base` y el driver JDBC quiere `jdbc:postgresql://host/base`, así que hay que separarlo en variables.
- El Static Site necesita una regla de *rewrite* `/*` → `/index.html` para que las rutas de React funcionen al entrar directamente (por ejemplo `/juegos/3`).
- Las credenciales de IGDB se sacan registrando una aplicación en la [consola de desarrolladores de Twitch](https://dev.twitch.tv/console/apps) y se cargan como variables de entorno del backend.

---

## Estado y siguientes pasos

Funcionando: catálogo completo con portadas, búsqueda, filtros, ficha de detalle con trailer, login, panel de administración para juegos, cuenta DEMO con revisión de pendientes y buscador de carátulas y trailers en IGDB.

Pendiente:

- Más tests (de momento cubren la integración con IGDB, la validación de portadas y trailers, los permisos de la cuenta DEMO y qué datos ve cada rol)
- Paginación en el listado
- Panel de administración para plataformas y géneros
- CI

Más detalle del recorrido en [`docs/FASES.md`](docs/FASES.md).

---

## Autoría

Proyecto personal de **Miguel Núñez**, hecho como pieza de portfolio tras el ciclo de DAW.

- LinkedIn: https://www.linkedin.com/in/miguel-n%C3%BA%C3%B1ez-4960aaa9/
- Correo: minunezme@gmail.com

## Licencia

MIT. Ver `LICENSE`.
