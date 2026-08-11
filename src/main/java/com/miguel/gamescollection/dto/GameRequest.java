package com.miguel.gamescollection.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record GameRequest(
        @NotBlank(message = "El título es obligatorio")
        @Size(max = 200, message = "El título no puede superar los 200 caracteres")
        String title,

        @Min(value = 1950, message = "El año debe ser 1950 o posterior")
        @Max(value = 2100, message = "El año debe ser 2100 o anterior")
        Short releaseYear,

        @Size(max = 120, message = "El desarrollador no puede superar los 120 caracteres")
        String developer,

        @Size(max = 120, message = "La distribuidora no puede superar los 120 caracteres")
        String publisher,

        String synopsis,

        String notes,

        @Pattern(regexp = "original|remake|remaster|port",
                message = "El tipo debe ser original, remake, remaster o port")
        String editionType,

        Set<Integer> genreIds
) {
}
