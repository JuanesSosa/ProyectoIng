package com.fullstack.fullstack.service;

import com.fullstack.fullstack.dto.ProductDto;

import java.util.List;

/**
 * Interfaz del servicio de productos.
 * Define las operaciones CRUD que expone la aplicación (listar, buscar por id, crear, actualizar, eliminar).
 * La implementación está en ProductServiceImpl.
 */
public interface ProductService {

    /** Obtiene todos los productos. */
    List<ProductDto> findAll();

    /** Obtiene un producto por su id. Lanza ResourceNotFoundException si no existe. */
    ProductDto findById(Long id);

    /** Crea un nuevo producto. El id en el DTO se ignora (se genera en base de datos). */
    ProductDto create(ProductDto productDto);

    /** Actualiza un producto existente por id. */
    ProductDto update(Long id, ProductDto productDto);

    /** Elimina un producto por id. Lanza ResourceNotFoundException si no existe. */
    void delete(Long id);
}
