package com.miguel.gamescollection.repository;

import com.miguel.gamescollection.model.Edition;
import com.miguel.gamescollection.model.GameStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// Las consultas públicas filtran por el estado del juego: las ediciones de
// un juego pendiente no se muestran fuera de la pestaña PENDIENTES
public interface EditionRepository extends JpaRepository<Edition, Integer> {

    @Override
    @EntityGraph(attributePaths = {"game", "platform"})
    Optional<Edition> findById(Integer id);

    @EntityGraph(attributePaths = {"game", "platform"})
    List<Edition> findByGameStatus(GameStatus status);

    @EntityGraph(attributePaths = {"game", "platform"})
    Optional<Edition> findByIdAndGameStatus(Integer id, GameStatus status);

    @EntityGraph(attributePaths = {"game", "platform"})
    List<Edition> findByPlatformIdAndGameStatus(Integer platformId, GameStatus status);

    @EntityGraph(attributePaths = {"game", "platform"})
    List<Edition> findByGameIdAndGameStatus(Integer gameId, GameStatus status);

    @EntityGraph(attributePaths = {"game", "platform"})
    List<Edition> findByOwnedTrueAndGameStatus(GameStatus status);

    boolean existsByPlatformId(Integer platformId);
}
