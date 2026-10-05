package com.miguel.gamescollection.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

// Credenciales de la app de Twitch con la que se accede a IGDB.
// Llegan por variables de entorno (ver application.properties).
@ConfigurationProperties("igdb")
public record IgdbProperties(String clientId, String clientSecret) {

    public boolean isConfigured() {
        return clientId != null && !clientId.isBlank()
                && clientSecret != null && !clientSecret.isBlank();
    }
}
