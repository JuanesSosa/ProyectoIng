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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Calificación de un estudiante en una evaluación de una asignatura durante un periodo (tabla "notas").
 * Ejemplo: Juan Pérez, Matemáticas, 2026-2, evaluación "Parcial", peso 30 (%), valor 4.20.
 * No puede haber dos evaluaciones con el mismo nombre para el mismo estudiante, asignatura y periodo.
 */
@Entity
@Table(
        name = "notas",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_nota_evaluacion",
                columnNames = {"estudiante_id", "asignatura_id", "periodo", "evaluacion"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Nota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asignatura_id", nullable = false)
    private Asignatura asignatura;

    /** Periodo académico con formato AAAA-1 o AAAA-2, por ejemplo "2026-2". */
    @Column(nullable = false, length = 10)
    private String periodo;

    /** Nombre de la evaluación: "Parcial", "Taller", "Final"... */
    @Column(nullable = false, length = 100)
    private String evaluacion;

    /** Peso de la evaluación en porcentaje (1 a 100). Opcional: si es null se usa promedio simple. */
    @Column
    private Integer peso;

    /** Nota entre 0.0 y 5.0. */
    @Column(nullable = false, precision = 3, scale = 2)
    private BigDecimal valor;
}
