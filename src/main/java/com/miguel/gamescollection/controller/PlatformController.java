package com.miguel.gamescollection.controller;

import com.miguel.gamescollection.model.Platform;
import com.miguel.gamescollection.repository.PlatformRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/platforms")
public class PlatformController {

    private final PlatformRepository repository;

    public PlatformController(PlatformRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Platform> findAll() {
        return repository.findAll();
    }
}