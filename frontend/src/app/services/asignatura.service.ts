import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Asignatura } from '../core/api.models';

/** Peticiones HTTP de asignaturas (GET: admin y docente; crear/editar/desactivar: admin). */
@Injectable({ providedIn: 'root' })
export class AsignaturaService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/asignaturas`;

  listar(soloActivas = false): Observable<Asignatura[]> {
    const params = new HttpParams().set('soloActivas', soloActivas);
    return this.http.get<Asignatura[]>(this.url, { params });
  }

  crear(asignatura: Asignatura): Observable<Asignatura> {
    return this.http.post<Asignatura>(this.url, asignatura);
  }

  actualizar(id: number, asignatura: Asignatura): Observable<Asignatura> {
    return this.http.put<Asignatura>(`${this.url}/${id}`, asignatura);
  }

  /** El backend la desactiva (no la borra). */
  desactivar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
