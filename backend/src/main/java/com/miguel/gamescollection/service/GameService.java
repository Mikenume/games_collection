package com.miguel.gamescollection.service;

import com.miguel.gamescollection.config.DemoProperties;
import com.miguel.gamescollection.dto.DemoQuotaDto;
import com.miguel.gamescollection.dto.EditionSummaryDto;
import com.miguel.gamescollection.dto.GameEditionRequest;
import com.miguel.gamescollection.dto.GameDto;
import com.miguel.gamescollection.dto.GameRequest;
import com.miguel.gamescollection.dto.GameSummaryDto;
import com.miguel.gamescollection.dto.GenreDto;
import com.miguel.gamescollection.dto.PendingGameDto;
import com.miguel.gamescollection.dto.PlatformRefDto;
import com.miguel.gamescollection.exception.DemoLimitExceededException;
import com.miguel.gamescollection.exception.ResourceNotFoundException;
import com.miguel.gamescollection.model.Edition;
import com.miguel.gamescollection.model.Game;
import com.miguel.gamescollection.model.GameStatus;
import com.miguel.gamescollection.model.Genre;
import com.miguel.gamescollection.model.Platform;
import com.miguel.gamescollection.model.Role;
import com.miguel.gamescollection.model.User;
import com.miguel.gamescollection.repository.GameRepository;
import com.miguel.gamescollection.repository.GenreRepository;
import com.miguel.gamescollection.repository.PlatformRepository;
import com.miguel.gamescollection.repository.UserRepository;
import com.miguel.gamescollection.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
public class GameService {

    private static final String DEFAULT_EDITION_TYPE = "original";

