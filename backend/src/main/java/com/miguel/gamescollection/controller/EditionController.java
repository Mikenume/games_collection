package com.miguel.gamescollection.controller;

import com.miguel.gamescollection.dto.EditionDto;
import com.miguel.gamescollection.dto.EditionRequest;
import com.miguel.gamescollection.security.UserPrincipal;
import com.miguel.gamescollection.service.EditionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@RequestMapping("/api/editions")
public class EditionController {

    private final EditionService service;

    public EditionController(EditionService service) {
        this.service = service;
    }

    // Permite filtrar por plataforma, juego o si se posee, con query params opcionales.
    // Si se posee o no solo lo ve el admin: para el resto ni se filtra ni se devuelve.
    @GetMapping
    public List<EditionDto> findAll(@RequestParam(required = false) Integer platformId,
                                    @RequestParam(required = false) Integer gameId,
                                    @RequestParam(required = false) Boolean owned,
                                    @AuthenticationPrincipal UserPrincipal user) {
        boolean admin = isAdmin(user);
        List<EditionDto> editions;
        if (platformId != null) {
            editions = service.findByPlatform(platformId);
        } else if (gameId != null) {
            editions = service.findByGame(gameId);
        } else if (admin && Boolean.TRUE.equals(owned)) {
            editions = service.findOwned();
        } else {
            editions = service.findAll();
        }
        return admin ? editions : editions.stream().map(EditionDto::withoutOwned).toList();
    }

    @GetMapping("/{id}")
    public EditionDto findById(@PathVariable Integer id,
                               @AuthenticationPrincipal UserPrincipal user) {
        EditionDto edition = service.findById(id);
        return isAdmin(user) ? edition : edition.withoutOwned();
    }

    @PostMapping
    public ResponseEntity<EditionDto> create(@Valid @RequestBody EditionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public EditionDto update(@PathVariable Integer id, @Valid @RequestBody EditionRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    private static boolean isAdmin(UserPrincipal user) {
        return user != null && user.isAdmin();
    }
}
