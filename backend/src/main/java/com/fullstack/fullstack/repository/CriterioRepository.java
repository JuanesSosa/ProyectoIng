package com.fullstack.fullstack.repository;

import com.fullstack.fullstack.model.entity.Criterio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/** Acceso a datos de los criterios de las rúbricas. */
public interface CriterioRepository extends JpaRepository<Criterio, Long> {

    List<Criterio> findByActividadIdOrderByIdAsc(Long actividadId);

    @Query("select c from Criterio c where c.actividad.grupo.id = :grupoId order by c.id")
    List<Criterio> findDelGrupo(@Param("grupoId") Long grupoId);
}
