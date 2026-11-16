package com.miguel.gamescollection.service;

import com.miguel.gamescollection.config.DemoProperties;
import com.miguel.gamescollection.dto.GameSummaryDto;
import com.miguel.gamescollection.dto.PlatformRefDto;
import com.miguel.gamescollection.model.Edition;
import com.miguel.gamescollection.model.Game;
import com.miguel.gamescollection.model.GameStatus;
import com.miguel.gamescollection.model.Platform;
import com.miguel.gamescollection.repository.GameRepository;
import com.miguel.gamescollection.repository.GenreRepository;
import com.miguel.gamescollection.repository.PlatformRepository;
import com.miguel.gamescollection.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// La estantería va en orden cronológico puro por la consola más antigua de
// cada juego, sin agrupar por fabricante ni por tipo
class GameServiceShelfOrderTest {

    private static final Platform MD = platform("Mega Drive", "MD", "Sega", 1988, Platform.PlatformType.HOME);
    private static final Platform GB = platform("Game Boy", "GB", "Nintendo", 1989, Platform.PlatformType.HANDHELD);
    private static final Platform PS1 = platform("PlayStation", "PS1", "Sony", 1994, Platform.PlatformType.HOME);
    private static final Platform N64 = platform("Nintendo 64", "N64", "Nintendo", 1996, Platform.PlatformType.HOME);
    private static final Platform NSW = platform("Nintendo Switch", "NSW", "Nintendo", 2017, Platform.PlatformType.HYBRID);
    private static final Platform UNDATED = platform("Prototipo", "PROTO", "Otro", null, Platform.PlatformType.HOME);

    private final GameRepository gameRepository = mock(GameRepository.class);
    private GameService service;

    @BeforeEach
    void setUp() {
        service = new GameService(gameRepository, mock(GenreRepository.class), mock(PlatformRepository.class),
                mock(UserRepository.class), new DemoProperties("demo", 50, ZoneId.of("Europe/Madrid")));
    }

    private static Platform platform(String name, String abbr, String maker, Integer year, Platform.PlatformType type) {
        Platform p = new Platform(name, abbr, maker, year == null ? null : year.shortValue());
        p.setType(type);
        return p;
    }

    private static Game game(String title, Platform... platforms) {
        Game game = new Game(title);
        for (Platform platform : platforms) {
            game.getEditions().add(new Edition(game, platform));
        }
        return game;
    }

    private List<String> shelf(Game... games) {
        when(gameRepository.findByStatus(GameStatus.APROBADO)).thenReturn(List.of(games));
        return service.findAll().stream().map(GameSummaryDto::title).toList();
    }

    @Test
    void mezclaFabricantesYTiposPorAnioDeLaConsola() {
        List<String> titles = shelf(
                game("Mario Odyssey", NSW),
                game("Crash Bandicoot", PS1),
                game("Tetris", GB),
                game("Sonic", MD),
                game("Mario 64", N64));

        assertEquals(List.of("Sonic", "Tetris", "Crash Bandicoot", "Mario 64", "Mario Odyssey"), titles);
    }

    @Test
    void enLaMismaConsolaDesempataPorTitulo() {
        assertEquals(List.of("Ape Escape", "Crash Bandicoot", "Spyro"),
                shelf(game("Spyro", PS1), game("Crash Bandicoot", PS1), game("Ape Escape", PS1)));
    }

    @Test
    void laConsolaRepresentativaEsLaMasAntigua() {
        // Con ediciones en Switch y Mega Drive cuenta la Mega Drive (1988)
        assertEquals(List.of("Sonic Origins", "Crash Bandicoot"),
                shelf(game("Crash Bandicoot", PS1), game("Sonic Origins", NSW, MD)));
    }

    @Test
    void lasConsolasSinAnioYLosJuegosSinEdicionVanAlFinal() {
        assertEquals(List.of("Mario Odyssey", "Prototipo", "Sin consola"),
                shelf(game("Sin consola"), game("Prototipo", UNDATED), game("Mario Odyssey", NSW)));
    }

    @Test
    void lasPlataformasDeCadaJuegoVanPorAnioConNombreCompleto() {
        when(gameRepository.findByStatus(GameStatus.APROBADO))
                .thenReturn(List.of(game("Sonic Origins", NSW, PS1, MD)));

        assertEquals(List.of(
                        new PlatformRefDto("MD", "Mega Drive", (short) 1988),
                        new PlatformRefDto("PS1", "PlayStation", (short) 1994),
                        new PlatformRefDto("NSW", "Nintendo Switch", (short) 2017)),
                service.findAll().get(0).platforms());
    }
}
