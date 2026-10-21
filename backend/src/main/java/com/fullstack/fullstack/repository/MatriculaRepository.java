package com.fullstack.fullstack.repository;

import com.fullstack.fullstack.model.entity.Matricula;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Acceso a datos de matrículas (estudiantes de cada grupo). */
public interface MatriculaRepository extends JpaRepository<Matricula, Long> {

    List<Matricula> findByGrupoIdOrderByEstudianteNombreAsc(Long grupoId);

    Optional<Matricula> findByGrupoIdAndEstudianteId(Long grupoId, Long estudianteId);

    boolean existsByGrupoIdAndEstudianteId(Long grupoId, Long estudianteId);

    boolean existsByGrupoId(Long grupoId);

    boolean existsByEstudianteId(Long estudianteId);

    long countByGrupoId(Long grupoId);
}
