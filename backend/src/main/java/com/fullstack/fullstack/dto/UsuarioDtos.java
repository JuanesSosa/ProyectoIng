package com.fullstack.fullstack.dto;

import com.fullstack.fullstack.model.entity.Rol;
import com.fullstack.fullstack.model.entity.Usuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** DTOs de gestión de usuarios (RF-02). La contraseña nunca se devuelve. */
public final class UsuarioDtos {

    private UsuarioDtos() {
    }

    /**
     * Datos para crear o editar un usuario.
     * password es obligatoria al crear; al editar, si viene vacía se conserva la actual.
     * estudianteId solo aplica al rol ESTUDIANTE.
     */
    public record UsuarioRequest(
            @NotBlank(message = "El nombre es obligatorio")
            @Size(max = 120, message = "El nombre no puede superar 120 caracteres")
            String nombre,

            @NotBlank(message = "El correo es obligatorio")
            @Email(message = "El correo no tiene un formato válido")
            @Size(max = 150, message = "El correo no puede superar 150 caracteres")
            String correo,

            @Size(min = 8, max = 64, message = "La contraseña debe tener entre 8 y 64 caracteres")
            String password,

            @NotNull(message = "El rol es obligatorio")
            Rol rol,

            Long estudianteId,

            Boolean activo) {
    }

    public record UsuarioResponse(
            Long id,
            String nombre,
            String correo,
            Rol rol,
            boolean activo,
            Long estudianteId,
            String estudianteNombre) {

        public static UsuarioResponse from(Usuario u) {
            Long estudianteId = u.getEstudiante() != null ? u.getEstudiante().getId() : null;
            String estudianteNombre = u.getEstudiante() != null ? u.getEstudiante().getNombre() : null;
            return new UsuarioResponse(u.getId(), u.getNombre(), u.getCorreo(), u.getRol(),
                    u.isActivo(), estudianteId, estudianteNombre);
        }
    }
}
