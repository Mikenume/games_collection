package com.miguel.gamescollection.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/*
 * El navegador bloquea por defecto las peticiones entre orígenes distintos.
 * React en el puerto 5173 y esta API en el 8080 son orígenes distintos,
 * así que sin esta configuración el frontend recibiría un error de CORS.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                        "http://localhost:5173",   // Vite
                        "http://localhost:3000"    // Create React App
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
