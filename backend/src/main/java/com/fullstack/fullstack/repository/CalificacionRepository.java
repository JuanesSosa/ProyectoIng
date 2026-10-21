package com.fullstack.fullstack.repository;

import com.fullstack.fullstack.model.entity.Calificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/** Acceso a datos de calificaciones. */
public interface CalificacionRepository extends JpaRepository<Calificacion, Long> {

    /** Calificaciones de un estudiante en los criterios de una actividad. */
    @Query("select c from Calificacion c where c.estudiante.id = :estudianteId and c.criterio.actividad.id = :actividadId")
    List<Calificacion> findDeEstudianteEnActividad(@Param("estudianteId") Long estudianteId,
                                                   @Param("actividadId") Long actividadId);

    /** Calificaciones de todos los estudiantes en una actividad. */
    @Query("select c from Calificacion c where c.criterio.actividad.id = :actividadId")
    List<Calificacion> findDeActividad(@Param("actividadId") Long actividadId);

    /** Calificaciones de todos los estudiantes en un grupo. */
    @Query("select c from Calificacion c where c.criterio.actividad.grupo.id = :grupoId")
    List<Calificacion> findDeGrupo(@Param("grupoId") Long grupoId);

    @Query("select count(c) > 0 from Calificacion c where c.criterio.id = :criterioId")
    boolean existeEnCriterio(@Param("criterioId") Long criterioId);

    @Query("select count(c) > 0 from Calificacion c where c.criterio.actividad.id = :actividadId")
    boolean existeEnActividad(@Param("actividadId") Long actividadId);

    @Query("select count(c) > 0 from Calificacion c "
            + "where c.estudiante.id = :estudianteId and c.criterio.actividad.grupo.id = :grupoId")
    boolean existeDeEstudianteEnGrupo(@Param("estudianteId") Long estudianteId, @Param("grupoId") Long grupoId);
}
