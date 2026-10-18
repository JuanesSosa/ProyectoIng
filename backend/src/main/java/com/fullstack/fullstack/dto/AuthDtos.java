package com.fullstack.fullstack.dto;

import com.fullstack.fullstack.model.entity.Rol;
import jakarta.validation.constraints.NotBlank;

/** DTOs de autenticación (RF-01). */
public final class AuthDtos {

    private AuthDtos() {
    }

    /** Cuerpo de POST /api/auth/login. */
    public record LoginRequest(
            @NotBlank(message = "El correo es obligatorio") String correo,
            @NotBlank(message = "La contraseña es obligatoria") String password) {
    }

    /**
     * Respuesta del login: el token JWT que el frontend debe enviar en la cabecera
     * "Authorization: Bearer ..." y los datos básicos del usuario para armar la interfaz.
     */
    public record LoginResponse(
            String token,
            String tipo,
            long expiraEnSegundos,
            String nombre,
            String correo,
            Rol rol,
            Long estudianteId) {
    }
}
