package com.fullstack.fullstack.dto;

import com.fullstack.fullstack.model.entity.Estudiante;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** DTOs de gestión de estudiantes (RF-03, RF-04, RF-05). */
public final class EstudianteDtos {

    private EstudianteDtos() {
    }

    public record EstudianteRequest(
            @NotBlank(message = "El documento es obligatorio")
            @Size(max = 30, message = "El documento no puede superar 30 caracteres")
            String documento,

            @NotBlank(message = "El nombre es obligatorio")
            @Size(max = 120, message = "El nombre no puede superar 120 caracteres")
            String nombre,

            @NotBlank(message = "El curso es obligatorio")
            @Size(max = 30, message = "El curso no puede superar 30 caracteres")
            String curso) {
    }

    public record EstudianteResponse(Long id, String documento, String nombre, String curso) {

        public static EstudianteResponse from(Estudiante e) {
            return new EstudianteResponse(e.getId(), e.getDocumento(), e.getNombre(), e.getCurso());
        }
    }
}
