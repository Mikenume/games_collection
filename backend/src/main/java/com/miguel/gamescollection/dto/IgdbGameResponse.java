package com.miguel.gamescollection.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

// Respuesta en bruto de POST /v4/games, con los campos que se piden en la consulta
@JsonIgnoreProperties(ignoreUnknown = true)
public record IgdbGameResponse(
        Long id,
        String name,
        @JsonProperty("first_release_date") Long firstReleaseDate,
        Cover cover,
        List<PlatformRef> platforms
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Cover(@JsonProperty("image_id") String imageId) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PlatformRef(String name) {
    }
}
