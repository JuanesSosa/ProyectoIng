package com.fullstack.fullstack.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Seguridad de la API (RNF-01, RNF-08).
 * - Autenticación: token JWT en la cabecera "Authorization: Bearer ...". No hay sesiones en el servidor.
 * - Autorización: cada ruta exige un rol. Además, en NotaController un ESTUDIANTE solo ve sus propias notas.
 * - CORS: solo se permite el origen del frontend Angular.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /** Origen permitido del frontend (app.cors.allowed-origin). */
    @Value("${app.cors.allowed-origin}")
    private String allowedOrigin;

    /** Cifrado de contraseñas con BCrypt. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Sin cookies de sesión, el ataque CSRF no aplica: la autenticación va en una cabecera.
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        // Usuarios: solo el administrador (RF-02)
                        .requestMatchers("/api/usuarios/**").hasRole("ADMIN")
                        // Estudiantes y asignaturas: consultar = admin y docente; crear/editar/borrar = admin
                        .requestMatchers(HttpMethod.GET, "/api/estudiantes/**").hasAnyRole("ADMIN", "DOCENTE")
                        .requestMatchers("/api/estudiantes/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/asignaturas/**").hasAnyRole("ADMIN", "DOCENTE")
                        .requestMatchers("/api/asignaturas/**").hasRole("ADMIN")
                        // Notas: solo el docente registra o modifica (HU-04, HU-05); consultar = cualquier usuario
                        // autenticado (el controlador limita al estudiante a sus propias notas)
                        .requestMatchers(HttpMethod.PUT, "/api/notas/registro").hasRole("DOCENTE")
                        .requestMatchers(HttpMethod.GET, "/api/notas/**").authenticated()
                        // Panel con totales
                        .requestMatchers("/api/panel").hasAnyRole("ADMIN", "DOCENTE")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
        return http.build();
    }

    /** Convierte el claim "rol" del token (por ejemplo "DOCENTE") en la autoridad ROLE_DOCENTE. */
    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName("rol");
        authorities.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }

    /** CORS: sin esto el navegador bloquearía las peticiones de http://localhost:4200 hacia este backend. */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(allowedOrigin));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
