package com.miguel.gamescollection.dto;

import com.miguel.gamescollection.model.Role;

// Usuario con sesión: lo devuelven el login, el acceso demo y GET /api/auth/me
public record LoginResponse(
        String username,
        Role role
) {
}
