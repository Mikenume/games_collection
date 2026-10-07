package com.miguel.gamescollection.dto;

// Cupo diario de juegos para el conjunto de cuentas DEMO
public record DemoQuotaDto(
        long createdToday,
        int limit,
        long remaining
) {
}
