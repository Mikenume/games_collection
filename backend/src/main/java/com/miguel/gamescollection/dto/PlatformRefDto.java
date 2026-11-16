package com.miguel.gamescollection.dto;

// Plataforma tal y como va dentro de un juego del listado: lo justo para
// pintar el nombre completo y ordenar por año sin pedir /api/platforms
public record PlatformRefDto(
        String abbreviation,
        String name,
        Short releaseYear
) {
}
