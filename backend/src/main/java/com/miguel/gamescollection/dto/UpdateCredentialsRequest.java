package com.miguel.gamescollection.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateCredentialsRequest(
        @NotBlank(message = "El usuario es obligatorio")
        String username,

        @NotBlank(message = "La contraseña actual es obligatoria")
        String currentPassword,

        String newPassword
) {
}
