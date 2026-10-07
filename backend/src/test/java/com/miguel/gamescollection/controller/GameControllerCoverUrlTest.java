package com.miguel.gamescollection.controller;

import com.miguel.gamescollection.config.CorsConfig;
import com.miguel.gamescollection.security.CustomUserDetailsService;
import com.miguel.gamescollection.security.SecurityConfig;
import com.miguel.gamescollection.service.GameService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GameController.class)
@Import({SecurityConfig.class, CorsConfig.class})
@WithMockUser(roles = "ADMIN")
class GameControllerCoverUrlTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private GameService gameService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    private static String body(String coverUrl) {
        String cover = coverUrl == null ? "null" : "\"" + coverUrl + "\"";
        return "{\"title\": \"Crash Bandicoot\", \"coverUrl\": " + cover + "}";
    }

    @Test
    void aceptaPortadaDeIgdb() throws Exception {
        mvc.perform(post("/api/games").contentType(MediaType.APPLICATION_JSON)
                        .content(body("https://images.igdb.com/igdb/image/upload/t_cover_big/co1.jpg")))
                .andExpect(status().isCreated());
        verify(gameService).create(any(), any());
    }

    @Test
    void laPortadaEsOpcional() throws Exception {
        mvc.perform(put("/api/games/1").contentType(MediaType.APPLICATION_JSON).content(body(null)))
                .andExpect(status().isOk());
        verify(gameService).update(eq(1), any());
    }

    @Test
    void aceptaUrlManualDeOtraWeb() throws Exception {
        // Carátulas que IGDB no tiene (p. ej. la PAL) se pegan a mano desde otra web
        mvc.perform(post("/api/games").contentType(MediaType.APPLICATION_JSON)
                        .content(body("https://example.com/covers/gran-turismo-2-pal.jpg")))
                .andExpect(status().isCreated());
        verify(gameService).create(any(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "http://example.com/cover.jpg",
            "javascript:alert(1)",
            "data:image/png;base64,AAAA",
            "https://example.com/mi portada.jpg",
            "https://localhost/cover.jpg",
            "example.com/cover.jpg"
    })
    void rechazaUrlsQueNoSonHttps(String coverUrl) throws Exception {
        mvc.perform(put("/api/games/1").contentType(MediaType.APPLICATION_JSON)
                        .content(body(coverUrl)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "La portada tiene que ser una URL que empiece por https://"));
        verifyNoInteractions(gameService);
    }

    @Test
    void rechazaUrlDemasiadoLarga() throws Exception {
        String longUrl = "https://example.com/" + "a".repeat(250) + ".jpg";
        mvc.perform(post("/api/games").contentType(MediaType.APPLICATION_JSON).content(body(longUrl)))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(gameService);
    }
}
