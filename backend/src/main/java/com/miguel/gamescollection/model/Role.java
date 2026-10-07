package com.miguel.gamescollection.model;

// Se guarda sin prefijo (ADMIN, DEMO); Spring Security le añade "ROLE_"
// al crear la autoridad (ver UserPrincipal)
public enum Role {
    ADMIN,
    DEMO
}
