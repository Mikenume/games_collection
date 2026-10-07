package com.miguel.gamescollection.service;

import com.miguel.gamescollection.config.DemoProperties;
import com.miguel.gamescollection.dto.GameEditionRequest;
import com.miguel.gamescollection.dto.GameRequest;
import com.miguel.gamescollection.exception.DemoLimitExceededException;
import com.miguel.gamescollection.model.Game;
import com.miguel.gamescollection.model.GameStatus;
import com.miguel.gamescollection.model.Platform;
import com.miguel.gamescollection.model.Role;
import com.miguel.gamescollection.repository.GameRepository;
import com.miguel.gamescollection.repository.GenreRepository;
import com.miguel.gamescollection.repository.PlatformRepository;
import com.miguel.gamescollection.repository.UserRepository;
import com.miguel.gamescollection.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GameServiceDemoLimitTest {

    private static final UserPrincipal DEMO = new UserPrincipal(2, "demo", null, Role.DEMO, true);

    private final GameRepository gameRepository = mock(GameRepository.class);
    private final PlatformRepository platformRepository = mock(PlatformRepository.class);
    private GameService service;

    @BeforeEach
    void setUp() {
        service = new GameService(gameRepository, mock(GenreRepository.class), platformRepository,
                mock(UserRepository.class), new DemoProperties("demo", 50, ZoneId.of("Europe/Madrid")));
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(platformRepository.findById(1)).thenReturn(Optional.of(mock(Platform.class)));
    }

    // Marca la edición como "la tengo" a propósito: la demo no puede meterla en mi colección
    private static GameRequest request(List<GameEditionRequest> editions) {
        return new GameRequest("Spyro the Dragon", (short) 1998, null, null, null,
                "notas", "remake", null, "https://example.com/portada.jpg", editions);
    }

    private static GameRequest request() {
        return request(List.of(new GameEditionRequest(null, 1, null, "PAL", "CD", true, null, null)));
    }

    private void createdToday(long count) {
        when(gameRepository.countByCreatedByRoleAndCreatedAtGreaterThanEqual(eq(Role.DEMO), any()))
                .thenReturn(count);
    }

    @Test
    void elJuego51DelDiaSuperaElLimite() {
        createdToday(50);

        assertThrows(DemoLimitExceededException.class, () -> service.create(request(), DEMO));
        verify(gameRepository, never()).save(any());
    }

    @Test
    void conCupoQuedaPendienteConTodosSusDatos() {
        createdToday(49);

        service.create(request(), DEMO);

        ArgumentCaptor<Game> saved = ArgumentCaptor.forClass(Game.class);
        verify(gameRepository).save(saved.capture());
        Game game = saved.getValue();
        assertEquals(GameStatus.PENDIENTE, game.getStatus());
        assertEquals("remake", game.getEditionType());
        assertEquals("https://example.com/portada.jpg", game.getCoverUrl());
        assertEquals("notas", game.getNotes());
        assertEquals(1, game.getEditions().size());
        assertFalse(game.getEditions().iterator().next().getOwned());
    }

    @Test
    void laDemoTieneQueEnviarAlMenosUnaEdicion() {
        createdToday(0);

        assertThrows(IllegalArgumentException.class, () -> service.create(request(List.of()), DEMO));
        assertThrows(IllegalArgumentException.class, () -> service.create(request(null), DEMO));
        verify(gameRepository, never()).save(any());
    }

    @Test
    void elCupoNoBajaDeCero() {
        createdToday(53);

        assertEquals(0, service.demoQuota().remaining());
    }
}
