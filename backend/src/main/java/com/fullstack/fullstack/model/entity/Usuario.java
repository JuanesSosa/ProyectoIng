package com.fullstack.fullstack.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * Usuario que inicia sesión en el sistema (tabla "usuarios").
 * La contraseña se guarda siempre cifrada con BCrypt, nunca en texto plano.
 * Los usuarios con rol ESTUDIANTE quedan vinculados a un registro de {@link Estudiante}.
 */
@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    /** Correo electrónico (único). Es el usuario con el que se inicia sesión. */
    @Column(nullable = false, unique = true, length = 150)
    private String correo;

    /** Hash BCrypt de la contraseña. */
    @Column(nullable = false, length = 100)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    /** false = usuario desactivado: no puede iniciar sesión (RF-02). */
    @Column(nullable = false)
    @Builder.Default
    private boolean activo = true;

    /** Solo para el rol ESTUDIANTE: el estudiante cuyas notas puede consultar. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estudiante_id")
    private Estudiante estudiante;
}
