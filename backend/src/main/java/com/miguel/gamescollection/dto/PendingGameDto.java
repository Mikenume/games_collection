package com.miguel.gamescollection.dto;

import java.time.OffsetDateTime;
import java.util.List;

// Juego pendiente de revisión, con quién lo creó y cuándo
public record PendingGameDto(
        Integer id,
        String title,
        Short releaseYear,
        String developer,
        String publisher,
        List<String> genres,
        List<String> platforms,
        String createdBy,
        OffsetDateTime createdAt
) {
}
