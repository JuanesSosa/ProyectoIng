package com.fullstack.fullstack.repository;

import com.fullstack.fullstack.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de acceso a datos para la entidad Product.
 * Extiende JpaRepository: Spring Data JPA implementa automáticamente métodos como
 * findAll(), findById(id), save(entity), deleteById(id), existsById(id).
 * No es necesario escribir consultas SQL para operaciones CRUD básicas.
 */
@Repository  // Marca esta interfaz como componente de acceso a datos; Spring la detecta y crea un bean.
public interface ProductRepository extends JpaRepository<Product, Long> {
}
