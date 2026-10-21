package com.fullstack.fullstack.dto;

import com.fullstack.fullstack.model.entity.Estudiante;
import com.fullstack.fullstack.model.entity.Grupo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** DTOs de grupos y matrículas. */
public final class GrupoDtos {

    private GrupoDtos() {
    }

    public record GrupoRequest(
            @NotNull(message = "La asignatura es obligatoria")
            Long asignaturaId,

            @NotBlank(message = "El nombre del grupo es obligatorio")
            @Size(max = 50, message = "El nombre del grupo no puede superar 50 caracteres")
            String nombre,

            @NotBlank(message = "El periodo es obligatorio")
            @Pattern(regexp = "^\\d{4}-[12]$", message = "El periodo debe tener formato AAAA-1 o AAAA-2")
            String periodo,

            @NotNull(message = "El docente es obligatorio")
            Long docenteId) {
    }

    public record GrupoResponse(Long id, Long asignaturaId, String asignaturaCodigo, String asignaturaNombre,
                                String nombre, String periodo, Long docenteId, String docenteNombre,
                                long estudiantes) {

        public static GrupoResponse from(Grupo g, long estudiantes) {
            return new GrupoResponse(g.getId(), g.getAsignatura().getId(), g.getAsignatura().getCodigo(),
                    g.getAsignatura().getNombre(), g.getNombre(), g.getPeriodo(), g.getDocente().getId(),
                    g.getDocente().getNombre(), estudiantes);
        }
    }

    /** Estudiante matriculado en un grupo. */
    public record MatriculadoResponse(Long estudianteId, String documento, String nombre, String curso) {

        public static MatriculadoResponse from(Estudiante e) {
            return new MatriculadoResponse(e.getId(), e.getDocumento(), e.getNombre(), e.getCurso());
        }
    }

    /** Totales del panel principal. */
    public record PanelResponse(long estudiantes, long asignaturas, long grupos, long calificaciones) {
    }
}
