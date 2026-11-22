package com.fullstack.fullstack.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Estudiante registrado en el sistema (tabla "estudiantes").
 * El documento es único: así se cumple la regla "la información de un estudiante no deberá duplicarse".
 */
@Entity
@Table(name = "estudiantes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Estudiante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Documento de identidad (único). */
    @Column(nullable = false, unique = true, length = 30)
    private String documento;

    /** Código estudiantil (único). Puede ser null en estudiantes creados antes de existir este campo. */
    @Column(unique = true, length = 20)
    private String codigo;

    @Column(nullable = false, length = 120)
    private String nombre;

    /** Correo del estudiante, al que se envían sus notas. Puede ser null en estudiantes antiguos. */
    @Column(length = 150)
    private String correo;

    /** Curso o grupo, por ejemplo "10A". */
    @Column(nullable = false, length = 30)
    private String curso;
}
