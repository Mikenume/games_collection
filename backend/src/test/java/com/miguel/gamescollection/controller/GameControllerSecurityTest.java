package com.miguel.gamescollection.controller;

import com.miguel.gamescollection.config.CorsConfig;
import com.miguel.gamescollection.model.Role;
import com.miguel.gamescollection.security.CustomUserDetailsService;
import com.miguel.gamescollection.security.SecurityConfig;
import com.miguel.gamescollection.security.UserPrincipal;
import com.miguel.gamescollection.service.GameService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.verify;

// Reglas de SecurityConfig para la cuenta DEMO y para quien no ha entrado
@WebMvcTest(GameController.class)
@Import({SecurityConfig.class, CorsConfig.class})
class GameControllerSecurityTest {

    private static final UserPrincipal DEMO = new UserPrincipal(2, "demo", null, Role.DEMO, true);
    private static final String BODY = "{\"title\": \"Spyro the Dragon\"}";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private GameService gameService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @Test
    void demoPuedeCrear() throws Exception {
        mvc.perform(post("/api/games").with(user(DEMO))
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated());
        verify(gameService).create(any(), any());
    }

    @Test
    void demoNoPuedeEditar() throws Exception {
        mvc.perform(put("/api/games/1").with(user(DEMO))
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isForbidden());
        verifyNoInteractions(gameService);
    }

    @Test
    void demoNoPuedeBorrar() throws Exception {
        mvc.perform(delete("/api/games/1").with(user(DEMO)))
                .andExpect(status().isForbidden());
        verifyNoInteractions(gameService);
    }

    @Test
    void demoNoPuedeAprobar() throws Exception {
        mvc.perform(put("/api/games/1/approve").with(user(DEMO)))
                .andExpect(status().isForbidden());
        verifyNoInteractions(gameService);
    }

    @Test
    void demoVeLosPendientes() throws Exception {
        mvc.perform(get("/api/games/pending").with(user(DEMO)))
                .andExpect(status().isOk());
    }

    @Test
    void sinSesionNoSeVenLosPendientes() throws Exception {
        mvc.perform(get("/api/games/pending"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void demoVeSuCupo() throws Exception {
        mvc.perform(get("/api/games/demo-quota").with(user(DEMO)))
                .andExpect(status().isOk());
    }

    @Test
    void sinSesionNoSePuedeCrear() throws Exception {
        mvc.perform(post("/api/games").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(gameService);
    }

    @Test
    void sinSesionSeVeLaEstanteria() throws Exception {
        mvc.perform(get("/api/games"))
                .andExpect(status().isOk());
    }
}
