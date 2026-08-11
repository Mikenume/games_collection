package com.miguel.gamescollection.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PlatformRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 60, message = "El nombre no puede superar los 60 caracteres")
        String name,

        @NotBlank(message = "La abreviatura es obligatoria")
        @Size(max = 10, message = "La abreviatura no puede superar los 10 caracteres")
        String abbreviation,

        @NotBlank(message = "El fabricante es obligatorio")
        @Size(max = 60, message = "El fabricante no puede superar los 60 caracteres")
        String manufacturer,

        @Min(value = 1950, message = "El año debe ser 1950 o posterior")
        @Max(value = 2100, message = "El año debe ser 2100 o anterior")
        Short releaseYear
) {
}
