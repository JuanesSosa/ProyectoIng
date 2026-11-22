package com.fullstack.fullstack.repository;

import com.fullstack.fullstack.model.entity.Estudiante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Acceso a datos de estudiantes. */
public interface EstudianteRepository extends JpaRepository<Estudiante, Long> {

    List<Estudiante> findAllByOrderByNombreAsc();

    /** Búsqueda por nombre, documento, código o correo (parcial y sin distinguir mayúsculas), HU-08. */
    List<Estudiante> findByNombreContainingIgnoreCaseOrDocumentoContainingIgnoreCaseOrCodigoContainingIgnoreCaseOrCorreoContainingIgnoreCaseOrderByNombreAsc(
            String nombre, String documento, String codigo, String correo);

    Optional<Estudiante> findByDocumento(String documento);

    boolean existsByDocumento(String documento);

    boolean existsByDocumentoAndIdNot(String documento, Long id);

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id);
}
