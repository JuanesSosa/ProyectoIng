package com.fullstack.fullstack.repository;

import com.fullstack.fullstack.model.entity.Estudiante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Acceso a datos de estudiantes. */
public interface EstudianteRepository extends JpaRepository<Estudiante, Long> {

    List<Estudiante> findAllByOrderByNombreAsc();

    /** Búsqueda por nombre o documento (parcial y sin distinguir mayúsculas), HU-08. */
    List<Estudiante> findByNombreContainingIgnoreCaseOrDocumentoContainingIgnoreCaseOrderByNombreAsc(
            String nombre, String documento);

    boolean existsByDocumento(String documento);

    boolean existsByDocumentoAndIdNot(String documento, Long id);
}
