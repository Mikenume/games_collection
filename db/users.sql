-- Tabla de usuarios para el login

CREATE TABLE users (
    id       INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username VARCHAR(60) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role     VARCHAR(20) NOT NULL CHECK (role IN ('ROLE_ADMIN', 'ROLE_USER')),
    enabled  BOOLEAN NOT NULL DEFAULT TRUE
);

-- Usuario admin de arranque, contraseña "changeme123" cifrada con BCrypt
INSERT INTO users (username, password, role, enabled)
VALUES (
    'admin',
    '$2a$10$c4n8kkq1K0CYSO9eh8E/pufAMhx1v7RAbuUUViXPneioRZOQKfFT2',
    'ROLE_ADMIN',
    TRUE
);
