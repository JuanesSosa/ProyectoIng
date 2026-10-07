package com.fullstack.fullstack.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO (Data Transfer Object) de Producto.
 * Se usa para enviar y recibir datos en la API REST sin exponer la entidad JPA directamente.
 * Tiene los mismos campos que la entidad Product pero es un objeto plano (sin anotaciones JPA).
 */
@Data  // Lombok: genera getters, setters, toString(), equals() y hashCode().
@NoArgsConstructor  // Lombok: constructor sin argumentos (útil para deserialización JSON).
@AllArgsConstructor  // Lombok: constructor con todos los campos.
@Builder  // Lombok: construcción fluida con ProductDto.builder().name("x").build().
public class ProductDto {

    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 255, message = "El nombre no puede superar 255 caracteres")
    private String name;

    @Size(max = 255, message = "La descripción no puede superar 255 caracteres")
    private String description;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor que 0")
    private BigDecimal price;

    @NotNull(message = "El stock es obligatorio")
    @PositiveOrZero(message = "El stock no puede ser negativo")
    private Integer stock;
}
