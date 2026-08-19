# Fases del proyecto

Notas de cómo fue montando **Central Videogames**, mi colección personal de videojuegos convertida en proyecto full-stack. Fui capa por capa: primero la base de datos, luego la API, luego el frontend.

---

## Fase 0 y 1 — Base de datos

Modelé el catálogo en PostgreSQL: `platforms`, `games`, `editions`, `genres` y la tabla puente `game_genres`.

La decisión más importante fue separar **juego** de **edición**. Al principio pensé en poner las plataformas como una columna en `games`, pero eso no deja usar claves foráneas ni guardar datos propios del ejemplar (región, formato...). Con una tabla `editions` aparte se resuelve mejor.

Usé `GENERATED ALWAYS AS IDENTITY` para las claves primarias, restricciones `CHECK` para los valores cerrados (región, formato...) y claves foráneas para la integridad. También hice unas vistas (`v_catalog`, `v_games`, `v_platform_stats`) para no repetir joins.

Cosas con las que me lié: confundir `SERIAL` con `IDENTITY`, y algún error de PostgreSQL raro (conectar a la base equivocada, sobre todo).

---

## Fase 2 — API con Spring Boot

Backend en Java 21 + Spring Boot, organizado en `model`, `repository`, `service`, `dto`, `controller`. Empecé por `Platform` y `Genre` como ejercicio para coger el patrón, y luego repetí lo mismo para `Game` y `Edition`.

Uso DTOs (`record`) para no exponer las entidades JPA tal cual en la mayoría de endpoints, aunque en `platforms` al final las devuelvo directamente porque no tienen relaciones y no vi la necesidad de duplicar la clase.

El `@RestControllerAdvice` traduce las excepciones más típicas (recurso no encontrado, sobre todo) a JSON en vez de dejar que se escape una traza de Java.

Cosas con las que me lié: la configuración de Maven en IntelliJ, y que Spring Boot 4 trae Jackson 3, así que alguna propiedad antigua de configuración de fechas ya no existe.

---

## Fase 3 — Frontend en React

Mi primer proyecto en React, viniendo de PHP y JS normal. SPA con Vite, React Router y Bootstrap.

- `api/client.js` centraliza las llamadas a `fetch()`.
- `auth/AuthContext.jsx` guarda el usuario logueado.
- Listado con búsqueda contra el servidor (con debounce) y filtros en el cliente.
- Ficha de detalle con su propia ruta.
- Cada juego se pinta como una "caja" con lomo de color según la consola.

Lo que más me costó fue el cambio de mentalidad respecto a JS vanilla: pensar en estado y en que el DOM se repinta solo, en vez de tocarlo yo a mano.

---

## Fase 4 — Despliegue

Repositorio en GitHub, Dockerfile para el backend, perfil `prod` con la configuración sensible en variables de entorno. Tres servicios en Render: base de datos, backend (Docker) y frontend (Static Site).

Lo que dio más guerra:

- La URL de conexión de Render viene como `postgres://usuario:contraseña@host/base` y el driver JDBC quiere `jdbc:postgresql://host/base`, hay que trocearla.
- El Static Site necesita una regla de *rewrite* para que las rutas de React no den 404 al entrar directamente.
- El plan gratuito tarda en arrancar si lleva un rato sin tráfico.

---

## Fase 5 — Documentación

README, este documento, captura de pantalla, licencia. Pendiente todavía limpiar del todo el histórico de git y pasar el repo a público.

---

## Fase 6 — Login y escritura

Objetivo: que se pueda consultar el catálogo sin loguearse, pero solo el admin pueda crear/editar/borrar.

Lo hice con Spring Security:

- Tabla `users` con username, contraseña con BCrypt y un rol.
- Login en `POST /api/auth/login`: si las credenciales son correctas, se abre una sesión y el navegador se encarga de mandar la cookie en las siguientes peticiones.
- Reglas de autorización: `GET` público, `POST/PUT/DELETE` solo para `ROLE_ADMIN`.
- CORS con `CorsConfigurationSource` en vez de la anotación `@CrossOrigin`, porque con Spring Security de por medio hay que registrarlo ahí para que las peticiones desde el frontend no se bloqueen antes de llegar al controlador.

El usuario admin de arranque tiene contraseña `changeme123` (hasheada en `db/users.sql`); hay que cambiarla en cuanto se tiene acceso.

---

## Ideas para más adelante

- Tests con `@SpringBootTest`
- Paginación en el listado de juegos
- Imágenes de portada
- Panel de estadísticas (la vista `v_platform_stats` ya existe y no se usa)
- CI con GitHub Actions
- Documentación de la API con Swagger
