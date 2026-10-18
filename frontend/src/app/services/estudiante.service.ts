import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Estudiante } from '../core/api.models';

/** Peticiones HTTP de estudiantes (GET: admin y docente; crear/editar/eliminar: admin). */
@Injectable({ providedIn: 'root' })
export class EstudianteService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/estudiantes`;

  /** Lista todos o, si hay texto, busca por nombre o documento. */
  listar(texto = ''): Observable<Estudiante[]> {
    let params = new HttpParams();
    if (texto.trim()) {
      params = params.set('q', texto.trim());
    }
    return this.http.get<Estudiante[]>(this.url, { params });
  }

  crear(estudiante: Estudiante): Observable<Estudiante> {
    return this.http.post<Estudiante>(this.url, estudiante);
  }

  actualizar(id: number, estudiante: Estudiante): Observable<Estudiante> {
    return this.http.put<Estudiante>(`${this.url}/${id}`, estudiante);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
