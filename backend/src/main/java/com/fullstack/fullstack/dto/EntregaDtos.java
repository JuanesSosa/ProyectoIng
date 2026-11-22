package com.fullstack.fullstack.dto;

import com.fullstack.fullstack.model.entity.Entrega;

/** DTOs de entregas (archivos subidos por los estudiantes). */
public final class EntregaDtos {

    private EntregaDtos() {
    }

    /** fecha va como texto ISO-8601 (por ejemplo 2026-11-22T16:05:00Z). */
    public record EntregaResponse(Long id, Long actividadId, Long estudianteId, String estudianteNombre,
                                  String nombre, long tamano, String fecha) {

        public static EntregaResponse from(Entrega e) {
            return new EntregaResponse(e.getId(), e.getActividad().getId(), e.getEstudiante().getId(),
                    e.getEstudiante().getNombre(), e.getNombre(), e.getTamano(), e.getFecha().toString());
        }
    }
}
