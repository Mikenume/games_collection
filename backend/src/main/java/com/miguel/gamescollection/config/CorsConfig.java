/* =========================================================
 *  Va en:  backend/src/main/java/com/miguel/gamescollection/config/CorsConfig.java
 *
 *  IMPORTANTE: si ya tienes CORS configurado en otro sitio
 *  (una clase parecida, o anotaciones @CrossOrigin en los
 *  controladores), QUITA lo anterior. Dos configuraciones de
 *  CORS a la vez se pisan y el resultado es impredecible.
 *
 *  La gracia de esta versión es que los orígenes permitidos
 *  no están escritos a fuego: se leen de una propiedad. En
 *  local vale localhost:5173, y en Render pones la URL real
 *  desde el panel sin tocar el código.
 * ========================================================= */

package com.miguel.gamescollection.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    // Lee app.cors.allowed-origins del .properties.
    // El valor tras los dos puntos es el que se usa si no existe.
    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:3000}")
    private String[] allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }
}
