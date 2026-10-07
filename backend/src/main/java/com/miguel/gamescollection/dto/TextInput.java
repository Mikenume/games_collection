package com.miguel.gamescollection.dto;

// Limpieza de los textos que llegan en los requests
final class TextInput {

    private TextInput() {
    }

    // Quita espacios al principio y al final; si no queda nada, null.
    // Así "   " no pasa un @NotBlank ni se guarda como texto vacío.
    static String clean(String value) {
        if (value == null) return null;
        String trimmed = value.strip();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
