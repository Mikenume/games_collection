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

    public EditionDto withoutOwned() {
        return new EditionDto(id, gameId, gameTitle, platformId, platformName, platformAbbreviation,
                releaseYear, region, format, null, portDeveloper, notes);
    }
}
