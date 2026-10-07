package com.fullstack.fullstack.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Entidad JPA que representa la tabla "products" en la base de datos.
 * Cada instancia de Product equivale a una fila en la tabla.
 */
@Entity  // Indica que esta clase es una entidad JPA y se mapea a una tabla en la BD.
@Table(name = "products")  // Nombre de la tabla en MySQL (si no se pone, JPA usa el nombre de la clase).
@Data  // Lombok: genera getters, setters, toString(), equals() y hashCode() automáticamente.
@NoArgsConstructor  // Lombok: genera un constructor sin argumentos (lo exige JPA).
@AllArgsConstructor  // Lombok: genera un constructor con todos los campos.
@Builder  // Lombok: permite crear instancias con Product.builder().name("x").price(10).build().
public class Product {

    /** Clave primaria. Se genera automáticamente al insertar (auto_increment en MySQL). */
    @Id  // Marca este campo como la clave primaria de la tabla.
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // El valor lo genera la BD (auto_increment).
    private Long id;

    /** Nombre del producto. */
    @Column(nullable = false)
    private String name;

    /** Descripción del producto. */
    private String description;

    /** Precio del producto (BigDecimal para evitar errores de redondeo con dinero). */
    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    /** Cantidad en stock. */
    private Integer stock;
}
