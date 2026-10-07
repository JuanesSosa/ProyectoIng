package com.fullstack.fullstack.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuración de seguridad y CORS del backend.
 * Permite que el frontend Angular (localhost:4200) pueda consumir la API sin ser bloqueado.
 */
@Configuration  // Indica que esta clase define beans de configuración que Spring debe cargar.
@EnableWebSecurity  // Activa la seguridad web de Spring Security en la aplicación.
public class SecurityConfig {

    /** Origen permitido del frontend, configurable en application.properties (app.cors.allowed-origin). */
    @Value("${app.cors.allowed-origin}")
    private String allowedOrigin;

    /**
     * Define la cadena de filtros de seguridad para las peticiones HTTP.
     * - CSRF deshabilitado: en desarrollo con API REST no usamos formularios HTML tradicionales.
     * - CORS: se usa la configuración personalizada para permitir el origen del frontend.
     * - authorizeHttpRequests: todas las rutas /api/** y el resto se permiten sin autenticación (desarrollo).
     */
    @Bean  // Registra el retorno de este método como un bean en el contexto de Spring (SecurityFilterChain).
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                // Solo desarrollo: todo abierto. Antes de producción, proteger /api/** con autenticación.
                .anyRequest().permitAll()
            );
        return http.build();
    }

    /**
     * Configuración CORS (Cross-Origin Resource Sharing).
     * Sin esto, el navegador bloquearía las peticiones desde http://localhost:4200 hacia este backend.
     */
    @Bean  // Registra la configuración CORS como bean para que Spring Security la use.
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Origen permitido: la URL donde corre el frontend Angular
        configuration.setAllowedOrigins(List.of(allowedOrigin));
        // Métodos HTTP que puede usar el frontend (GET, POST, PUT, DELETE, OPTIONS para preflight)
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // Cabeceras permitidas (Authorization, Content-Type, etc.)
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
