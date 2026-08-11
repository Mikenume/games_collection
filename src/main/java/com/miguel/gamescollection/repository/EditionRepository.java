package com.miguel.gamescollection.repository;

import com.miguel.gamescollection.model.Edition;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EditionRepository extends JpaRepository<Edition, Integer> {

    @Override
    @EntityGraph(attributePaths = {"game", "platform"})
    List<Edition> findAll();

    @Override
    @EntityGraph(attributePaths = {"game", "platform"})
    Optional<Edition> findById(Integer id);

    @EntityGraph(attributePaths = {"game", "platform"})
    List<Edition> findByPlatformId(Integer platformId);

    @EntityGraph(attributePaths = {"game", "platform"})
    List<Edition> findByGameId(Integer gameId);

    @EntityGraph(attributePaths = {"game", "platform"})
    List<Edition> findByOwnedTrue();

    boolean existsByPlatformId(Integer platformId);
}
