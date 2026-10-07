package com.miguel.gamescollection.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IgdbGameDtoTest {

    @Test
    void mapeaAnioPlataformasYUrlsDeLaPortada() {
        // 1996-09-09T00:00:00Z, lanzamiento de Crash Bandicoot en Norteamérica
        IgdbGameResponse game = new IgdbGameResponse(
                1234L,
                "Crash Bandicoot",
                842227200L,
                new IgdbGameResponse.Cover("co1abc"),
                List.of(new IgdbGameResponse.PlatformRef("PlayStation")),
                null
        );

        IgdbGameDto dto = IgdbGameDto.from(game);

        assertEquals(1234L, dto.igdbId());
        assertEquals("Crash Bandicoot", dto.name());
        assertEquals(1996, dto.year());
        assertEquals(List.of("PlayStation"), dto.platforms());
        assertEquals("https://images.igdb.com/igdb/image/upload/t_cover_big/co1abc.jpg", dto.coverUrl());
        assertEquals("https://images.igdb.com/igdb/image/upload/t_cover_small/co1abc.jpg", dto.thumbUrl());
    }

    @Test
    void elAnioSeCalculaEnUtc() {
        // 1999-12-31T23:30:00Z: en zonas al este de UTC ya sería 2000
        IgdbGameResponse game = new IgdbGameResponse(1L, "X", 946683000L, null, null, null);

        assertEquals(1999, IgdbGameDto.from(game).year());
    }

    @Test
    void sinPortadaNiFechaNiPlataformas() {
        IgdbGameResponse game = new IgdbGameResponse(1L, "Sin datos", null, null, null, null);

        IgdbGameDto dto = IgdbGameDto.from(game);

        assertNull(dto.year());
        assertNull(dto.coverUrl());
        assertNull(dto.thumbUrl());
        assertTrue(dto.platforms().isEmpty());
        assertTrue(dto.videos().isEmpty());
    }

    @Test
    void portadaSinImageIdCuentaComoSinPortada() {
        IgdbGameResponse game = new IgdbGameResponse(
                1L, "X", null, new IgdbGameResponse.Cover(null), List.of(), null);

        IgdbGameDto dto = IgdbGameDto.from(game);

        assertNull(dto.coverUrl());
        assertNull(dto.thumbUrl());
    }

    @Test
    void videosDeYoutubeDescartandoLosIdsRaros() {
        IgdbGameResponse game = new IgdbGameResponse(1L, "Crash Bandicoot", null, null, null, List.of(
                new IgdbGameResponse.Video("Trailer", "dQw4w9WgXcQ"),
                new IgdbGameResponse.Video("Sin id", null),
                new IgdbGameResponse.Video("Id raro", "no es un id")));

        assertEquals(List.of(new IgdbGameDto.Video("Trailer", "dQw4w9WgXcQ")), IgdbGameDto.from(game).videos());
    }
}
