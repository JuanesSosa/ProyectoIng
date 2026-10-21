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

/**
 * Grupo de una asignatura en un periodo, por ejemplo "Ingeniería de Software - Grupo 1 - 2026-2" (tabla "grupos").
 * Tiene un docente a cargo y estudiantes matriculados. Las actividades y notas se llevan por grupo.
 */
@Entity
@Table(name = "grupos",
        uniqueConstraints = @UniqueConstraint(name = "uk_grupo", columnNames = {"asignatura_id", "periodo", "nombre"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Grupo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asignatura_id", nullable = false)
    private Asignatura asignatura;

    /** Nombre del grupo, por ejemplo "Grupo 1". */
    @Column(nullable = false, length = 50)
    private String nombre;

    /** Periodo académico con formato AAAA-1 o AAAA-2. */
    @Column(nullable = false, length = 10)
    private String periodo;

    /** Docente a cargo: es el único que puede definir actividades y calificar en este grupo. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "docente_id", nullable = false)
    private Usuario docente;
}
