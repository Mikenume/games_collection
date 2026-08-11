package com.miguel.gamescollection.repository;

import com.miguel.gamescollection.model.Platform;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlatformRepository extends JpaRepository<Platform, Integer> {

    Optional<Platform> findByAbbreviationIgnoreCase(String abbreviation);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByAbbreviationIgnoreCase(String abbreviation);
}
