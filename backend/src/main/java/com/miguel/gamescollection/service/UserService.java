package com.miguel.gamescollection.service;

import com.miguel.gamescollection.dto.LoginResponse;
import com.miguel.gamescollection.dto.UpdateCredentialsRequest;
import com.miguel.gamescollection.exception.ResourceNotFoundException;
import com.miguel.gamescollection.model.User;
import com.miguel.gamescollection.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public LoginResponse updateCredentials(String currentUsername, UpdateCredentialsRequest request) {
        User user = repository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("No se ha encontrado el usuario " + currentUsername));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("La contraseña actual no es correcta");
        }

        user.setUsername(request.username());

        if (request.newPassword() != null && !request.newPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.newPassword()));
        }

        return new LoginResponse(user.getUsername(), List.of(user.getRole()));
    }
}
