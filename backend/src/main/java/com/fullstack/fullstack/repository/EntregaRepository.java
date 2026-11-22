package com.fullstack.fullstack.repository;

import com.fullstack.fullstack.model.entity.Entrega;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Acceso a datos de entregas (archivos subidos por los estudiantes). */
public interface EntregaRepository extends JpaRepository<Entrega, Long> {

    List<Entrega> findByActividadIdAndEstudianteIdOrderByIdAsc(Long actividadId, Long estudianteId);

    long countByActividadIdAndEstudianteId(Long actividadId, Long estudianteId);
}
