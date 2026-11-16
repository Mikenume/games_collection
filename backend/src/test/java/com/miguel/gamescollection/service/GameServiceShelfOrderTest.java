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

// La estantería agrupa por fabricante (alfabético) y, dentro de cada uno, va
// en orden cronológico por consola; en la misma consola, por título
class GameServiceShelfOrderTest {

    private static final Platform MD = platform("Mega Drive", "MD", "Sega", 1988, Platform.PlatformType.HOME);
    private static final Platform GB = platform("Game Boy", "GB", "Nintendo", 1989, Platform.PlatformType.HANDHELD);
    private static final Platform PS1 = platform("PlayStation", "PS1", "Sony", 1994, Platform.PlatformType.HOME);
    private static final Platform N64 = platform("Nintendo 64", "N64", "Nintendo", 1996, Platform.PlatformType.HOME);
    private static final Platform NSW = platform("Nintendo Switch", "NSW", "Nintendo", 2017, Platform.PlatformType.HYBRID);
    private static final Platform UNDATED = platform("Prototipo", "PROTO", "Nintendo", null, Platform.PlatformType.HOME);
    private static final Platform NO_MAKER = platform("Clónica", "CLON", null, 1990, Platform.PlatformType.HOME);

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
    void agrupaPorFabricanteAlfabeticoYCronologicoDentro() {
        List<String> titles = shelf(
                game("Mario Odyssey", NSW),
                game("Crash Bandicoot", PS1),
                game("Tetris", GB),
                game("Sonic", MD),
                game("Mario 64", N64));

        // Nintendo (GB 1989, N64 1996, NSW 2017) → Sega → Sony
        assertEquals(List.of("Tetris", "Mario 64", "Mario Odyssey", "Sonic", "Crash Bandicoot"), titles);
    }

    @Test
    void enLaMismaConsolaDesempataPorTitulo() {
        assertEquals(List.of("Ape Escape", "Crash Bandicoot", "Spyro"),
                shelf(game("Spyro", PS1), game("Crash Bandicoot", PS1), game("Ape Escape", PS1)));
    }

    @Test
    void laConsolaRepresentativaEsLaPrimeraEnElOrdenDeLaEstanteria() {
        // Con ediciones en PS1 y Mega Drive cuenta la Mega Drive (Sega va antes que Sony)
        assertEquals(List.of("Mario 64", "Sonic Origins"),
                shelf(game("Sonic Origins", PS1, MD), game("Mario 64", N64)));
    }

    @Test
    void sinAnioAlFinalDeSuFabricanteYSinFabricanteOSinEdicionAlFinal() {
        assertEquals(List.of("Mario Odyssey", "Prototipo", "Crash Bandicoot", "Clon", "Sin consola"),
                shelf(game("Sin consola"), game("Clon", NO_MAKER), game("Crash Bandicoot", PS1),
                        game("Prototipo", UNDATED), game("Mario Odyssey", NSW)));
    }

    @Test
    void lasPlataformasDeCadaJuegoVanEnElOrdenDeLaEstanteriaConFabricante() {
        when(gameRepository.findByStatus(GameStatus.APROBADO))
                .thenReturn(List.of(game("Sonic Origins", NSW, PS1, MD, GB)));

        assertEquals(List.of(
                        new PlatformRefDto("GB", "Game Boy", "Nintendo", (short) 1989),
                        new PlatformRefDto("NSW", "Nintendo Switch", "Nintendo", (short) 2017),
                        new PlatformRefDto("MD", "Mega Drive", "Sega", (short) 1988),
                        new PlatformRefDto("PS1", "PlayStation", "Sony", (short) 1994)),
                service.findAll().get(0).platforms());
    }
}
