package com.miguel.gamescollection.dto;

import java.time.OffsetDateTime;
import java.util.List;

// Juego pendiente de revisión, con quién lo creó y cuándo; carátula y
// trailer van incluidos para poder aprobarlo sin abrir el formulario
public record PendingGameDto(
        Integer id,
        String title,
        Short releaseYear,
        String developer,
        String publisher,
        String synopsis,
        String coverUrl,
        String trailerId,
        List<String> genres,
        List<String> platforms,
        List<String> formats,
        String createdBy,
        OffsetDateTime createdAt
) {
}
