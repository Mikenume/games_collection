package com.miguel.gamescollection.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(CorsConfigurationSource corsConfigurationSource) {
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            CustomUserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .securityContext(context -> context.securityContextRepository(securityContextRepository()))
                // Sin sesión: 401 (por defecto Spring devolvería 403, igual que sin permisos)
                .exceptionHandling(ex -> ex.authenticationEntryPoint(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                // Invalida la sesión en el servidor y responde 204, sin redirigir
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT)))
                // El orden importa: gana la primera regla que encaja, así que
                // las rutas concretas van antes que los comodines
                .authorizeHttpRequests(auth -> auth
                        // Página de error de Spring: si no, un 400 o un 404 acabarían en 401
                        .requestMatchers("/error").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/auth/me").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/demo").permitAll()
                        // La cuenta DEMO es compartida: no puede cambiar usuario ni contraseña
                        .requestMatchers(HttpMethod.PUT, "/api/auth/me").hasRole("ADMIN")

                        // Buscar en IGDB gasta cuota de la API: solo con sesión (la demo
                        // también, para poner carátula a lo que envía)
                        .requestMatchers("/api/igdb/**").hasAnyRole("ADMIN", "DEMO")

                        // DEMO también lo ve (solo lectura) para no reenviar un juego que ya está pendiente
                        .requestMatchers(HttpMethod.GET, "/api/games/pending").hasAnyRole("ADMIN", "DEMO")
                        .requestMatchers(HttpMethod.GET, "/api/games/demo-quota").hasAnyRole("ADMIN", "DEMO")
                        .requestMatchers(HttpMethod.GET,
                                "/api/games/**", "/api/editions/**", "/api/platforms/**", "/api/genres/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/games").hasAnyRole("ADMIN", "DEMO")
                        .requestMatchers("/api/games/**", "/api/editions/**",
                                "/api/platforms/**", "/api/genres/**").hasRole("ADMIN")

                        .requestMatchers("/actuator/**").hasRole("ADMIN")
                        .anyRequest().denyAll());

        return http.build();
    }
}
