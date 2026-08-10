package com.miguel.gamescollection.repository;

import com.miguel.gamescollection.model.Platform;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlatformRepository extends JpaRepository<Platform, Integer> {
}