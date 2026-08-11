package com.miguel.gamescollection.controller;

import com.miguel.gamescollection.dto.PlatformDto;
import com.miguel.gamescollection.dto.PlatformRequest;
import com.miguel.gamescollection.service.PlatformService;
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
@RequestMapping("/api/platforms")
public class PlatformController {

    private final PlatformService service;

    public PlatformController(PlatformService service) {
        this.service = service;
    }

    @GetMapping
    public List<PlatformDto> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public PlatformDto findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<PlatformDto> create(@Valid @RequestBody PlatformRequest request) {
        PlatformDto created = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public PlatformDto update(@PathVariable Integer id,
                              @Valid @RequestBody PlatformRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
