package com.fullstack.fullstack.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Criterio de la rúbrica de una actividad, por ejemplo "Teoría (50 %)" (tabla "criterios").
 * Los pesos de los criterios de una actividad suman como máximo 100.
 */
@Entity
@Table(name = "criterios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Criterio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "actividad_id", nullable = false)
    private Actividad actividad;

    @Column(nullable = false, length = 100)
    private String nombre;

    /** Porcentaje de la nota de la actividad (1 a 100). */
    @Column(nullable = false)
    private Integer peso;
}
