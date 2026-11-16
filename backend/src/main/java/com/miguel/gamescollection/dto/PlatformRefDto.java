package com.miguel.gamescollection.dto;

// Plataforma tal y como va dentro de un juego del listado: lo justo para
// pintar el nombre completo y el fabricante sin pedir /api/platforms
public record PlatformRefDto(
        String abbreviation,
        String name,
        String manufacturer,
        Short releaseYear
) {
}
