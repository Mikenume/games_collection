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
- Ficha de detalle por juego, con sus ediciones (consola, año, región, formato...).
- Panel de administración (login + alta/edición/borrado de juegos) para el usuario admin.

La idea central del modelo es separar **juego** de **edición**: un juego es la obra (Resident Evil 4), y una edición es el ejemplar concreto en una plataforma (la de GameCube, la de PS2...). Así se pueden representar ports y multiplataforma sin repetir datos.

---

## Stack

- **Base de datos:** PostgreSQL
- **Backend:** Java 21, Spring Boot, Spring Data JPA, Spring Security, Maven
- **Frontend:** React, Vite, React Router, Bootstrap
- **Despliegue:** Docker (backend) + Render

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
│       ├── config/        CORS
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
| `games` | La obra: título, año, desarrolladora, distribuidora, sinopsis |
| `editions` | Ejemplar en una plataforma: región, formato, si se posee |
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

### Login

```
POST /api/auth/login    { username, password }
```

Si las credenciales son correctas, el backend abre una sesión (cookie) que el navegador manda automáticamente en las siguientes peticiones. No hace falta guardar ni mandar ningún token a mano.

### Escritura (solo admin)

```
POST/PUT/DELETE sobre /api/games, /api/platforms, /api/genres, /api/editions
```

Solo `games` tiene panel en el frontend por ahora.

### Códigos de respuesta

| Código | Significado |
|---|---|
| `200` / `201` / `204` | OK / creado / borrado |
| `400` | Validación fallida |
| `401` / `403` | Sin sesión / sin permiso |
| `404` | No existe |
| `409` | Conflicto con la base de datos (duplicado, FK en uso...) |

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

`db/schema.sql` trae también el catálogo de ejemplo (los mismos juegos que se ven en la demo). `db/users.sql` crea un usuario `admin` con contraseña `changeme123`. Cámbiala cuando puedas (desde la propia app, en Ajustes).

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
| `VITE_API_URL` | frontend | URL de la API |

---

## Despliegue

Tres servicios en Render: PostgreSQL gestionado, el backend como Web Service con Docker (perfil `prod`), y el frontend como Static Site.

Cosas que dieron guerra:

- Render da la URL de conexión como `postgres://usuario:contraseña@host/base` y el driver JDBC quiere `jdbc:postgresql://host/base`, así que hay que separarlo en variables.
- El Static Site necesita una regla de *rewrite* `/*` → `/index.html` para que las rutas de React funcionen al entrar directamente (por ejemplo `/juegos/3`).

---

## Estado y siguientes pasos

Funcionando: catálogo completo, búsqueda, filtros, ficha de detalle, login y panel de administración para juegos.

Pendiente:

- Tests
- Paginación en el listado
- Imágenes de portada
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
