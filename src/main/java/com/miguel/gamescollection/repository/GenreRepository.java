package com.miguel.gamescollection.repository;

import com.miguel.gamescollection.model.Genre;
import com.miguel.gamescollection.model.Platform;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GenreRepository extends JpaRepository<Genre, Integer>{
}