    // Orden cronológico de plataformas: por año de lanzamiento (las que no
    // tienen año, al final) y, si coinciden, por abreviatura para que sea estable
    static final Comparator<Platform> CHRONOLOGICAL = Comparator
            .comparing(Platform::getReleaseYear, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(Platform::getAbbreviation, String.CASE_INSENSITIVE_ORDER);

    // Orden de plataformas en la estantería: agrupadas por fabricante (en orden
    // alfabético; las que no tienen fabricante, al final) y, dentro de cada uno,
    // cronológicas (Nintendo: NES 1983 → Game Boy 1989 → SNES 1990...)
    static final Comparator<Platform> SHELF_PLATFORM_ORDER = Comparator
            .comparing(Platform::getManufacturer, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
            .thenComparing(CHRONOLOGICAL);

    // Orden de "la estantería": por la plataforma representativa del juego según
    // el orden anterior y, dentro de la misma plataforma, por título. Los juegos
    // sin plataforma, al final.
    static final Comparator<Game> SHELF_ORDER = Comparator
            .comparing(GameService::representativePlatform, Comparator.nullsLast(SHELF_PLATFORM_ORDER))
            .thenComparing(Game::getTitle, String.CASE_INSENSITIVE_ORDER);

    private final GameRepository gameRepository;
    private final GenreRepository genreRepository;
    private final PlatformRepository platformRepository;
    private final UserRepository userRepository;
    private final DemoProperties demoProperties;

    public GameService(GameRepository gameRepository, GenreRepository genreRepository,
                       PlatformRepository platformRepository, UserRepository userRepository,
                       DemoProperties demoProperties) {
        this.gameRepository = gameRepository;
        this.genreRepository = genreRepository;
        this.platformRepository = platformRepository;
        this.userRepository = userRepository;
        this.demoProperties = demoProperties;
    }

    // Estantería: solo los juegos aprobados
    @Transactional(readOnly = true)
    public List<GameSummaryDto> findAll() {
        return gameRepository.findByStatus(GameStatus.APROBADO)
                .stream()
                .distinct()
                .sorted(SHELF_ORDER)
                .map(this::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GameSummaryDto> searchByTitle(String title) {
        return gameRepository.findByStatusAndTitleContainingIgnoreCaseOrderByTitleAsc(GameStatus.APROBADO, title)
                .stream()
                .distinct()
                .sorted(SHELF_ORDER)
                .map(this::toSummary)
                .toList();
    }

    // La plataforma "representativa" de un juego es la primera de sus ediciones
    // en el orden de la estantería; es la que decide en qué tramo cae.
    static Platform representativePlatform(Game game) {
        return game.getEditions().stream()
                .map(Edition::getPlatform)
                .min(SHELF_PLATFORM_ORDER)
                .orElse(null);
    }

    // Plataformas distintas de un juego en el orden de la estantería; la primera
    // es la representativa
    private static List<Platform> platformsOf(Game game) {
        return game.getEditions().stream()
                .map(Edition::getPlatform)
                .distinct()
                .sorted(SHELF_PLATFORM_ORDER)
                .toList();
    }

    // El admin ve también los pendientes; para el resto un pendiente no existe (404)
    @Transactional(readOnly = true)
    public GameDto findById(Integer id, UserPrincipal user) {
        Optional<Game> game = (user != null && user.isAdmin())
                ? gameRepository.findById(id)
                : gameRepository.findByIdAndStatus(id, GameStatus.APROBADO);
        return toDto(game.orElseThrow(() -> new ResourceNotFoundException("el juego", id)));
    }

    // El estado, el creador y la fecha nunca salen del request: los pone el servidor.
    // Los dos envían el juego completo con sus ediciones.
    // ADMIN: queda aprobado directamente.
    // DEMO: queda pendiente de que el admin lo apruebe, edite o descarte.
    @Transactional
    public GameDto create(GameRequest request, UserPrincipal user) {
        boolean demo = !user.isAdmin();
        if (demo) {
            checkDemoQuota();
            // Sin plataforma acabaría al final de la estantería, bajo "—"
            if (request.editions() == null || request.editions().isEmpty()) {
                throw new IllegalArgumentException("Añade al menos una edición con su plataforma");
            }
        }

        User creator = userRepository.getReferenceById(user.getId());
        Game game = new Game(request.title());
        game.setCreatedBy(creator);
        game.setStatus(demo ? GameStatus.PENDIENTE : GameStatus.APROBADO);
        applyRequest(game, request);
        applyEditions(game, request.editions());

        // "La tengo" es de mi colección: lo que envía la demo nunca entra en ella
        if (demo) {
            game.getEditions().forEach(edition -> edition.setOwned(Boolean.FALSE));
        }
        return toDto(gameRepository.save(game));
    }

    @Transactional(readOnly = true)
    public List<PendingGameDto> findPending() {
        return gameRepository.findByStatusOrderByCreatedAtAsc(GameStatus.PENDIENTE)
                .stream()
                .distinct()
                .map(this::toPending)
                .toList();
    }

    // Si ya estaba aprobado se devuelve tal cual: aprobar dos veces no es un error
    @Transactional
    public GameDto approve(Integer id) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("el juego", id));
        game.setStatus(GameStatus.APROBADO);
        return toDto(game);
    }

    @Transactional(readOnly = true)
    public DemoQuotaDto demoQuota() {
        long createdToday = gameRepository.countByCreatedByRoleAndCreatedAtGreaterThanEqual(
                Role.DEMO, startOfToday());
        int limit = demoProperties.dailyLimit();
        return new DemoQuotaDto(createdToday, limit, Math.max(0, limit - createdToday));
    }

    // No controla dos peticiones a la vez con 49 creados: como mucho entra uno de más
    private void checkDemoQuota() {
        if (demoQuota().remaining() <= 0) {
            throw new DemoLimitExceededException(demoProperties.dailyLimit());
        }
    }

    // 00:00 de hoy en la zona configurada (Europe/Madrid), aunque el servidor vaya en UTC
    private OffsetDateTime startOfToday() {
        ZoneId zone = demoProperties.zone();
        return LocalDate.now(zone).atStartOfDay(zone).toOffsetDateTime();
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
        game.setTrailerId(request.trailerId());
        game.setEditionType(
                request.editionType() == null ? DEFAULT_EDITION_TYPE : request.editionType()
        );
        applyGenres(game, request.genreIds());
    }

    private void applyGenres(Game game, Set<Integer> genreIds) {
        if (genreIds == null) return;

        Set<Genre> genres = new LinkedHashSet<>();
        for (Integer genreId : genreIds) {
            Genre genre = genreRepository.findById(genreId)
                    .orElseThrow(() -> new ResourceNotFoundException("el género", genreId));
            genres.add(genre);
        }
        game.setGenres(genres);
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

        List<PlatformRefDto> platforms = platformsOf(game).stream()
                .map(p -> new PlatformRefDto(p.getAbbreviation(), p.getName(), p.getManufacturer(), p.getReleaseYear()))
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
                platforms,
                owned
        );
    }

    private PendingGameDto toPending(Game game) {
        List<String> genreNames = game.getGenres().stream()
                .map(Genre::getName)
                .sorted()
                .toList();

        List<String> platformNames = platformsOf(game).stream()
                .map(Platform::getAbbreviation)
                .toList();

        List<String> formats = game.getEditions().stream()
                .map(Edition::getFormat)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();

        String createdBy = game.getCreatedBy() == null ? null : game.getCreatedBy().getUsername();

        return new PendingGameDto(
                game.getId(),
                game.getTitle(),
                game.getReleaseYear(),
                game.getDeveloper(),
                game.getPublisher(),
                game.getSynopsis(),
                game.getCoverUrl(),
                game.getTrailerId(),
                genreNames,
                platformNames,
                formats,
                createdBy,
                game.getCreatedAt()
        );
    }

    private GameDto toDto(Game game) {
        List<GenreDto> genres = game.getGenres().stream()
                .map(genre -> new GenreDto(genre.getId(), genre.getName()))
                .sorted(Comparator.comparing(GenreDto::name))
                .toList();

        // En el orden de la estantería (fabricante y año); la primera es la que
        // da color a la ficha
        List<EditionSummaryDto> editions = game.getEditions().stream()
                .sorted(Comparator.comparing(Edition::getPlatform, SHELF_PLATFORM_ORDER)
                        .thenComparing(Edition::getRegion, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .map(this::toEditionSummary)
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
                game.getTrailerId(),
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
