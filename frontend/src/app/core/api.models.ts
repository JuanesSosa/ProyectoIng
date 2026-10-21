/** Modelos que coinciden con los DTO de la API (backend Spring Boot). */

export type Rol = 'ADMIN' | 'DOCENTE' | 'ESTUDIANTE';

export interface LoginResponse {
  token: string;
  tipo: string;
  expiraEnSegundos: number;
  nombre: string;
  correo: string;
  rol: Rol;
  estudianteId: number | null;
}

export interface Usuario {
  id: number;
  nombre: string;
  correo: string;
  rol: Rol;
  activo: boolean;
  estudianteId: number | null;
  estudianteNombre: string | null;
}

/** password es obligatoria al crear; al editar, si va vacía se conserva la actual. */
export interface UsuarioRequest {
  nombre: string;
  correo: string;
  password?: string;
  rol: Rol;
  estudianteId?: number | null;
  activo?: boolean;
}

export interface Estudiante {
  id?: number;
  documento: string;
  nombre: string;
  curso: string;
}

export interface Asignatura {
  id?: number;
  codigo: string;
  nombre: string;
  activa?: boolean;
}

export interface Grupo {
  id?: number;
  asignaturaId: number;
  asignaturaCodigo?: string;
  asignaturaNombre?: string;
  nombre: string;
  periodo: string;
  docenteId: number;
  docenteNombre?: string;
  estudiantes?: number;
}

export interface Matriculado {
  estudianteId: number;
  documento: string;
  nombre: string;
  curso: string;
}

export interface Criterio {
  id: number;
  nombre: string;
  peso: number;
}

export interface Actividad {
  id: number;
  grupoId: number;
  nombre: string;
  peso: number;
  criterios: Criterio[];
  pesoCriterios: number;
}

/** Un criterio con la nota del estudiante (valor null = sin calificar). */
export interface CriterioNota {
  criterioId: number;
  nombre: string;
  peso: number;
  valor: number | null;
}

export interface RubricaEstudiante {
  actividadId: number;
  actividadNombre: string;
  actividadPeso: number;
  estudianteId: number;
  estudianteNombre: string;
  criterios: CriterioNota[];
  nota: number | null;
  avance: number;
}

export interface EstudianteActividad {
  estudianteId: number;
  documento: string;
  nombre: string;
  nota: number | null;
  avance: number;
}

export interface ActividadResumen {
  actividadId: number;
  nombre: string;
  peso: number;
  criterios: CriterioNota[];
  nota: number | null;
  avance: number;
}

/** Seguimiento de un estudiante en un grupo (promedio de lo evaluado, acumulado sobre 5.0 y avance %). */
export interface ResumenGrupo {
  grupoId: number;
  asignaturaId: number;
  asignaturaNombre: string;
  grupoNombre: string;
  periodo: string;
  estudianteId: number;
  estudianteDocumento: string;
  estudianteNombre: string;
  actividades: ActividadResumen[];
  pesoActividades: number;
  promedio: number | null;
  acumulado: number;
  avance: number;
}

export interface Panel {
  estudiantes: number;
  asignaturas: number;
  grupos: number;
  calificaciones: number;
}
