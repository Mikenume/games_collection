package com.miguel.gamescollection.dto;

public record PlatformDto(
        Integer id,
        String name,
        String abbreviation,
        String manufacturer,
        Short releaseYear
) {
}
