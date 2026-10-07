package com.miguel.gamescollection.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.miguel.gamescollection.config.IgdbProperties;
import com.miguel.gamescollection.dto.IgdbGameDto;
import com.miguel.gamescollection.dto.IgdbGameResponse;
import com.miguel.gamescollection.exception.IgdbException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class IgdbService {

    private static final Logger log = LoggerFactory.getLogger(IgdbService.class);

    private static final String TOKEN_URL = "https://id.twitch.tv/oauth2/token";
    private static final String GAMES_URL = "https://api.igdb.com/v4/games";

    // El token se renueva un poco antes de que caduque, para no usarlo justo al límite
    private static final Duration TOKEN_MARGIN = Duration.ofSeconds(60);

    private final RestClient restClient;
    private final IgdbProperties properties;

    private String accessToken;
    private Instant tokenExpiresAt = Instant.EPOCH;

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record TokenResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("expires_in") long expiresIn
    ) {
    }

    public IgdbService(RestClient igdbRestClient, IgdbProperties properties) {
        this.restClient = igdbRestClient;
        this.properties = properties;
    }

    public List<IgdbGameDto> search(String title) {
        if (!properties.isConfigured()) {
            throw new IgdbException("La búsqueda en IGDB no está configurada en el servidor");
        }

        String query = buildQuery(title);
        try {
            return requestGames(query);
        } catch (HttpClientErrorException.Unauthorized ex) {
            // El token puede haberse revocado antes de tiempo: se pide otro y se reintenta una vez
            invalidateToken();
            try {
                return requestGames(query);
            } catch (RestClientException retryEx) {
                throw failure(retryEx);
            }
        } catch (RestClientException ex) {
            throw failure(ex);
        }
    }

    // Cuerpo de la consulta en el lenguaje de IGDB (Apicalypse)
    static String buildQuery(String title) {
        String escaped = title.trim()
                .replaceAll("[\\r\\n]+", " ")
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
        return "search \"" + escaped + "\"; "
                + "fields name, first_release_date, cover.image_id, platforms.name, videos.name, videos.video_id; "
                + "limit 10;";
    }

    private List<IgdbGameDto> requestGames(String query) {
        List<IgdbGameResponse> games = restClient.post()
                .uri(GAMES_URL)
                .header("Client-ID", properties.clientId())
                .header("Authorization", "Bearer " + token())
                .contentType(MediaType.TEXT_PLAIN)
                .accept(MediaType.APPLICATION_JSON)
                .body(query)
                .retrieve()
                .body(new ParameterizedTypeReference<List<IgdbGameResponse>>() {
                });

        return games == null ? List.of() : games.stream().map(IgdbGameDto::from).toList();
    }

    private synchronized String token() {
        if (accessToken == null || Instant.now().isAfter(tokenExpiresAt.minus(TOKEN_MARGIN))) {
            // Las credenciales van en el cuerpo y no en la URL, para que no acaben en ningún log
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("client_id", properties.clientId());
            form.add("client_secret", properties.clientSecret());
            form.add("grant_type", "client_credentials");

            TokenResponse response = restClient.post()
                    .uri(TOKEN_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(TokenResponse.class);

            if (response == null || response.accessToken() == null) {
                throw new IgdbException("Twitch no ha devuelto un token para IGDB");
            }
            accessToken = response.accessToken();
            tokenExpiresAt = Instant.now().plusSeconds(response.expiresIn());
        }
        return accessToken;
    }

    private synchronized void invalidateToken() {
        accessToken = null;
    }

    private IgdbException failure(RestClientException ex) {
        log.warn("Fallo al consultar IGDB: {}", ex.getMessage());
        return new IgdbException("No se ha podido consultar IGDB. Inténtalo de nuevo más tarde.", ex);
    }
}
