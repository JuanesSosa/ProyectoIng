import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  Nota,
  Panel,
  RegistroNotasRequest,
  RegistroNotasResponse,
  ResumenNotas,
} from '../core/api.models';

/** Peticiones HTTP de notas, promedios y panel. */
@Injectable({ providedIn: 'root' })
export class NotaService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/notas`;

  /** Evaluaciones de un estudiante en una asignatura y periodo. */
  listar(estudianteId: number, asignaturaId: number, periodo: string): Observable<Nota[]> {
    const params = new HttpParams()
      .set('estudianteId', estudianteId)
      .set('asignaturaId', asignaturaId)
      .set('periodo', periodo);
    return this.http.get<Nota[]>(this.url, { params });
  }

  /** Guarda todas las evaluaciones de un estudiante en una asignatura y periodo (solo docente). */
  registrar(registro: RegistroNotasRequest): Observable<RegistroNotasResponse> {
    return this.http.put<RegistroNotasResponse>(`${this.url}/registro`, registro);
  }

  /**
   * Consulta de notas con promedios. Para el rol ESTUDIANTE no se envía estudianteId:
   * el backend usa el del token.
   */
  resumen(periodo: string, estudianteId?: number): Observable<ResumenNotas> {
    let params = new HttpParams().set('periodo', periodo);
    if (estudianteId !== undefined) {
      params = params.set('estudianteId', estudianteId);
    }
    return this.http.get<ResumenNotas>(`${this.url}/resumen`, { params });
  }

  periodos(): Observable<string[]> {
    return this.http.get<string[]>(`${this.url}/periodos`);
  }

  panel(): Observable<Panel> {
    return this.http.get<Panel>(`${environment.apiUrl}/panel`);
  }
}
