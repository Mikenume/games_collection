package com.miguel.gamescollection.repository;

import com.miguel.gamescollection.model.Game;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Integer> {

    /*
     * @EntityGraph le dice a Hibernate que traiga estas relaciones en la MISMA
     * consulta, con JOIN FETCH, en lugar de una consulta extra por cada juego.
     * Es la solución al problema N+1 sin escribir JPQL a mano.
     */
    @Override
    @EntityGraph(attributePaths = {"genres", "editions", "editions.platform"})
    List<Game> findAll();

    @Override
    @EntityGraph(attributePaths = {"genres", "editions", "editions.platform"})
    Optional<Game> findById(Integer id);

    @EntityGraph(attributePaths = {"genres", "editions", "editions.platform"})
    List<Game> findByTitleContainingIgnoreCaseOrderByTitleAsc(String title);
}
