package com.miguel.gamescollection.controller;

import com.miguel.gamescollection.dto.LoginRequest;
import com.miguel.gamescollection.dto.LoginResponse;
import com.miguel.gamescollection.dto.UpdateCredentialsRequest;
import com.miguel.gamescollection.security.UserPrincipal;
import com.miguel.gamescollection.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// El logout no está aquí: lo hace Spring Security en POST /api/auth/logout (ver SecurityConfig)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager,
                           SecurityContextRepository securityContextRepository,
                           UserService userService) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.userService = userService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request,
                                HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        saveInSession(authentication, httpRequest, httpResponse);
        return toResponse((UserPrincipal) authentication.getPrincipal());
    }

    // Botón "Probar como demo": entra en la cuenta DEMO compartida sin contraseña
    @PostMapping("/demo")
    public LoginResponse demo(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        UserPrincipal principal = UserPrincipal.from(userService.findOrCreateDemoUser());
        principal.eraseCredentials();
        saveInSession(authenticated(principal), httpRequest, httpResponse);
        return toResponse(principal);
    }

    // Quién tiene la sesión abierta; el frontend lo pregunta al arrancar
    @GetMapping("/me")
    public ResponseEntity<LoginResponse> me(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(toResponse(principal));
    }

    // Solo ADMIN (ver SecurityConfig). Si cambia el nombre de usuario, se
    // actualiza también el de la sesión para que /me no devuelva el antiguo.
    @PutMapping("/me")
    public LoginResponse updateCredentials(@Valid @RequestBody UpdateCredentialsRequest request,
                                            @AuthenticationPrincipal UserPrincipal principal,
                                            HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        UserPrincipal updated = UserPrincipal.from(userService.updateCredentials(principal.getId(), request));
        updated.eraseCredentials();
        saveInSession(authenticated(updated), httpRequest, httpResponse);
        return toResponse(updated);
    }

    private static Authentication authenticated(UserPrincipal principal) {
        return UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities());
    }

    private void saveInSession(Authentication authentication,
                               HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);
    }

    private static LoginResponse toResponse(UserPrincipal principal) {
        return new LoginResponse(principal.getUsername(), principal.getRole());
    }
}
