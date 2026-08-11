package com.miguel.gamescollection.controller;

import com.miguel.gamescollection.dto.GenreDto;
import com.miguel.gamescollection.dto.GenreRequest;
import com.miguel.gamescollection.service.GenreService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/genres")
public class GenreController {

    private final GenreService service;

    public GenreController(GenreService service) {
        this.service = service;
    }

    @GetMapping
    public List<GenreDto> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public GenreDto findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<GenreDto> create(@Valid @RequestBody GenreRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public GenreDto update(@PathVariable Integer id, @Valid @RequestBody GenreRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
