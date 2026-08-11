package com.miguel.gamescollection.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EditionRequest(
        @NotNull(message = "El id del juego es obligatorio")
        Integer gameId,

        @NotNull(message = "El id de la plataforma es obligatorio")
        Integer platformId,

        @Min(value = 1970, message = "El año debe ser 1970 o posterior")
        @Max(value = 2100, message = "El año debe ser 2100 o anterior")
        Short releaseYear,

        @Pattern(regexp = "PAL|NTSC-U|NTSC-J",
                message = "La región debe ser PAL, NTSC-U o NTSC-J")
        String region,

        @Pattern(regexp = "cartucho|CD|DVD|Blu-ray|BR|BD|tarjeta|digital",
                message = "Formato no válido")
        String format,

        Boolean owned,

        @Size(max = 120, message = "El port developer no puede superar los 120 caracteres")
        String portDeveloper,

        String notes
) {
}
