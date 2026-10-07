package com.miguel.gamescollection.controller;

import com.miguel.gamescollection.config.CorsConfig;
import com.miguel.gamescollection.dto.EditionDto;
import com.miguel.gamescollection.dto.EditionSummaryDto;
import com.miguel.gamescollection.dto.GameDto;
import com.miguel.gamescollection.dto.GameSummaryDto;
import com.miguel.gamescollection.model.Role;
import com.miguel.gamescollection.security.CustomUserDetailsService;
import com.miguel.gamescollection.security.SecurityConfig;
import com.miguel.gamescollection.security.UserPrincipal;
import com.miguel.gamescollection.service.EditionService;
import com.miguel.gamescollection.service.GameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Si tengo o no cada juego/edición solo se le devuelve al admin
@WebMvcTest({GameController.class, EditionController.class})
@Import({SecurityConfig.class, CorsConfig.class})
class OwnedVisibilityTest {

    private static final UserPrincipal ADMIN = new UserPrincipal(1, "admin", null, Role.ADMIN, true);
    private static final UserPrincipal DEMO = new UserPrincipal(2, "demo", null, Role.DEMO, true);

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private GameService gameService;

    @MockitoBean
    private EditionService editionService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        when(gameService.findAll()).thenReturn(List.of(new GameSummaryDto(
                1, "Crash Bandicoot", (short) 1996, "Naughty Dog", "Sony", "original",
                null, List.of(), List.of("PS1"), true)));

        EditionSummaryDto edition = new EditionSummaryDto(
                10, 1, "PlayStation", "PS1", (short) 1996, "PAL", "CD", true, null, null);
        when(gameService.findById(eq(1), any())).thenReturn(new GameDto(
                1, "Crash Bandicoot", (short) 1996, "Naughty Dog", "Sony", null, null, "original",
                null, null, null, List.of(), List.of(edition)));

        when(editionService.findAll()).thenReturn(List.of(new EditionDto(
                10, 1, "Crash Bandicoot", 1, "PlayStation", "PS1", (short) 1996, "PAL", "CD",
                true, null, null)));
    }

    @Test
    void sinSesionLaEstanteriaNoDiceSiLoTengo() throws Exception {
        mvc.perform(get("/api/games"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Crash Bandicoot"))
                .andExpect(jsonPath("$[0].owned").value(nullValue()));
    }

    @Test
    void demoTampocoVeSiLoTengo() throws Exception {
        mvc.perform(get("/api/games/1").with(user(DEMO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.editions[0].format").value("CD"))
                .andExpect(jsonPath("$.editions[0].owned").value(nullValue()));
    }

    @Test
    void elAdminSiLoVe() throws Exception {
        mvc.perform(get("/api/games").with(user(ADMIN)))
                .andExpect(jsonPath("$[0].owned").value(true));
        mvc.perform(get("/api/games/1").with(user(ADMIN)))
                .andExpect(jsonPath("$.editions[0].owned").value(true));
    }

    @Test
    void sinSesionLasEdicionesNoDicenSiLasTengo() throws Exception {
        mvc.perform(get("/api/editions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].owned").value(nullValue()));
    }

    @Test
    void sinSesionNoSePuedeFiltrarPorLasQueTengo() throws Exception {
        // El filtro ?owned=true también lo delataría: se ignora
        mvc.perform(get("/api/editions").param("owned", "true"))
                .andExpect(status().isOk());
        verify(editionService).findAll();
        verify(editionService, never()).findOwned();
    }
}
