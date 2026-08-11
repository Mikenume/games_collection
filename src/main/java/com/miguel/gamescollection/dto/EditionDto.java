package com.miguel.gamescollection.dto;

public record EditionDto(
        Integer id,
        Integer gameId,
        String gameTitle,
        Integer platformId,
        String platformName,
        String platformAbbreviation,
        Short releaseYear,
        String region,
        String format,
        Boolean owned,
        String portDeveloper,
        String notes
) {
}
