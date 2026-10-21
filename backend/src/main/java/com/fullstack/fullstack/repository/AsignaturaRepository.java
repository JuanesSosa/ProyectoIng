package com.fullstack.fullstack.repository;

import com.fullstack.fullstack.model.entity.Asignatura;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Acceso a datos de asignaturas. */
public interface AsignaturaRepository extends JpaRepository<Asignatura, Long> {

    List<Asignatura> findAllByOrderByNombreAsc();

    List<Asignatura> findAllByActivaTrueOrderByNombreAsc();

    Optional<Asignatura> findByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id);
}
