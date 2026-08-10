package com.miguel.gamescollection.controller;

import com.miguel.gamescollection.model.Genre;
import com.miguel.gamescollection.repository.GenreRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/genres")

public class GenreController {

    private final GenreRepository repository;

    public GenreController(GenreRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Genre> findAll() {
        return repository.findAll();
    }
}
