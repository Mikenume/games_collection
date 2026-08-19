package com.miguel.gamescollection.controller;

import com.miguel.gamescollection.dto.GameDto;
import com.miguel.gamescollection.dto.GameRequest;
import com.miguel.gamescollection.dto.GameSummaryDto;
import com.miguel.gamescollection.service.GameService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService service;

    public GameController(GameService service) {
        this.service = service;
    }

    /*
     * GET /api/games            -> todos los juegos
     * GET /api/games?title=zel  -> filtrados por título
     *
     * required = false hace que el parámetro sea opcional
     */
    @GetMapping
    public List<GameSummaryDto> findAll(@RequestParam(required = false) String title) {
        if (title != null && !title.isBlank()) {
            return service.searchByTitle(title);
        }
        return service.findAll();
    }

    @GetMapping("/{id}")
    public GameDto findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<GameDto> create(@Valid @RequestBody GameRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public GameDto update(@PathVariable Integer id, @Valid @RequestBody GameRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
