package com.fullstack.fullstack.mapper;

import com.fullstack.fullstack.dto.ProductDto;
import com.fullstack.fullstack.model.entity.Product;

/**
 * Mapper estático para convertir entre la entidad Product y el DTO ProductDto.
 * - toDto: entidad → DTO (para enviar en respuestas HTTP).
 * - toEntity: DTO → entidad (para guardar en base de datos).
 * - updateEntity: actualiza una entidad existente con los datos del DTO (para PUT).
 */
public final class ProductMapper {

    private ProductMapper() {
        // Clase de utilidad: no se debe instanciar
    }

    /**
     * Convierte una entidad Product a ProductDto.
     * Si entity es null, devuelve null (evita NullPointerException).
     */
    public static ProductDto toDto(Product entity) {
        if (entity == null) {
            return null;
        }
        return ProductDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .stock(entity.getStock())
                .build();
    }

    /**
     * Convierte un ProductDto a entidad Product.
     * Útil al crear o cuando recibimos datos del cliente.
     */
    public static Product toEntity(ProductDto dto) {
        if (dto == null) {
            return null;
        }
        return Product.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .stock(dto.getStock())
                .build();
    }

    /**
     * Actualiza los campos de una entidad existente con los valores del DTO.
     * No modifica el id (se usa en actualizaciones PUT).
     */
    public static void updateEntity(Product entity, ProductDto dto) {
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setPrice(dto.getPrice());
        entity.setStock(dto.getStock());
    }
}
