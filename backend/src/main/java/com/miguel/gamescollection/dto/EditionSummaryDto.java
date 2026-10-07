package com.miguel.gamescollection.dto;

public record EditionSummaryDto(
        Integer id,
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

    public EditionSummaryDto withoutOwned() {
        return new EditionSummaryDto(id, platformId, platformName, platformAbbreviation,
                releaseYear, region, format, null, portDeveloper, notes);
    }
}
