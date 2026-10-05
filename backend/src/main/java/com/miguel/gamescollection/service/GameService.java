package com.miguel.gamescollection.service;

import com.miguel.gamescollection.dto.EditionSummaryDto;
import com.miguel.gamescollection.dto.GameEditionRequest;
import com.miguel.gamescollection.dto.GameDto;
import com.miguel.gamescollection.dto.GameRequest;
import com.miguel.gamescollection.dto.GameSummaryDto;
import com.miguel.gamescollection.dto.GenreDto;
import com.miguel.gamescollection.exception.ResourceNotFoundException;
import com.miguel.gamescollection.model.Edition;
import com.miguel.gamescollection.model.Game;
import com.miguel.gamescollection.model.Genre;
import com.miguel.gamescollection.model.Platform;
import com.miguel.gamescollection.repository.GameRepository;
import com.miguel.gamescollection.repository.GenreRepository;
import com.miguel.gamescollection.repository.PlatformRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class GameService {

    private static final String DEFAULT_EDITION_TYPE = "original";

    // Orden de "la estantería": primero las de sobremesa (por fabricante y luego
    // por año de la consola), después las portátiles, y por último las híbridas.
    private static final List<ShelfGroup> SHELF_ORDER = List.of(
            new ShelfGroup("Nintendo", Platform.PlatformType.HOME),
            new ShelfGroup("Sony", Platform.PlatformType.HOME),
            new ShelfGroup("Nintendo", Platform.PlatformType.HANDHELD),
            new ShelfGroup("Sony", Platform.PlatformType.HANDHELD),
            new ShelfGroup("Nintendo", Platform.PlatformType.HYBRID),
            new ShelfGroup("Sony", Platform.PlatformType.HYBRID)
    );

    private record ShelfGroup(String manufacturer, Platform.PlatformType type) {
    }

    private record ShelfKey(int groupRank, int platformYear, String title) implements Comparable<ShelfKey> {
        @Override
        public int compareTo(ShelfKey other) {
            int cmp = Integer.compare(groupRank, other.groupRank);
            if (cmp != 0) return cmp;
            cmp = Integer.compare(platformYear, other.platformYear);
            if (cmp != 0) return cmp;
            return title.compareToIgnoreCase(other.title);
        }
    }

    private final GameRepository gameRepository;
    private final GenreRepository genreRepository;
    private final PlatformRepository platformRepository;

    public GameService(GameRepository gameRepository, GenreRepository genreRepository,
                       PlatformRepository platformRepository) {
        this.gameRepository = gameRepository;
        this.genreRepository = genreRepository;
        this.platformRepository = platformRepository;
    }

    @Transactional(readOnly = true)
    public List<GameSummaryDto> findAll() {
        return gameRepository.findAll()
                .stream()
                .distinct()
                .sorted(Comparator.comparing(this::shelfKey))
                .map(this::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GameSummaryDto> searchByTitle(String title) {
        return gameRepository.findByTitleContainingIgnoreCaseOrderByTitleAsc(title)
                .stream()
                .distinct()
                .sorted(Comparator.comparing(this::shelfKey))
                .map(this::toSummary)
                .toList();
    }

    // La plataforma "representativa" de un juego es la de menor rango en el
    // orden de la estantería; si tiene varias ediciones en el mismo grupo
    // (p.ej. NES y SNES, ambas Nintendo/HOME), gana la de año más antiguo.
    private ShelfKey shelfKey(Game game) {
        Platform representative = game.getEditions().stream()
                .map(Edition::getPlatform)
                .min(Comparator.comparingInt(this::shelfGroupRank)
                        .thenComparing(p -> p.getReleaseYear() == null ? Short.MAX_VALUE : p.getReleaseYear()))
                .orElse(null);

        int rank = representative == null ? SHELF_ORDER.size() : shelfGroupRank(representative);
        int year = (representative == null || representative.getReleaseYear() == null)
                ? Integer.MAX_VALUE
                : representative.getReleaseYear();

        return new ShelfKey(rank, year, game.getTitle());
    }

    private int shelfGroupRank(Platform platform) {
        for (int i = 0; i < SHELF_ORDER.size(); i++) {
            ShelfGroup group = SHELF_ORDER.get(i);
            if (group.manufacturer().equalsIgnoreCase(platform.getManufacturer()) && group.type() == platform.getType()) {
                return i;
            }
        }
        return SHELF_ORDER.size();
    }

    @Transactional(readOnly = true)
    public GameDto findById(Integer id) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("el juego", id));
        return toDto(game);
    }

    @Transactional
    public GameDto create(GameRequest request) {
        Game game = new Game(request.title());
        applyRequest(game, request);
        applyEditions(game, request.editions());
        return toDto(gameRepository.save(game));
    }

    @Transactional
    public GameDto update(Integer id, GameRequest request) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("el juego", id));
        game.setTitle(request.title());
        applyRequest(game, request);
        applyEditions(game, request.editions());
        return toDto(game);
    }

    @Transactional
    public void delete(Integer id) {
        if (!gameRepository.existsById(id)) {
            throw new ResourceNotFoundException("el juego", id);
        }
        gameRepository.deleteById(id);
    }

    // Copia los campos del request sobre la entidad y resuelve los géneros
    private void applyRequest(Game game, GameRequest request) {
        game.setReleaseYear(request.releaseYear());
        game.setDeveloper(request.developer());
        game.setPublisher(request.publisher());
        game.setSynopsis(request.synopsis());
        game.setNotes(request.notes());
        game.setCoverUrl(request.coverUrl());
        game.setEditionType(
                request.editionType() == null ? DEFAULT_EDITION_TYPE : request.editionType()
        );

        if (request.genreIds() != null) {
            Set<Genre> genres = new LinkedHashSet<>();
            for (Integer genreId : request.genreIds()) {
                Genre genre = genreRepository.findById(genreId)
                        .orElseThrow(() -> new ResourceNotFoundException("el género", genreId));
                genres.add(genre);
            }
            game.setGenres(genres);
        }
    }

    // Deja las ediciones del juego igual que la lista recibida: las que no traen
    // id se crean, las que lo traen se actualizan y las que no vienen se borran.
    // Con null no se toca nada, para que un request sin ediciones no las borre.
    private void applyEditions(Game game, List<GameEditionRequest> requests) {
        if (requests == null) return;

        Set<String> keys = new HashSet<>();
        Set<Integer> keptIds = new HashSet<>();
        for (GameEditionRequest request : requests) {
            if (!keys.add(request.platformId() + "|" + request.region())) {
                throw new IllegalArgumentException("Hay dos ediciones con la misma plataforma y región");
            }
            if (request.id() != null) keptIds.add(request.id());
        }

        Map<Integer, Edition> existing = new HashMap<>();
        for (Edition edition : game.getEditions()) {
            existing.put(edition.getId(), edition);
        }
        for (Integer id : keptIds) {
            if (!existing.containsKey(id)) {
                throw new ResourceNotFoundException("la edición", id);
            }
        }

        // Hibernate inserta antes de borrar al hacer flush; si se quita una
        // edición y se vuelve a añadir con la misma plataforma y región,
        // uq_editions saltaría. Por eso se borran primero.
        boolean removed = game.getEditions().removeIf(edition -> !keptIds.contains(edition.getId()));
        if (removed) gameRepository.flush();

        for (GameEditionRequest request : requests) {
            Platform platform = platformRepository.findById(request.platformId())
                    .orElseThrow(() -> new ResourceNotFoundException("la plataforma", request.platformId()));

            Edition edition;
            if (request.id() == null) {
                edition = new Edition(game, platform);
                game.getEditions().add(edition);
            } else {
                edition = existing.get(request.id());
                edition.setPlatform(platform);
            }

            // Mismos valores por defecto que add_game(): el año de la edición
            // es el del juego si no se indica, y se da por hecho que se tiene.
            edition.setReleaseYear(request.releaseYear() != null ? request.releaseYear() : game.getReleaseYear());
            edition.setRegion(request.region());
            edition.setFormat(request.format());
            edition.setOwned(request.owned() != null ? request.owned() : Boolean.TRUE);
            edition.setPortDeveloper(request.portDeveloper());
            edition.setNotes(request.notes());
        }
    }

    private GameSummaryDto toSummary(Game game) {
        List<String> genreNames = game.getGenres().stream()
                .map(Genre::getName)
                .sorted()
                .toList();

        List<String> platformNames = game.getEditions().stream()
                .map(edition -> edition.getPlatform().getAbbreviation())
                .distinct()
                .sorted()
                .toList();

        boolean owned = game.getEditions().stream()
                .anyMatch(edition -> Boolean.TRUE.equals(edition.getOwned()));

        return new GameSummaryDto(
                game.getId(),
                game.getTitle(),
                game.getReleaseYear(),
                game.getDeveloper(),
                game.getPublisher(),
                game.getEditionType(),
                game.getCoverUrl(),
                genreNames,
                platformNames,
                owned
        );
    }

    private GameDto toDto(Game game) {
        List<GenreDto> genres = game.getGenres().stream()
                .map(genre -> new GenreDto(genre.getId(), genre.getName()))
                .sorted(Comparator.comparing(GenreDto::name))
                .toList();

        List<EditionSummaryDto> editions = game.getEditions().stream()
                .map(this::toEditionSummary)
                .sorted(Comparator.comparing(EditionSummaryDto::platformAbbreviation))
                .toList();

        return new GameDto(
                game.getId(),
                game.getTitle(),
                game.getReleaseYear(),
                game.getDeveloper(),
                game.getPublisher(),
                game.getSynopsis(),
                game.getNotes(),
                game.getEditionType(),
                game.getCoverUrl(),
                game.getCreatedAt(),
                genres,
                editions
        );
    }

    private EditionSummaryDto toEditionSummary(Edition edition) {
        return new EditionSummaryDto(
                edition.getId(),
                edition.getPlatform().getId(),
                edition.getPlatform().getName(),
                edition.getPlatform().getAbbreviation(),
                edition.getReleaseYear(),
                edition.getRegion(),
                edition.getFormat(),
                edition.getOwned(),
                edition.getPortDeveloper(),
                edition.getNotes()
        );
    }
}
