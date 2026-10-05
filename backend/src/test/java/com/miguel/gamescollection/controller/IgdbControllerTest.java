package com.miguel.gamescollection.controller;

import com.miguel.gamescollection.config.CorsConfig;
import com.miguel.gamescollection.dto.IgdbGameDto;
import com.miguel.gamescollection.exception.IgdbException;
import com.miguel.gamescollection.security.CustomUserDetailsService;
import com.miguel.gamescollection.security.SecurityConfig;
import com.miguel.gamescollection.service.IgdbService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IgdbController.class)
@Import({SecurityConfig.class, CorsConfig.class})
class IgdbControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private IgdbService igdbService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminPuedeBuscar() throws Exception {
        when(igdbService.search("crash")).thenReturn(List.of(new IgdbGameDto(
                1L, "Crash Bandicoot", 1996, List.of("PlayStation"),
                "https://images.igdb.com/igdb/image/upload/t_cover_big/co1.jpg",
                "https://images.igdb.com/igdb/image/upload/t_cover_small/co1.jpg")));

        mvc.perform(get("/api/igdb/search").param("q", "crash"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Crash Bandicoot"))
                .andExpect(jsonPath("$[0].year").value(1996))
                .andExpect(jsonPath("$[0].thumbUrl").value(
                        "https://images.igdb.com/igdb/image/upload/t_cover_small/co1.jpg"));
    }

    @Test
    void sinSesionNoSePuedeBuscar() throws Exception {
        mvc.perform(get("/api/igdb/search").param("q", "crash"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(igdbService);
    }

    @Test
    @WithMockUser(roles = "USER")
    void unUsuarioQueNoEsAdminTampoco() throws Exception {
        mvc.perform(get("/api/igdb/search").param("q", "crash"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(igdbService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void qVacioDevuelve400() throws Exception {
        mvc.perform(get("/api/igdb/search").param("q", "   "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Indica qué juego quieres buscar"));
        verifyNoInteractions(igdbService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void sinQDevuelve400() throws Exception {
        mvc.perform(get("/api/igdb/search"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Indica qué juego quieres buscar"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void siIgdbFallaDevuelve502SinTrazas() throws Exception {
        when(igdbService.search(anyString())).thenThrow(new IgdbException(
                "No se ha podido consultar IGDB. Inténtalo de nuevo más tarde.",
                new RuntimeException("connect timed out")));

        mvc.perform(get("/api/igdb/search").param("q", "crash"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.message").value(
                        "No se ha podido consultar IGDB. Inténtalo de nuevo más tarde."))
                .andExpect(jsonPath("$.trace").doesNotExist());
    }
}
