import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../core/auth.service';

/** Estructura de las pantallas con sesión: menú lateral (según el rol) y área de contenido. */
@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="shell">
      <aside class="sidebar">
        <div class="brand">Gestor de Notas</div>
        <nav>
          <a routerLink="/inicio" routerLinkActive="activo">Inicio</a>
          @if (auth.tieneRol('ADMIN', 'DOCENTE')) {
            <a routerLink="/estudiantes" routerLinkActive="activo">Estudiantes</a>
            <a routerLink="/asignaturas" routerLinkActive="activo">Asignaturas</a>
          }
          @if (auth.tieneRol('ADMIN')) {
            <a routerLink="/usuarios" routerLinkActive="activo">Usuarios</a>
          }
          @if (auth.tieneRol('DOCENTE')) {
            <a routerLink="/notas/registro" routerLinkActive="activo">Registrar notas</a>
          }
          <a routerLink="/notas/consulta" routerLinkActive="activo">Consultar notas</a>
        </nav>
        <div class="usuario">
          <strong>{{ auth.sesion()?.nombre }}</strong>
          <span class="rol">{{ auth.sesion()?.rol }}</span>
          <button type="button" class="btn btn-secundario" (click)="auth.logout()">Cerrar sesión</button>
        </div>
      </aside>
      <main class="contenido">
        <router-outlet />
      </main>
    </div>
  `,
  styles: [
    `
      .shell {
        display: grid;
        grid-template-columns: 240px 1fr;
        min-height: 100vh;
      }
      .sidebar {
        background: var(--color-sidebar);
        color: #fff;
        display: flex;
        flex-direction: column;
        padding: 1.25rem 1rem;
        gap: 1.5rem;
      }
      .brand {
        font-size: 1.15rem;
        font-weight: 700;
        letter-spacing: 0.01em;
      }
      nav {
        display: flex;
        flex-direction: column;
        gap: 0.25rem;
        flex: 1;
      }
      nav a {
        color: #cbd5e1;
        text-decoration: none;
        padding: 0.6rem 0.75rem;
        border-radius: 8px;
      }
      nav a:hover {
        background: rgba(255, 255, 255, 0.08);
        color: #fff;
      }
      nav a.activo {
        background: var(--color-primario);
        color: #fff;
      }
      .usuario {
        display: flex;
        flex-direction: column;
        gap: 0.5rem;
        font-size: 0.9rem;
      }
      .rol {
        font-size: 0.75rem;
        letter-spacing: 0.08em;
        color: #94a3b8;
      }
      .contenido {
        padding: 2rem;
        max-width: 1100px;
        width: 100%;
      }
      @media (max-width: 800px) {
        .shell {
          grid-template-columns: 1fr;
        }
        .sidebar {
          flex-direction: row;
          flex-wrap: wrap;
          align-items: center;
        }
        nav {
          flex-direction: row;
          flex-wrap: wrap;
        }
      }
    `,
  ],
})
export class ShellComponent {
  protected readonly auth = inject(AuthService);
}
