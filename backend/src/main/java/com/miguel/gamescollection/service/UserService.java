package com.miguel.gamescollection.service;

import com.miguel.gamescollection.config.DemoProperties;
import com.miguel.gamescollection.dto.UpdateCredentialsRequest;
import com.miguel.gamescollection.exception.ResourceNotFoundException;
import com.miguel.gamescollection.model.Role;
import com.miguel.gamescollection.model.User;
import com.miguel.gamescollection.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final DemoProperties demoProperties;

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder,
                       DemoProperties demoProperties) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.demoProperties = demoProperties;
    }

    @Transactional
    public User updateCredentials(Integer userId, UpdateCredentialsRequest request) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("el usuario", userId));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("La contraseña actual no es correcta");
        }

        user.setUsername(request.username());

        if (request.newPassword() != null && !request.newPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.newPassword()));
        }

        return user;
    }

    // Usuario compartido del botón "Probar como demo". Se entra sin contraseña,
    // así que la que se guarda es aleatoria: nadie la conoce ni hace falta
    // escribirla en ningún sitio. Se crea la primera vez que alguien entra.
    @Transactional
    public User findOrCreateDemoUser() {
        User user = repository.findByUsername(demoProperties.username())
                .orElseGet(() -> repository.save(new User(
                        demoProperties.username(),
                        passwordEncoder.encode(UUID.randomUUID().toString()),
                        Role.DEMO,
                        true)));

        // Si el admin la desactiva (enabled = false) se deja de poder entrar,
        // y nunca se entra sin contraseña en una cuenta que no sea DEMO
        if (user.getRole() != Role.DEMO || !user.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "La cuenta demo no está disponible en este momento");
        }
        return user;
    }
}
