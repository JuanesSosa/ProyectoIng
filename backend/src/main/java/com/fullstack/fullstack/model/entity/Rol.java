package com.fullstack.fullstack.model.entity;

/**
 * Roles del sistema (ver "1.6 Usuarios del sistema" del documento de requisitos).
 * - ADMIN: gestiona usuarios, estudiantes y asignaturas y consulta toda la información.
 * - DOCENTE: consulta estudiantes y registra/modifica calificaciones.
 * - ESTUDIANTE: consulta únicamente sus propias calificaciones y promedios.
 */
public enum Rol {
    ADMIN,
    DOCENTE,
    ESTUDIANTE
}
