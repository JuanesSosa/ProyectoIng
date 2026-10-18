package com.fullstack.fullstack.dto;

import com.fullstack.fullstack.model.entity.Nota;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/** DTOs de registro y consulta de notas (RF-07 a RF-11). */
public final class NotaDtos {

    private NotaDtos() {
    }

    /** Una evaluación dentro del registro: "Parcial", peso 30 (%), nota 4.2. */
    public record EvaluacionRequest(
            @NotBlank(message = "El nombre de la evaluación es obligatorio")
            @Size(max = 100, message = "El nombre de la evaluación no puede superar 100 caracteres")
            String evaluacion,

            @Min(value = 1, message = "El peso debe ser al menos 1 %")
            @Max(value = 100, message = "El peso no puede superar 100 %")
            Integer peso,

            @NotNull(message = "La nota es obligatoria")
            @DecimalMin(value = "0.0", message = "La nota no puede ser menor que 0.0")
            @DecimalMax(value = "5.0", message = "La nota no puede ser mayor que 5.0")
            @Digits(integer = 1, fraction = 2, message = "La nota admite máximo 2 decimales")
            BigDecimal valor) {
    }

    /**
     * Cuerpo de PUT /api/notas/registro: el conjunto completo de evaluaciones de un estudiante en una
     * asignatura y periodo (es la pantalla "Registro de calificaciones": se guardan todas con un botón).
     * Las evaluaciones que ya existían y no vienen en la lista se eliminan.
     */
    public record RegistroNotasRequest(
            @NotNull(message = "El estudiante es obligatorio") Long estudianteId,

            @NotNull(message = "La asignatura es obligatoria") Long asignaturaId,

            @NotBlank(message = "El periodo es obligatorio")
            @Pattern(regexp = "^\\d{4}-[12]$",
                    message = "El periodo debe tener el formato AAAA-1 o AAAA-2 (por ejemplo 2026-2)")
            String periodo,

            @NotEmpty(message = "Debes registrar al menos una evaluación")
            @Size(max = 20, message = "Máximo 20 evaluaciones por asignatura")
            @Valid
            List<EvaluacionRequest> evaluaciones) {
    }

    public record NotaResponse(
            Long id,
            Long estudianteId,
            Long asignaturaId,
            String periodo,
            String evaluacion,
            Integer peso,
            BigDecimal valor) {

        public static NotaResponse from(Nota n) {
            return new NotaResponse(n.getId(), n.getEstudiante().getId(), n.getAsignatura().getId(),
                    n.getPeriodo(), n.getEvaluacion(), n.getPeso(), n.getValor());
        }
    }

    /** Resultado de guardar un registro: las notas guardadas y su promedio. */
    public record RegistroNotasResponse(
            Long estudianteId,
            Long asignaturaId,
            String periodo,
            List<NotaResponse> notas,
            BigDecimal promedio,
            int pesoTotal) {
    }

    /** Una fila de la pantalla "Consulta de notas": una asignatura con sus notas y su promedio. */
    public record AsignaturaResumen(
            Long asignaturaId,
            String codigo,
            String nombre,
            List<NotaResponse> notas,
            BigDecimal promedio,
            int pesoTotal) {
    }

    /** Respuesta de GET /api/notas/resumen: todas las asignaturas del periodo y el promedio general. */
    public record ResumenNotasResponse(
            Long estudianteId,
            String estudianteNombre,
            String periodo,
            List<AsignaturaResumen> asignaturas,
            BigDecimal promedioGeneral) {
    }

    /** Totales para el panel principal. */
    public record PanelResponse(long estudiantes, long asignaturas, long notas) {
    }
}
