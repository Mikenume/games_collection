package com.miguel.gamescollection.repository;

import com.miguel.gamescollection.model.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GenreRepository extends JpaRepository<Genre, Integer> {

    boolean existsByNameIgnoreCase(String name);
}
