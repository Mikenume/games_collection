package com.miguel.gamescollection.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.ZoneId;

// Configuración de la cuenta DEMO (ver application.properties).
// username: nombre del usuario demo; se crea solo la primera vez que alguien entra.
// dailyLimit: juegos que pueden crear entre todas las cuentas DEMO en un día.
// zone: zona horaria en la que empieza "hoy" (el servidor de Render va en UTC).
@ConfigurationProperties("app.demo")
public record DemoProperties(String username, int dailyLimit, ZoneId zone) {
}
