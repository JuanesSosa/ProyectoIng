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

/** Una evaluación del registro: "Parcial", 30 (%), 4.2. */
export interface Evaluacion {
  evaluacion: string;
  peso: number | null;
  valor: number;
}

export interface Nota extends Evaluacion {
  id: number;
  estudianteId: number;
  asignaturaId: number;
  periodo: string;
}

export interface RegistroNotasRequest {
  estudianteId: number;
  asignaturaId: number;
  periodo: string;
  evaluaciones: Evaluacion[];
}

export interface RegistroNotasResponse {
  estudianteId: number;
  asignaturaId: number;
  periodo: string;
  notas: Nota[];
  promedio: number | null;
  pesoTotal: number;
}

export interface AsignaturaResumen {
  asignaturaId: number;
  codigo: string;
  nombre: string;
  notas: Nota[];
  promedio: number | null;
  pesoTotal: number;
}

export interface ResumenNotas {
  estudianteId: number;
  estudianteNombre: string;
  periodo: string;
  asignaturas: AsignaturaResumen[];
  promedioGeneral: number | null;
}

export interface Panel {
  estudiantes: number;
  asignaturas: number;
  notas: number;
}
