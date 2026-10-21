package com.fullstack.fullstack.dto;

import com.fullstack.fullstack.model.entity.Actividad;
import com.fullstack.fullstack.model.entity.Criterio;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/** DTOs de actividades, rúbricas, calificaciones y progreso. */
public final class ActividadDtos {

    private ActividadDtos() {
    }

    // ---------- actividades y rúbricas ----------

    public record ActividadRequest(
            @NotBlank(message = "El nombre de la actividad es obligatorio")
            @Size(max = 100, message = "El nombre de la actividad no puede superar 100 caracteres")
            String nombre,

            @NotNull(message = "El porcentaje de la actividad es obligatorio")
            @Min(value = 1, message = "El porcentaje debe ser al menos 1")
            @Max(value = 100, message = "El porcentaje no puede superar 100")
            Integer peso) {
    }

    /** id es null para un criterio nuevo; con id se actualiza el existente. */
    public record CriterioRequest(
            Long id,

            @NotBlank(message = "El nombre del criterio es obligatorio")
            @Size(max = 100, message = "El nombre del criterio no puede superar 100 caracteres")
            String nombre,

            @NotNull(message = "El porcentaje del criterio es obligatorio")
            @Min(value = 1, message = "El porcentaje del criterio debe ser al menos 1")
            @Max(value = 100, message = "El porcentaje del criterio no puede superar 100")
            Integer peso) {
    }

    /** Rúbrica completa de la actividad: reemplaza a la anterior. */
    public record RubricaRequest(
            @NotEmpty(message = "La rúbrica necesita al menos un criterio")
            @Size(max = 20, message = "La rúbrica admite máximo 20 criterios")
            @Valid
            List<CriterioRequest> criterios) {
    }

    public record CriterioResponse(Long id, String nombre, Integer peso) {

        public static CriterioResponse from(Criterio c) {
            return new CriterioResponse(c.getId(), c.getNombre(), c.getPeso());
        }
    }

    public record ActividadResponse(Long id, Long grupoId, String nombre, Integer peso,
                                    List<CriterioResponse> criterios, int pesoCriterios) {

        public static ActividadResponse from(Actividad a, List<Criterio> criterios) {
            return new ActividadResponse(a.getId(), a.getGrupo().getId(), a.getNombre(), a.getPeso(),
                    criterios.stream().map(CriterioResponse::from).toList(),
                    criterios.stream().mapToInt(Criterio::getPeso).sum());
        }
    }

    // ---------- calificaciones ----------

    /** valor null = quitar la nota de ese criterio. */
    public record CalificacionRequest(
            @NotNull(message = "El criterio es obligatorio")
            Long criterioId,

            @DecimalMin(value = "0.0", message = "La nota debe estar entre 0.0 y 5.0")
            @DecimalMax(value = "5.0", message = "La nota debe estar entre 0.0 y 5.0")
            @Digits(integer = 1, fraction = 2, message = "La nota admite máximo 2 decimales")
            BigDecimal valor) {
    }

    public record CalificacionesRequest(
            @NotEmpty(message = "Debes enviar al menos una calificación")
            @Size(max = 20, message = "Máximo 20 calificaciones por solicitud")
            @Valid
            List<CalificacionRequest> calificaciones) {
    }

    // ---------- progreso ----------

    /** Un criterio con la nota del estudiante (valor null = sin calificar). */
    public record CriterioNota(Long criterioId, String nombre, Integer peso, BigDecimal valor) {
    }

    /** Rúbrica de una actividad con las notas de un estudiante; nota = promedio de lo calificado. */
    public record RubricaEstudianteResponse(Long actividadId, String actividadNombre, Integer actividadPeso,
                                            Long estudianteId, String estudianteNombre,
                                            List<CriterioNota> criterios, BigDecimal nota, int avance) {
    }

    /** Estudiante del grupo con su nota y avance en una actividad. */
    public record EstudianteActividad(Long estudianteId, String documento, String nombre,
                                      BigDecimal nota, int avance) {
    }

    public record ActividadResumen(Long actividadId, String nombre, Integer peso, List<CriterioNota> criterios,
                                   BigDecimal nota, int avance) {
    }

    /**
     * Seguimiento de un estudiante en un grupo.
     * promedio: promedio de lo calificado hasta ahora; acumulado: puntos ganados sobre 5.0;
     * avance: porcentaje de la asignatura ya evaluado; pesoActividades: suma de los % de las actividades definidas.
     */
    public record ResumenGrupoResponse(Long grupoId, Long asignaturaId, String asignaturaNombre, String grupoNombre,
                                       String periodo, Long estudianteId, String estudianteDocumento,
                                       String estudianteNombre, List<ActividadResumen> actividades,
                                       int pesoActividades, BigDecimal promedio, BigDecimal acumulado, int avance) {
    }
}
