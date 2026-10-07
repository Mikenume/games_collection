package com.miguel.gamescollection.dto;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

// ID de un vídeo de YouTube: lo que se guarda como trailer de un juego
public final class YoutubeId {

    // 11 caracteres de [A-Za-z0-9_-]; lo comprueba @Pattern en GameRequest
    public static final String PATTERN = "[A-Za-z0-9_-]{11}";

    // youtube.com/watch?v=ID, youtu.be/ID, youtube.com/embed|shorts|live/ID…
    private static final Pattern URL = Pattern.compile(
            "^(?:https?://)?(?:www\\.|m\\.)?"
                    + "(?:youtu\\.be/|youtube(?:-nocookie)?\\.com/(?:watch\\?(?:\\S*&)?v=|embed/|shorts/|live/|v/))"
                    + "(" + PATTERN + ")(?:[?&#/]\\S*)?$");

    private YoutubeId() {
    }

    public static boolean isValid(String value) {
        return value != null && value.matches(PATTERN);
    }

    // Si es una URL de YouTube devuelve su ID; si no, la deja tal cual
    // para que la validación la rechace con su mensaje
    static String from(String value) {
        if (value == null) return null;
        Matcher matcher = URL.matcher(value);
        return matcher.matches() ? matcher.group(1) : value;
    }
}
