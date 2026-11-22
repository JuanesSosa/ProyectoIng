import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Actividad, Entrega, EstudianteActividad, RubricaEstudiante } from '../core/api.models';

/** Calificación de una actividad: valor null = quitar la nota de ese criterio. */
export interface CalificacionRequest {
  criterioId: number;
  valor: number | null;
}

/** Criterio al guardar la rúbrica: sin id = nuevo. */
export interface CriterioRequest {
  id?: number;
  nombre: string;
  peso: number;
}

/** Peticiones HTTP de actividades, rúbricas y calificaciones. */
@Injectable({ providedIn: 'root' })
export class ActividadService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/actividades`;

  obtener(id: number): Observable<Actividad> {
    return this.http.get<Actividad>(`${this.url}/${id}`);
  }

  actualizar(id: number, actividad: { nombre: string; peso: number }): Observable<Actividad> {
    return this.http.put<Actividad>(`${this.url}/${id}`, actividad);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }

  /** Reemplaza la rúbrica (criterios con su %). */
  guardarRubrica(id: number, criterios: CriterioRequest[]): Observable<Actividad> {
    return this.http.put<Actividad>(`${this.url}/${id}/rubrica`, { criterios });
  }

  /** Estudiantes del grupo con su nota y avance en la actividad. */
  estudiantes(id: number): Observable<EstudianteActividad[]> {
    return this.http.get<EstudianteActividad[]>(`${this.url}/${id}/estudiantes`);
  }

  /** Rúbrica con las notas de un estudiante (el estudiante solo puede ver la suya). */
  rubrica(id: number, estudianteId?: number): Observable<RubricaEstudiante> {
    const query = estudianteId === undefined ? '' : `?estudianteId=${estudianteId}`;
    return this.http.get<RubricaEstudiante>(`${this.url}/${id}/rubrica${query}`);
  }

  calificar(id: number, estudianteId: number, calificaciones: CalificacionRequest[]): Observable<RubricaEstudiante> {
    return this.http.put<RubricaEstudiante>(`${this.url}/${id}/estudiantes/${estudianteId}/calificaciones`, {
      calificaciones,
    });
  }

  // ---------- entregas (archivos) ----------

  /** Entregas de un estudiante en la actividad (el estudiante ve solo las suyas). */
  entregas(id: number, estudianteId?: number): Observable<Entrega[]> {
    const query = estudianteId === undefined ? '' : `?estudianteId=${estudianteId}`;
    return this.http.get<Entrega[]>(`${this.url}/${id}/entregas${query}`);
  }

  /** Sube un archivo como entrega (solo estudiantes). */
  subirEntrega(id: number, archivo: File): Observable<Entrega> {
    const datos = new FormData();
    datos.append('archivo', archivo);
    return this.http.post<Entrega>(`${this.url}/${id}/entregas`, datos);
  }

  eliminarEntrega(entregaId: number): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/entregas/${entregaId}`);
  }

  /** Descarga el archivo (la petición lleva el token, por eso no es un simple enlace). */
  descargarEntrega(entregaId: number): Observable<Blob> {
    return this.http.get(`${environment.apiUrl}/entregas/${entregaId}/descargar`, { responseType: 'blob' });
  }
}
