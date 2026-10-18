import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { LoginResponse, Rol } from './api.models';

/** Sesión del usuario: el token JWT y los datos para armar la interfaz. */
export interface Sesion {
  token: string;
  nombre: string;
  correo: string;
  rol: Rol;
  estudianteId: number | null;
  /** Momento en que vence el token (milisegundos desde 1970). */
  expiraEn: number;
}

const CLAVE = 'gestor-notas.sesion';

/**
 * Autenticación (RF-01, RF-12). El token se guarda en sessionStorage: se pierde al cerrar la pestaña.
 * Cerrar sesión es descartar el token; el backend no guarda sesiones.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly _sesion = signal<Sesion | null>(this.leer());

  /** Sesión actual (señal de solo lectura): se usa en las plantillas como auth.sesion(). */
  readonly sesion = this._sesion.asReadonly();

  login(correo: string, password: string): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${environment.apiUrl}/auth/login`, { correo, password })
      .pipe(
        tap((r) =>
          this.guardar({
            token: r.token,
            nombre: r.nombre,
            correo: r.correo,
            rol: r.rol,
            estudianteId: r.estudianteId,
            expiraEn: Date.now() + r.expiraEnSegundos * 1000,
          })
        )
      );
  }

  /** Descarta la sesión y vuelve al login. */
  logout(): void {
    this._sesion.set(null);
    try {
      sessionStorage.removeItem(CLAVE);
    } catch {
      // sin almacenamiento disponible: no hay nada que borrar
    }
    void this.router.navigate(['/login']);
  }

  /** true si hay sesión y el token no ha vencido. */
  estaAutenticado(): boolean {
    const s = this._sesion();
    return s !== null && s.expiraEn > Date.now();
  }

  token(): string | null {
    return this.estaAutenticado() ? this._sesion()!.token : null;
  }

  rol(): Rol | null {
    return this._sesion()?.rol ?? null;
  }

  tieneRol(...roles: Rol[]): boolean {
    const rol = this.rol();
    return rol !== null && roles.includes(rol);
  }

  private guardar(sesion: Sesion): void {
    this._sesion.set(sesion);
    try {
      sessionStorage.setItem(CLAVE, JSON.stringify(sesion));
    } catch {
      // sin almacenamiento: la sesión vive solo en memoria
    }
  }

  private leer(): Sesion | null {
    try {
      const texto = sessionStorage.getItem(CLAVE);
      if (!texto) {
        return null;
      }
      const sesion = JSON.parse(texto) as Sesion;
      return sesion.expiraEn > Date.now() ? sesion : null;
    } catch {
      return null;
    }
  }
}
