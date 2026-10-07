package com.miguel.gamescollection.model;

// Los juegos que crea una cuenta DEMO entran como PENDIENTE y solo los ve
// el admin; al aprobarlos pasan a APROBADO y aparecen en la estantería
public enum GameStatus {
    PENDIENTE,
    APROBADO
}
