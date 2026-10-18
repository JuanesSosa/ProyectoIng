import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { Panel } from '../../core/api.models';
import { NotaService } from '../../services/nota.service';

/** Panel principal (RF-02, mock-up 5.2): totales para admin/docente y accesos rápidos según rol. */
@Component({
  selector: 'app-inicio',
  standalone: true,
  imports: [RouterLink],
  template: `
    <h1>Hola, {{ auth.sesion()?.nombre }}</h1>
    @if (auth.tieneRol('ADMIN', 'DOCENTE')) {
      <div class="fila">
        <div class="tarjeta dato"><span class="suave">Estudiantes</span><strong>{{ panel()?.estudiantes ?? '–' }}</strong></div>
        <div class="tarjeta dato"><span class="suave">Asignaturas</span><strong>{{ panel()?.asignaturas ?? '–' }}</strong></div>
        <div class="tarjeta dato"><span class="suave">Notas registradas</span><strong>{{ panel()?.notas ?? '–' }}</strong></div>
      </div>
    }
    <div class="tarjeta">
      <h2>Accesos rápidos</h2>
      <div class="fila">
        @if (auth.tieneRol('DOCENTE')) {
          <a class="btn" routerLink="/notas/registro">Registrar notas</a>
        }
        @if (auth.tieneRol('ADMIN', 'DOCENTE')) {
          <a class="btn btn-secundario" routerLink="/estudiantes">Estudiantes</a>
        }
        @if (auth.tieneRol('ADMIN')) {
          <a class="btn btn-secundario" routerLink="/usuarios">Usuarios</a>
        }
        <a class="btn btn-secundario" routerLink="/notas/consulta">Consultar notas</a>
      </div>
    </div>
  `,
  styles: [
    `
      .dato { flex: 1; min-width: 160px; display: flex; flex-direction: column; gap: 0.25rem; }
      .dato strong { font-size: 2rem; }
      a.btn { text-decoration: none; }
      h2 { margin-top: 0; font-size: 1.1rem; }
    `,
  ],
})
export class InicioComponent implements OnInit {
  protected readonly auth = inject(AuthService);
  private readonly notas = inject(NotaService);
  readonly panel = signal<Panel | null>(null);

  ngOnInit(): void {
    if (this.auth.tieneRol('ADMIN', 'DOCENTE')) {
      this.notas.panel().subscribe({ next: (p) => this.panel.set(p), error: () => this.panel.set(null) });
    }
  }
}
