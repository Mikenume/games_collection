package com.miguel.gamescollection.controller;

import com.miguel.gamescollection.dto.DemoQuotaDto;
import com.miguel.gamescollection.dto.GameDto;
import com.miguel.gamescollection.dto.GameRequest;
import com.miguel.gamescollection.dto.GameSummaryDto;
import com.miguel.gamescollection.dto.PendingGameDto;
import com.miguel.gamescollection.security.UserPrincipal;
import com.miguel.gamescollection.service.GameService;
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
@RequestMapping("/api/games")
public class GameController {

    private final GameService service;

    public GameController(GameService service) {
        this.service = service;
    }

    /*
     * Solo juegos aprobados (los de la estantería)
     * GET /api/games            -> todos los juegos
     * GET /api/games?title=zel  -> filtrados por título
     *
     * required = false hace que el parámetro sea opcional
     */
    @GetMapping
    public List<GameSummaryDto> findAll(@RequestParam(required = false) String title,
                                        @AuthenticationPrincipal UserPrincipal user) {
        List<GameSummaryDto> games = (title != null && !title.isBlank())
                ? service.searchByTitle(title)
                : service.findAll();
        return isAdmin(user) ? games : games.stream().map(GameSummaryDto::withoutOwned).toList();
    }

    // Pendiente -> 404 salvo para el admin
    @GetMapping("/{id}")
    public GameDto findById(@PathVariable Integer id,
                            @AuthenticationPrincipal UserPrincipal user) {
        GameDto game = service.findById(id, user);
        return isAdmin(user) ? game : game.withoutOwned();
    }

    // ADMIN y DEMO (ver SecurityConfig); DEMO solo lo consulta
    @GetMapping("/pending")
    public List<PendingGameDto> findPending() {
        return service.findPending();
    }

    // ADMIN y DEMO: juegos que les quedan hoy a las cuentas DEMO
    @GetMapping("/demo-quota")
    public DemoQuotaDto demoQuota() {
        return service.demoQuota();
    }

    // ADMIN y DEMO. Lo de DEMO queda pendiente de revisión.
    @PostMapping
    public ResponseEntity<GameDto> create(@Valid @RequestBody GameRequest request,
                                          @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request, user));
    }

    // Solo ADMIN. Editar un pendiente (PUT /{id}) no lo aprueba: hace falta esto.
    @PutMapping("/{id}/approve")
    public GameDto approve(@PathVariable Integer id) {
        return service.approve(id);
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

    // Lo que tengo y lo que no solo se le enseña al admin
    private static boolean isAdmin(UserPrincipal user) {
        return user != null && user.isAdmin();
    }
}
