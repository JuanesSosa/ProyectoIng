import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Actividad, Grupo, Matriculado, Panel, ResumenGrupo } from '../core/api.models';

/** Peticiones HTTP de grupos, matrículas, actividades del grupo y seguimiento. */
@Injectable({ providedIn: 'root' })
export class GrupoService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/grupos`;

  /** Grupos que la persona puede ver: admin todos, docente los suyos, estudiante donde está matriculado. */
  listar(): Observable<Grupo[]> {
    return this.http.get<Grupo[]>(this.url);
  }

  obtener(id: number): Observable<Grupo> {
    return this.http.get<Grupo>(`${this.url}/${id}`);
  }

  crear(grupo: Grupo): Observable<Grupo> {
    return this.http.post<Grupo>(this.url, grupo);
  }

  actualizar(id: number, grupo: Grupo): Observable<Grupo> {
    return this.http.put<Grupo>(`${this.url}/${id}`, grupo);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }

  // ---------- matrículas (admin) ----------

  estudiantes(id: number): Observable<Matriculado[]> {
    return this.http.get<Matriculado[]>(`${this.url}/${id}/estudiantes`);
  }

  matricular(id: number, estudianteId: number): Observable<Matriculado> {
    return this.http.post<Matriculado>(`${this.url}/${id}/estudiantes/${estudianteId}`, {});
  }

  desmatricular(id: number, estudianteId: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}/estudiantes/${estudianteId}`);
  }

  // ---------- actividades y seguimiento ----------

  actividades(id: number): Observable<Actividad[]> {
    return this.http.get<Actividad[]>(`${this.url}/${id}/actividades`);
  }

  crearActividad(id: number, actividad: { nombre: string; peso: number }): Observable<Actividad> {
    return this.http.post<Actividad>(`${this.url}/${id}/actividades`, actividad);
  }

  /** Seguimiento de un estudiante; si no se indica estudianteId, el backend usa el del token (estudiante). */
  resumen(id: number, estudianteId?: number): Observable<ResumenGrupo> {
    const query = estudianteId === undefined ? '' : `?estudianteId=${estudianteId}`;
    return this.http.get<ResumenGrupo>(`${this.url}/${id}/resumen${query}`);
  }

  /** Seguimiento de todos los estudiantes del grupo (admin o docente). */
  progreso(id: number): Observable<ResumenGrupo[]> {
    return this.http.get<ResumenGrupo[]>(`${this.url}/${id}/progreso`);
  }

  panel(): Observable<Panel> {
    return this.http.get<Panel>(`${environment.apiUrl}/panel`);
  }
}
