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

import java.time.Instant;

/**
 * Archivo que un estudiante sube como entrega de una actividad (tabla "entregas").
 * El contenido se guarda en disco con un nombre generado ({@link #archivo}); aquí solo queda el registro.
 */
@Entity
@Table(name = "entregas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Entrega {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "actividad_id", nullable = false)
    private Actividad actividad;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;

    /** Nombre con el que el estudiante subió el archivo (ya limpiado). */
    @Column(nullable = false, length = 200)
    private String nombre;

    /** Tamaño en bytes. */
    @Column(nullable = false)
    private long tamano;

    /** Nombre del archivo en el disco (generado por el sistema, nunca el que envía el usuario). */
    @Column(nullable = false, length = 100)
    private String archivo;

    @Column(nullable = false)
    private Instant fecha;
}
