package com.miguel.gamescollection.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record GameDto(
        Integer id,
        String title,
        Short releaseYear,
        String developer,
        String publisher,
        String synopsis,
        String notes,
        String editionType,
        String coverUrl,
        OffsetDateTime createdAt,
        List<GenreDto> genres,
        List<EditionSummaryDto> editions
) {
}
