package com.miguel.gamescollection.controller;

import com.miguel.gamescollection.config.CorsConfig;
import com.miguel.gamescollection.dto.GameRequest;
import com.miguel.gamescollection.security.CustomUserDetailsService;
import com.miguel.gamescollection.security.SecurityConfig;
import com.miguel.gamescollection.service.GameService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GameController.class)
@Import({SecurityConfig.class, CorsConfig.class})
@WithMockUser(roles = "ADMIN")
class GameControllerTrailerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private GameService gameService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    private static String body(String trailerId) {
        String trailer = trailerId == null ? "null" : "\"" + trailerId + "\"";
        return "{\"title\": \"Crash Bandicoot\", \"trailerId\": " + trailer + "}";
    }

    private GameRequest sentToService() {
        ArgumentCaptor<GameRequest> captor = ArgumentCaptor.forClass(GameRequest.class);
        verify(gameService).update(eq(1), captor.capture());
        return captor.getValue();
    }

    @Test
    void deLaUrlPegadaSeGuardaSoloElId() throws Exception {
        mvc.perform(put("/api/games/1").contentType(MediaType.APPLICATION_JSON)
                        .content(body("https://www.youtube.com/watch?v=dQw4w9WgXcQ&t=10s")))
                .andExpect(status().isOk());
        assertEquals("dQw4w9WgXcQ", sentToService().trailerId());
    }

    @Test
    void elTrailerEsOpcional() throws Exception {
        mvc.perform(put("/api/games/1").contentType(MediaType.APPLICATION_JSON).content(body("  ")))
                .andExpect(status().isOk());
        assertNull(sentToService().trailerId());
    }

    @Test
    void rechazaLoQueNoEsDeYoutube() throws Exception {
        mvc.perform(put("/api/games/1").contentType(MediaType.APPLICATION_JSON)
                        .content(body("https://vimeo.com/123456789")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "El trailer tiene que ser un enlace de YouTube o el ID de un vídeo"));
        verifyNoInteractions(gameService);
    }
}
