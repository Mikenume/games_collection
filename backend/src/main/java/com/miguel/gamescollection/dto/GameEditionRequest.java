package com.miguel.gamescollection.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Edición enviada dentro de un GameRequest. Sin id se crea; con id se actualiza.
public record GameEditionRequest(
        Integer id,

        @NotNull(message = "La plataforma de la edición es obligatoria")
        Integer platformId,

        @Min(value = 1970, message = "El año de la edición debe ser 1970 o posterior")
        @Max(value = 2100, message = "El año de la edición debe ser 2100 o anterior")
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

        @Size(max = 1000, message = "Las notas de la edición no pueden superar los 1000 caracteres")
        String notes
) {
    public GameEditionRequest {
        region = TextInput.clean(region);
        format = TextInput.clean(format);
        portDeveloper = TextInput.clean(portDeveloper);
        notes = TextInput.clean(notes);
    }
}
