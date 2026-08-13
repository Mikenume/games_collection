# Central Videogames

Catálogo web de una colección personal de videojuegos: base de datos relacional en PostgreSQL, API REST en Spring Boot y frontend en React. Proyecto full-stack construido de cero y desplegado en producción.

**Demo:** https://central-videogames.onrender.com
**API:** https://central-videogames-api.onrender.com/api/games


![Captura de la aplicación](docs/captura_index.png)

> El backend está alojado en Render (región Frankfurt). La primera carga puede tardar unos segundos.

---

## Índice

1. [Qué hace](#qué-hace)
2. [Stack](#stack)
3. [Estructura del repositorio](#estructura-del-repositorio)
4. [Modelo de datos](#modelo-de-datos)
5. [API](#api)
6. [Puesta en marcha en local](#puesta-en-marcha-en-local)
7. [Despliegue](#despliegue)
8. [Decisiones técnicas](#decisiones-técnicas)
9. [Estado y hoja de ruta](#estado-y-hoja-de-ruta)
10. [Autoría](#autoría)

---

## Qué hace

- **Catálogo navegable** de juegos con su portada-caja, plataformas y géneros.
- **Búsqueda por título** contra el servidor, con *debounce* para no saturar la API.
- **Filtros** por plataforma y género en el cliente.
- **Ficha de detalle** por juego, con sus ediciones (consola, año, región, formato, desarrollador del port y notas).
- **Rutas profundas**: cada juego tiene su propia URL compartible (`/juegos/{id}`).
- **Panel de administración** (en desarrollo): alta, edición y borrado de juegos restringido a usuarios con rol de administrador.

La distinción central del modelo es entre **juego** y **edición**: un juego es la obra (*Resident Evil 4*), y una edición es un ejemplar concreto en una plataforma concreta (la de GameCube, la de PS2, la de Switch). Eso permite representar ports y multiplataforma sin duplicar información.

---

## Stack

| Capa | Tecnología |
|---|---|
| Base de datos | PostgreSQL 18 |
| Backend | Java 21 (LTS) · Spring Boot 4.1 · Spring Data JPA / Hibernate · Maven |
| Frontend | React 18 · Vite · React Router · Bootstrap 5 |
| Contenedores | Docker (multi-stage build para el backend) |
| Hosting | Render (PostgreSQL gestionado + Web Service + Static Site) |

---

## Estructura del repositorio

Monorepo con los dos proyectos independientes:

```
games_collection/
├── backend/                     API REST en Spring Boot
│   ├── src/main/java/com/miguel/gamescollection/
│   │   ├── model/               Entidades JPA (Game, Edition, Platform, Genre)
│   │   ├── repository/          Interfaces de Spring Data
│   │   ├── service/             Lógica de negocio y @Transactional
│   │   ├── dto/                 Records de entrada y salida
│   │   ├── controller/          Endpoints REST
│   │   ├── config/              CORS y configuración de la aplicación
│   │   └── exception/           GlobalExceptionHandler (@RestControllerAdvice)
│   ├── src/main/resources/
│   │   ├── application.properties
│   │   └── application-prod.properties
│   ├── Dockerfile
│   └── pom.xml
│
├── frontend/                    SPA en React
│   ├── src/
│   │   ├── api/                 Cliente HTTP y funciones por endpoint
│   │   ├── auth/                Contexto de sesión (token, usuario, rol)
│   │   ├── components/          Piezas de presentación (reciben props)
│   │   ├── pages/               Una por ruta; aquí vive el estado
│   │   ├── App.jsx              Tabla de rutas
│   │   └── main.jsx             Punto de entrada
│   ├── .env.example
│   └── package.json
│
├── db/                          Esquema y datos de ejemplo
│   └── schema.sql
│
├── docs/
│   └── FASES.md                 Fases del desarrollo y decisiones
│
└── README.md
```

**Regla de organización del frontend:** los componentes de `components/` no llaman a la API; reciben datos por props. Las páginas de `pages/` son las únicas que cargan datos y guardan estado. Todas las llamadas HTTP pasan por `api/client.js`, así que un cambio en la API se absorbe en un único sitio.

---

## Modelo de datos

Cinco tablas y tres vistas:

| Tabla | Contenido |
|---|---|
| `platforms` | Consolas: nombre, abreviatura, fabricante, año |
| `games` | La obra: título, año, desarrolladora, distribuidora, sinopsis |
| `editions` | Ejemplar en una plataforma: región, formato, si se posee, notas |
| `genres` | Catálogo de géneros |
| `game_genres` | Tabla puente N:M entre juegos y géneros |

| Vista | Para qué |
|---|---|
| `v_catalog` | Una fila por edición, con juego y plataforma resueltos |
| `v_games` | Una fila por juego, con plataformas y géneros agregados en arrays |
| `v_platform_stats` | Recuentos por consola, para el panel de estadísticas |

Relaciones:

```
platforms 1 ──── N editions N ──── 1 games N ──── N genres
                                        (vía game_genres)
```

La integridad se defiende en la base de datos, no solo en Java: claves foráneas, restricciones `CHECK` sobre región, formato y tipo de edición, y `UNIQUE` donde corresponde. El backend arranca con `spring.jpa.hibernate.ddl-auto=validate`, de modo que Hibernate nunca toca el esquema: si una entidad no cuadra con la tabla real, la aplicación falla al arrancar en lugar de corromper nada.

El esquema completo está en `db/schema.sql`.

---

## API

Base: `/api`. Todas las respuestas en JSON.

### Lectura (pública)

```
GET  /api/games                    Lista de juegos con géneros y plataformas
GET  /api/games?title=zelda        Búsqueda por título (contiene, sin distinguir mayúsculas)
GET  /api/games/{id}               Ficha detallada con ediciones

GET  /api/platforms                Lista de plataformas
GET  /api/platforms/{id}

GET  /api/genres                   Lista de géneros
GET  /api/genres/{id}

GET  /api/editions                 Lista de ediciones
GET  /api/editions?platformId=3    Filtrar por consola
GET  /api/editions?gameId=42       Filtrar por juego
GET  /api/editions?owned=true      Solo las que se poseen
GET  /api/editions/{id}
```

### Escritura (pendiente — requerirá rol de administrador)

```
POST   /api/auth/login             Devuelve { token, username, roles }
POST   /api/games
PUT    /api/games/{id}
DELETE /api/games/{id}
```

### Códigos de respuesta

| Código | Significado |
|---|---|
| `200` | OK |
| `201` | Recurso creado |
| `204` | Borrado, sin contenido |
| `400` | Validación fallida (incluye el detalle por campo) |
| `401` / `403` | Sin autenticar / sin permisos |
| `404` | El recurso no existe |
| `409` | Conflicto con la base de datos: duplicado, FK en uso o `CHECK` incumplido |

Los errores se devuelven con un cuerpo uniforme y sin trazas de Java, gracias a un `@RestControllerAdvice` centralizado.

---

## Puesta en marcha en local

### Requisitos

- JDK 21
- Node.js 20 o superior
- PostgreSQL 16 o superior

### 1. Base de datos

```bash
createdb central_videogames
psql -d central_videogames -f db/schema.sql
```

### 2. Backend

Las credenciales **no** están en el repositorio. Defínelas como variables de entorno:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/central_videogames
export DB_USERNAME=tu_usuario
export DB_PASSWORD=tu_contraseña
```

En Windows (PowerShell):

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/central_videogames"
$env:DB_USERNAME="tu_usuario"
$env:DB_PASSWORD="tu_contraseña"
```

Arrancar:

```bash
cd backend
./mvnw spring-boot:run
```

La API queda en `http://localhost:8080`. Comprobación rápida: `http://localhost:8080/api/games`.

### 3. Frontend

```bash
cd frontend
cp .env.example .env.development   # ajusta VITE_API_URL si hace falta
npm install
npm run dev
```

Abre `http://localhost:5173`.

### Variables de entorno

**Backend**

| Variable | Descripción |
|---|---|
| `DB_URL` | URL JDBC de PostgreSQL |
| `DB_USERNAME` | Usuario de la base de datos |
| `DB_PASSWORD` | Contraseña |
| `APP_CORS_ALLOWED_ORIGINS` | Orígenes permitidos, separados por coma |
| `SPRING_PROFILES_ACTIVE` | `prod` en despliegue |

**Frontend**

| Variable | Descripción |
|---|---|
| `VITE_API_URL` | URL base de la API, **sin** barra final |

Vite solo expone al navegador las variables con prefijo `VITE_`. Nunca pongas ahí un secreto: acaba en el bundle.

---

## Despliegue

Tres servicios en Render, todos en la región Frankfurt:

| Servicio | Tipo | Configuración |
|---|---|---|
| Base de datos | PostgreSQL gestionado | — |
| `central-videogames-api` | Web Service (Docker) | Root directory `backend`, perfil `prod` |
| `central-videogames` | Static Site | Root directory `frontend`, build `npm ci && npm run build`, publish `dist` |

Dos detalles que dan problemas y conviene tener presentes:

**Transformación de la URL de conexión.** Render entrega la cadena en formato `postgres://usuario:contraseña@host/base`, que el driver JDBC no acepta. Hay que convertirla a `jdbc:postgresql://host/base` y llevar usuario y contraseña a sus propias variables. Se usa la *Internal Database URL*, para que el tráfico no salga a internet.

**Rewrite para el enrutado del cliente.** Al ser una SPA, el Static Site necesita una regla `/*` → `/index.html` de tipo *Rewrite*. Sin ella la portada carga, pero entrar directamente en `/juegos/3` devuelve 404.

---

## Decisiones técnicas

**Separación juego / edición.** Podría haberse resuelto con una columna multivalor de plataformas en `games`, pero eso impide usar claves foráneas y obliga a parsear cadenas. La tabla intermedia `editions` permite además guardar datos propios del ejemplar: región, formato, desarrollador del port.

**DTOs como `record`, entidades hacia dentro.** Las entidades JPA no salen nunca del backend. Los controladores hablan con DTOs inmutables, lo que evita exponer el modelo interno, filtrar relaciones perezosas sin querer y acoplar el JSON al esquema.

**`@EntityGraph` en `GameRepository`.** El listado de juegos necesita géneros, ediciones y plataformas. Sin él, cada juego dispara consultas adicionales (problema N+1). Con él, un único `SELECT` con sus `LEFT JOIN`.

**`spring.jpa.open-in-view=false`.** Con el valor por defecto, las relaciones perezosas se pueden cargar fuera de la transacción sin avisar, disparando consultas silenciosas desde la capa de presentación. Desactivarlo hace que ese error salte pronto y de forma clara.

**Región, formato y tipo de edición como `String`, no como `enum`.** Los `CHECK` de PostgreSQL usan valores con mayúsculas mezcladas y guiones, incompatibles con la serialización por defecto de los enums de Java. Mapearlos como cadena mantiene la base de datos como única fuente de verdad sobre los valores válidos.

**Validación en dos capas.** `@Valid` y anotaciones de Bean Validation en la entrada de la API, y `CHECK`/`FK`/`UNIQUE` en la base de datos. La primera da mensajes útiles por campo; la segunda garantiza que nada incorrecto entra, venga por donde venga.

**Contexto de sesión en React.** El token y el usuario viven en un `AuthContext` en lugar de pasarse por props a través de media aplicación. `api/client.js` lo lee y lo adjunta como cabecera `Authorization: Bearer`.

---

## Estado y hoja de ruta

Funcionando en producción: base de datos, API de lectura, frontend completo y despliegue.

En desarrollo:

- [ ] Spring Security con JWT: lectura pública, escritura restringida a rol de administrador
- [ ] Endpoints `POST` / `PUT` / `DELETE` sobre `/api/games`
- [ ] Panel de administración conectado (el formulario ya está construido en el frontend)

Previsto:

- [ ] Tests de integración de los controladores con `@SpringBootTest` y Testcontainers
- [ ] Paginación en el listado de juegos
- [ ] Imágenes de portada
- [ ] Panel de estadísticas sobre `v_platform_stats`
- [ ] Pipeline de CI en GitHub Actions

El recorrido completo del proyecto, fase a fase, está en [`docs/FASES.md`](docs/FASES.md).

---

## Autoría

Proyecto personal de **Miguel Núñez**, desarrollado como pieza de portfolio tras finalizar el ciclo de Desarrollo de Aplicaciones Web.


- LinkedIn: https://www.linkedin.com/in/miguel-n%C3%BA%C3%B1ez-4960aaa9/
- Correo: minunezme@gmail.com



## Licencia
Distribuido bajo licencia MIT. Ver `LICENSE`.

