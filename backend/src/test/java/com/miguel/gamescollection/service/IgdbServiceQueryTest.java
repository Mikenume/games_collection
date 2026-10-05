package com.miguel.gamescollection.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IgdbServiceQueryTest {

    private static final String FIELDS =
            "fields name, first_release_date, cover.image_id, platforms.name; limit 10;";

    @Test
    void construyeLaConsulta() {
        assertEquals("search \"Silent Hill\"; " + FIELDS, IgdbService.buildQuery("  Silent Hill "));
    }

    @Test
    void escapaComillasYBarras() {
        assertEquals("search \"Say \\\"Hi\\\" \\\\o/\"; " + FIELDS,
                IgdbService.buildQuery("Say \"Hi\" \\o/"));
    }

    @Test
    void quitaLosSaltosDeLinea() {
        assertEquals("search \"Metal Gear Solid\"; " + FIELDS,
                IgdbService.buildQuery("Metal Gear\r\nSolid"));
    }
}
