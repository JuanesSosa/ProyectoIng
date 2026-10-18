package com.fullstack.fullstack.repository;

import com.fullstack.fullstack.model.entity.Nota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/** Acceso a datos de notas. */
public interface NotaRepository extends JpaRepository<Nota, Long> {

    /** Evaluaciones de un estudiante en una asignatura durante un periodo. */
    List<Nota> findByEstudianteIdAndAsignaturaIdAndPeriodoOrderByIdAsc(
            Long estudianteId, Long asignaturaId, String periodo);

    /** Todas las notas de un estudiante en un periodo, ordenadas por asignatura (carga la asignatura de una vez). */
    @Query("select n from Nota n join fetch n.asignatura a "
            + "where n.estudiante.id = :estudianteId and n.periodo = :periodo "
            + "order by a.nombre asc, n.id asc")
    List<Nota> findDelPeriodo(@Param("estudianteId") Long estudianteId, @Param("periodo") String periodo);

    boolean existsByEstudianteId(Long estudianteId);

    boolean existsByAsignaturaId(Long asignaturaId);

    /** Periodos en los que hay notas registradas, del más reciente al más antiguo. */
    @Query("select distinct n.periodo from Nota n order by n.periodo desc")
    List<String> findPeriodos();
}
