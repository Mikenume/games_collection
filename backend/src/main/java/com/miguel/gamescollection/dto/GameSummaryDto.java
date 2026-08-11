package com.miguel.gamescollection.dto;

import java.util.List;

public record GameSummaryDto(
        Integer id,
        String title,
        Short releaseYear,
        String developer,
        String publisher,
        String editionType,
        List<String> genres,
        List<String> platforms,
        boolean owned
) {
}
