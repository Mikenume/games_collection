package com.miguel.gamescollection.service;

import com.miguel.gamescollection.dto.EditionDto;
import com.miguel.gamescollection.dto.EditionRequest;
import com.miguel.gamescollection.exception.ResourceNotFoundException;
import com.miguel.gamescollection.model.Edition;
import com.miguel.gamescollection.model.Game;
import com.miguel.gamescollection.model.GameStatus;
import com.miguel.gamescollection.model.Platform;
import com.miguel.gamescollection.repository.EditionRepository;
import com.miguel.gamescollection.repository.GameRepository;
import com.miguel.gamescollection.repository.PlatformRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EditionService {

    private final EditionRepository editionRepository;
    private final GameRepository gameRepository;
    private final PlatformRepository platformRepository;

    public EditionService(EditionRepository editionRepository,
                          GameRepository gameRepository,
                          PlatformRepository platformRepository) {
        this.editionRepository = editionRepository;
        this.gameRepository = gameRepository;
        this.platformRepository = platformRepository;
    }

    // Los GET son públicos: solo ediciones de juegos aprobados

    @Transactional(readOnly = true)
    public List<EditionDto> findAll() {
        return editionRepository.findByGameStatus(GameStatus.APROBADO)
                .stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public EditionDto findById(Integer id) {
        Edition edition = editionRepository.findByIdAndGameStatus(id, GameStatus.APROBADO)
                .orElseThrow(() -> new ResourceNotFoundException("la edición", id));
        return toDto(edition);
    }

    @Transactional(readOnly = true)
    public List<EditionDto> findByPlatform(Integer platformId) {
        return editionRepository.findByPlatformIdAndGameStatus(platformId, GameStatus.APROBADO)
                .stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<EditionDto> findByGame(Integer gameId) {
        return editionRepository.findByGameIdAndGameStatus(gameId, GameStatus.APROBADO)
                .stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<EditionDto> findOwned() {
        return editionRepository.findByOwnedTrueAndGameStatus(GameStatus.APROBADO)
                .stream().map(this::toDto).toList();
    }

    @Transactional
    public EditionDto create(EditionRequest request) {
        Game game = findApprovedGame(request.gameId());
        Platform platform = platformRepository.findById(request.platformId())
                .orElseThrow(() -> new ResourceNotFoundException("la plataforma", request.platformId()));

        Edition edition = new Edition(game, platform);
        applyRequest(edition, request);
        return toDto(editionRepository.save(edition));
    }

    @Transactional
    public EditionDto update(Integer id, EditionRequest request) {
        Edition edition = editionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("la edición", id));

        Game game = findApprovedGame(request.gameId());
        Platform platform = platformRepository.findById(request.platformId())
                .orElseThrow(() -> new ResourceNotFoundException("la plataforma", request.platformId()));

        edition.setGame(game);
        edition.setPlatform(platform);
        applyRequest(edition, request);
        return toDto(edition);
    }

    @Transactional
    public void delete(Integer id) {
        if (!editionRepository.existsById(id)) {
            throw new ResourceNotFoundException("la edición", id);
        }
        editionRepository.deleteById(id);
    }

    // Un juego pendiente se completa desde su formulario (PUT /api/games/{id}),
    // no añadiéndole ediciones sueltas
    private Game findApprovedGame(Integer gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("el juego", gameId));
        if (game.getStatus() != GameStatus.APROBADO) {
            throw new IllegalArgumentException(
                    "El juego " + gameId + " está pendiente de revisión: apruébalo antes de añadirle ediciones");
        }
        return game;
    }

    private void applyRequest(Edition edition, EditionRequest request) {
        edition.setReleaseYear(request.releaseYear());
        edition.setRegion(request.region());
        edition.setFormat(request.format());
        edition.setOwned(request.owned() != null ? request.owned() : Boolean.FALSE);
        edition.setPortDeveloper(request.portDeveloper());
        edition.setNotes(request.notes());
    }

    private EditionDto toDto(Edition edition) {
        return new EditionDto(
                edition.getId(),
                edition.getGame().getId(),
                edition.getGame().getTitle(),
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
