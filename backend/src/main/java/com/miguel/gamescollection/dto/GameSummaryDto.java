package com.miguel.gamescollection.dto;

import java.util.List;

public record GameSummaryDto(
        Integer id,
        String title,
        Short releaseYear,
        String developer,
        String publisher,
        String editionType,
        String coverUrl,
        List<String> genres,
        List<String> platforms,
        Boolean owned
) {

    // Si tengo o no un juego solo lo ve el admin: para el resto va a null
    public GameSummaryDto withoutOwned() {
        return new GameSummaryDto(id, title, releaseYear, developer, publisher, editionType,
                coverUrl, genres, platforms, null);
    }
}
