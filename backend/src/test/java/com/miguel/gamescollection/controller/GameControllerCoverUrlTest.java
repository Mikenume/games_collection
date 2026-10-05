package com.miguel.gamescollection.controller;

import com.miguel.gamescollection.config.CorsConfig;
import com.miguel.gamescollection.security.CustomUserDetailsService;
import com.miguel.gamescollection.security.SecurityConfig;
import com.miguel.gamescollection.service.GameService;
import org.junit.jupiter.api.Test;
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
        verify(gameService).create(any());
    }

    @Test
    void laPortadaEsOpcional() throws Exception {
        mvc.perform(put("/api/games/1").contentType(MediaType.APPLICATION_JSON).content(body(null)))
                .andExpect(status().isOk());
        verify(gameService).update(eq(1), any());
    }

    @Test
    void rechazaPortadaDeOtroDominio() throws Exception {
        mvc.perform(post("/api/games").contentType(MediaType.APPLICATION_JSON)
                        .content(body("https://example.com/cover.jpg")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "La portada tiene que ser una imagen de https://images.igdb.com/"));
        verifyNoInteractions(gameService);
    }

    @Test
    void rechazaDominioQueSoloEmpiezaIgual() throws Exception {
        // images.igdb.com.evil.net no es IGDB aunque empiece por el mismo texto
        mvc.perform(put("/api/games/1").contentType(MediaType.APPLICATION_JSON)
                        .content(body("https://images.igdb.com.evil.net/cover.jpg")))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(gameService);
    }
}
