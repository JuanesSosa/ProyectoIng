package com.fullstack.fullstack.dto;

import com.fullstack.fullstack.model.entity.Asignatura;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** DTOs de gestión de asignaturas (RF-06). */
public final class AsignaturaDtos {

    private AsignaturaDtos() {
    }

    /** activa es opcional: si no viene, al crear queda activa y al editar se conserva el valor actual. */
    public record AsignaturaRequest(
            @NotBlank(message = "El código es obligatorio")
            @Size(max = 20, message = "El código no puede superar 20 caracteres")
            String codigo,

            @NotBlank(message = "El nombre es obligatorio")
            @Size(max = 120, message = "El nombre no puede superar 120 caracteres")
            String nombre,

            Boolean activa) {
    }

    public record AsignaturaResponse(Long id, String codigo, String nombre, boolean activa) {

        public static AsignaturaResponse from(Asignatura a) {
            return new AsignaturaResponse(a.getId(), a.getCodigo(), a.getNombre(), a.isActiva());
        }
    }
}
