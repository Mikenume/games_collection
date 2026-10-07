package com.miguel.gamescollection.repository;

import com.miguel.gamescollection.model.Game;
import com.miguel.gamescollection.model.GameStatus;
import com.miguel.gamescollection.model.Role;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

// Ojo: findAll() y findById() heredados devuelven también los pendientes.
// Lo público (estantería, búsqueda, detalle) usa las versiones con status.
public interface GameRepository extends JpaRepository<Game, Integer> {

    List<Game> findByStatus(GameStatus status);

    List<Game> findByStatusAndTitleContainingIgnoreCaseOrderByTitleAsc(GameStatus status, String title);

    Optional<Game> findByIdAndStatus(Integer id, GameStatus status);

    // Pestaña PENDIENTES: los más antiguos primero, con creador, géneros y
    // plataformas en la misma consulta
    @EntityGraph(attributePaths = {"createdBy", "genres", "editions", "editions.platform"})
    List<Game> findByStatusOrderByCreatedAtAsc(GameStatus status);

    // Juegos creados desde "since" por usuarios con ese rol (cupo DEMO)
    long countByCreatedByRoleAndCreatedAtGreaterThanEqual(Role role, OffsetDateTime since);
}
