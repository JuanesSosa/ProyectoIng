package com.fullstack.fullstack.repository;

import com.fullstack.fullstack.model.entity.Grupo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/** Acceso a datos de grupos. */
public interface GrupoRepository extends JpaRepository<Grupo, Long> {

    @Query("select g from Grupo g join fetch g.asignatura a join fetch g.docente d "
            + "order by a.nombre, g.periodo desc, g.nombre")
    List<Grupo> listarTodos();

    @Query("select g from Grupo g join fetch g.asignatura a join fetch g.docente d where d.id = :docenteId "
            + "order by a.nombre, g.periodo desc, g.nombre")
    List<Grupo> listarDelDocente(@Param("docenteId") Long docenteId);

    @Query("select g from Matricula m join m.grupo g join fetch g.asignatura a join fetch g.docente d "
            + "where m.estudiante.id = :estudianteId order by a.nombre, g.periodo desc, g.nombre")
    List<Grupo> listarDelEstudiante(@Param("estudianteId") Long estudianteId);

    boolean existsByAsignaturaIdAndPeriodoAndNombreIgnoreCase(Long asignaturaId, String periodo, String nombre);

    boolean existsByAsignaturaIdAndPeriodoAndNombreIgnoreCaseAndIdNot(
            Long asignaturaId, String periodo, String nombre, Long id);
}
