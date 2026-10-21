package com.fullstack.fullstack.repository;

import com.fullstack.fullstack.model.entity.Actividad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Acceso a datos de actividades. */
public interface ActividadRepository extends JpaRepository<Actividad, Long> {

    List<Actividad> findByGrupoIdOrderByIdAsc(Long grupoId);

    boolean existsByGrupoId(Long grupoId);
}
