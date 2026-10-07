package com.miguel.gamescollection.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;
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

        @Size(max = 4000, message = "La sinopsis no puede superar los 4000 caracteres")
        String synopsis,

        @Size(max = 2000, message = "Las notas no pueden superar los 2000 caracteres")
        String notes,

        @Pattern(regexp = "original|remake|remaster|port",
                message = "El tipo debe ser original, remake, remaster o port")
        String editionType,

        @Size(max = 20, message = "Como máximo 20 géneros")
        Set<@NotNull(message = "Hay un género sin id") Integer> genreIds,

        // Suele venir de IGDB, pero se puede pegar una URL a mano cuando IGDB
        // no tiene la carátula que quiere (p. ej. la europea). Solo https, sin espacios.
        @Size(max = 255, message = "La URL de la portada no puede superar los 255 caracteres")
        @Pattern(regexp = "https://[^\\s/]+\\.[^\\s/]+/\\S*",
                message = "La portada tiene que ser una URL que empiece por https://")
        String coverUrl,

        // null deja las ediciones como están; una lista (aunque esté vacía)
        // las sustituye: crea las nuevas, actualiza las que traen id y borra el resto
        @Size(max = 30, message = "Como máximo 30 ediciones")
        List<@NotNull(message = "Hay una edición vacía") @Valid GameEditionRequest> editions
) {
    public GameRequest {
        title = TextInput.clean(title);
        developer = TextInput.clean(developer);
        publisher = TextInput.clean(publisher);
        synopsis = TextInput.clean(synopsis);
        notes = TextInput.clean(notes);
        editionType = TextInput.clean(editionType);
        coverUrl = TextInput.clean(coverUrl);
    }
}
