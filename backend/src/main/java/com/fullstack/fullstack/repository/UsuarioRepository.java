package com.fullstack.fullstack.repository;

import com.fullstack.fullstack.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Acceso a datos de usuarios. Spring Data genera las consultas a partir del nombre de cada método. */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    boolean existsByCorreoIgnoreCase(String correo);

    boolean existsByCorreoIgnoreCaseAndIdNot(String correo, Long id);

    /** true si algún usuario está vinculado a ese estudiante. */
    boolean existsByEstudianteId(Long estudianteId);

    List<Usuario> findAllByOrderByNombreAsc();
}
