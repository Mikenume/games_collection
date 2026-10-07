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
        String trailerId,
        OffsetDateTime createdAt,
        List<GenreDto> genres,
        List<EditionSummaryDto> editions
) {

    public GameDto withoutOwned() {
        List<EditionSummaryDto> publicEditions = editions == null
                ? null
                : editions.stream().map(EditionSummaryDto::withoutOwned).toList();
        return new GameDto(id, title, releaseYear, developer, publisher, synopsis, notes,
                editionType, coverUrl, trailerId, createdAt, genres, publicEditions);
    }
}
