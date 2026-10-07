package com.miguel.gamescollection.controller;

import com.miguel.gamescollection.dto.IgdbGameDto;
import com.miguel.gamescollection.service.IgdbService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Solo ADMIN y DEMO (ver SecurityConfig): cada búsqueda consume cuota de la API de IGDB
@RestController
@RequestMapping("/api/igdb")
public class IgdbController {

    private final IgdbService service;

    public IgdbController(IgdbService service) {
        this.service = service;
    }

    // GET /api/igdb/search?q=crash
    @GetMapping("/search")
    public List<IgdbGameDto> search(
            @RequestParam(required = false)
            @NotBlank(message = "Indica qué juego quieres buscar")
            @Size(max = 100, message = "La búsqueda no puede superar los 100 caracteres")
            String q) {
        return service.search(q);
    }
}
