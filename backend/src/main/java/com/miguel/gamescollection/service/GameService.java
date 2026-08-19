package com.miguel.gamescollection.service;

import com.miguel.gamescollection.dto.EditionSummaryDto;
import com.miguel.gamescollection.dto.GameDto;
import com.miguel.gamescollection.dto.GameRequest;
import com.miguel.gamescollection.dto.GameSummaryDto;
import com.miguel.gamescollection.dto.GenreDto;
import com.miguel.gamescollection.exception.ResourceNotFoundException;
import com.miguel.gamescollection.model.Edition;
import com.miguel.gamescollection.model.Game;
import com.miguel.gamescollection.model.Genre;
import com.miguel.gamescollection.repository.GameRepository;
import com.miguel.gamescollection.repository.GenreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class GameService {

    private static final String DEFAULT_EDITION_TYPE = "original";

    private final GameRepository gameRepository;
    private final GenreRepository genreRepository;

    public GameService(GameRepository gameRepository, GenreRepository genreRepository) {
        this.gameRepository = gameRepository;
        this.genreRepository = genreRepository;
    }

    @Transactional(readOnly = true)
    public List<GameSummaryDto> findAll() {
        return gameRepository.findAll()
                .stream()
                .distinct()
                .map(this::toSummary)
                .sorted(Comparator.comparing(GameSummaryDto::title, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GameSummaryDto> searchByTitle(String title) {
        return gameRepository.findByTitleContainingIgnoreCaseOrderByTitleAsc(title)
                .stream()
                .distinct()
                .map(this::toSummary)
                .toList();
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
        return toDto(gameRepository.save(game));
    }

    @Transactional
    public GameDto update(Integer id, GameRequest request) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("el juego", id));
        game.setTitle(request.title());
        applyRequest(game, request);
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
