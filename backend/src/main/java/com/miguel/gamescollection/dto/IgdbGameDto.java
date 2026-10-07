package com.miguel.gamescollection.dto;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;

public record IgdbGameDto(
        Long igdbId,
        String name,
        Integer year,
        List<String> platforms,
        String coverUrl,
        String thumbUrl,
        List<Video> videos
) {

    public record Video(String name, String youtubeId) {
    }

    private static final String IMAGE_BASE = "https://images.igdb.com/igdb/image/upload/";

    public static IgdbGameDto from(IgdbGameResponse game) {
        // first_release_date es un timestamp Unix en segundos; el año se toma en UTC
        Integer year = game.firstReleaseDate() == null
                ? null
                : Instant.ofEpochSecond(game.firstReleaseDate()).atZone(ZoneOffset.UTC).getYear();

        List<String> platforms = game.platforms() == null
                ? List.of()
                : game.platforms().stream()
                        .map(IgdbGameResponse.PlatformRef::name)
                        .filter(Objects::nonNull)
                        .toList();

        List<Video> videos = game.videos() == null
                ? List.of()
                : game.videos().stream()
                        .filter(v -> YoutubeId.isValid(v.videoId()))
                        .map(v -> new Video(v.name(), v.videoId()))
                        .toList();

        String imageId = game.cover() == null ? null : game.cover().imageId();
        boolean hasCover = imageId != null && !imageId.isBlank();

        return new IgdbGameDto(
                game.id(),
                game.name(),
                year,
                platforms,
                hasCover ? IMAGE_BASE + "t_cover_big/" + imageId + ".jpg" : null,
                hasCover ? IMAGE_BASE + "t_cover_small/" + imageId + ".jpg" : null,
                videos
        );
    }
}
