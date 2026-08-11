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
}
