-- Tabla de usuarios para el login (se ejecuta después de schema.sql)

-- El rol se guarda sin el prefijo ROLE_: lo añade el backend.
-- DEMO es la cuenta compartida del botón "Probar como demo"; el backend
-- la crea sola la primera vez que alguien entra, no hace falta insertarla.
CREATE TABLE users (
    id       INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username VARCHAR(60) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role     VARCHAR(20) NOT NULL,
    enabled  BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_usuario_rol CHECK (role IN ('ADMIN', 'DEMO', 'USER'))
);

-- Usuario admin de arranque, contraseña "changeme123" cifrada con BCrypt
INSERT INTO users (username, password, role, enabled)
VALUES (
    'admin',
    '$2a$10$c4n8kkq1K0CYSO9eh8E/pufAMhx1v7RAbuUUViXPneioRZOQKfFT2',
    'ADMIN',
    TRUE
);

-- Quién creó cada juego. La clave foránea va aquí y no en schema.sql
-- porque la tabla users no existe hasta este script. ON DELETE SET NULL:
-- si se borra un usuario, sus juegos se quedan.
ALTER TABLE games
    ADD CONSTRAINT games_created_by_id_fkey
        FOREIGN KEY (created_by_id) REFERENCES users(id) ON DELETE SET NULL;

-- Los juegos de ejemplo de schema.sql son del admin
UPDATE games
SET created_by_id = (SELECT id FROM users WHERE role = 'ADMIN' ORDER BY id LIMIT 1)
WHERE created_by_id IS NULL;
