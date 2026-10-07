package com.fullstack.fullstack.service.impl;

import com.fullstack.fullstack.dto.ProductDto;
import com.fullstack.fullstack.exception.ResourceNotFoundException;
import com.fullstack.fullstack.mapper.ProductMapper;
import com.fullstack.fullstack.model.entity.Product;
import com.fullstack.fullstack.repository.ProductRepository;
import com.fullstack.fullstack.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementación del servicio de productos.
 * Usa el repositorio para acceder a la base de datos y el mapper para convertir entidad ↔ DTO.
 */
@Service  // Marca esta clase como servicio; Spring la registra como bean y puede inyectarla (ej. en el Controller).
@Transactional  // Todas las operaciones de esta clase se ejecutan dentro de una transacción (rollback si hay excepción).
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Lista todos los productos: obtiene entidades, las convierte a DTO y devuelve la lista.
     */
    @Override  // Indica que este método implementa el de la interfaz ProductService.
    @Transactional(readOnly = true)  // Transacción de solo lectura (optimización; no hace flush innecesario).
    public List<ProductDto> findAll() {
        return productRepository.findAll()
                .stream()
                .map(ProductMapper::toDto)
                .toList();
    }

    /**
     * Busca un producto por id. Si no existe, lanza ResourceNotFoundException (se traduce a 404).
     */
    @Override
    @Transactional(readOnly = true)
    public ProductDto findById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + id));
        return ProductMapper.toDto(product);
    }

    /**
     * Crea un nuevo producto: DTO → entidad, id a null para que la BD lo genere, guardar y devolver DTO.
     */
    @Override  // Indica que implementa el método create de la interfaz ProductService.
    public ProductDto create(ProductDto productDto) {
        Product product = ProductMapper.toEntity(productDto);
        product.setId(null); // Asegura que sea insert y no update
        Product saved = productRepository.save(product);
        return ProductMapper.toDto(saved);
    }

    /**
     * Actualiza un producto existente: buscar por id, actualizar campos con el DTO, guardar y devolver DTO.
     */
    @Override
    public ProductDto update(Long id, ProductDto productDto) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + id));
        ProductMapper.updateEntity(existing, productDto);
        Product saved = productRepository.save(existing);
        return ProductMapper.toDto(saved);
    }

    /**
     * Elimina un producto por id. Verifica que exista antes de borrar (para devolver 404 si no existe).
     */
    @Override  // Indica que implementa el método delete de la interfaz ProductService.
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id " + id);
        }
        productRepository.deleteById(id);
    }
}
