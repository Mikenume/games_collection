package com.miguel.gamescollection.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YoutubeIdTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "dQw4w9WgXcQ",
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ&t=42s",
            "https://www.youtube.com/watch?feature=share&v=dQw4w9WgXcQ",
            "https://m.youtube.com/watch?v=dQw4w9WgXcQ",
            "youtube.com/watch?v=dQw4w9WgXcQ",
            "https://youtu.be/dQw4w9WgXcQ",
            "https://youtu.be/dQw4w9WgXcQ?si=abc123",
            "https://www.youtube.com/embed/dQw4w9WgXcQ",
            "https://www.youtube-nocookie.com/embed/dQw4w9WgXcQ",
            "https://www.youtube.com/shorts/dQw4w9WgXcQ"
    })
    void sacaElIdDeLasUrlsDeYoutube(String value) {
        assertEquals("dQw4w9WgXcQ", YoutubeId.from(value));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://vimeo.com/123456789",
            "https://evil.com/watch?v=dQw4w9WgXcQ",
            "https://www.youtube.com/watch?v=corto",
            "javascript:alert(1)"
    })
    void loQueNoEsDeYoutubeSeQuedaComoEstaYNoValida(String value) {
        assertEquals(value, YoutubeId.from(value));
        assertFalse(YoutubeId.isValid(YoutubeId.from(value)));
    }

    @Test
    void nullSigueSiendoNull() {
        assertNull(YoutubeId.from(null));
        assertFalse(YoutubeId.isValid(null));
        assertTrue(YoutubeId.isValid("a-b_c1234XZ"));
    }
}
