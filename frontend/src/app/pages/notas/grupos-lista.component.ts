import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { Grupo } from '../../core/api.models';
import { mensajeError } from '../../core/errores';
import { GrupoService } from '../../services/grupo.service';

/** Pantalla "asignaturas con sus grupos" (boceto 1). Cada grupo lleva a sus actividades (o al seguimiento, si es estudiante). */
@Component({
  selector: 'app-grupos-lista',
  standalone: true,
  imports: [RouterLink],
  template: `
    <h1>{{ esEstudiante ? 'Mis notas' : 'Asignaturas y grupos' }}</h1>
    @if (error()) { <p class="error" role="alert">{{ error() }}</p> }
    @for (a of asignaturas(); track a.id) {
      <div class="tarjeta">
        <h2>{{ a.nombre }}</h2>
        <div class="fila">
          @for (g of a.grupos; track g.id) {
            <a class="grupo" [routerLink]="[base, 'grupo', g.id]">
              <strong>{{ g.nombre }}</strong>
              <span class="suave">{{ g.periodo }}</span>
              @if (!esEstudiante) { <span class="suave">{{ g.estudiantes }} estudiantes</span> }
            </a>
          }
        </div>
      </div>
    } @empty {
      @if (!error() && cargado()) {
        <p class="suave">
          {{ esEstudiante ? 'Todavía no estás matriculado en ningún grupo.' : 'No tienes grupos asignados.' }}
        </p>
      }
    }
  `,
  styles: [
    `
      h2 { margin: 0 0 0.75rem; font-size: 1.1rem; }
      .fila { align-items: stretch; }
      .grupo {
        display: flex; flex-direction: column; gap: 0.15rem; min-width: 150px;
        padding: 0.75rem 1rem; border: 1px solid var(--color-borde); border-radius: 10px;
        text-decoration: none; color: inherit; background: #f8fafc;
      }
      .grupo:hover { border-color: var(--color-primario); background: #eff6ff; }
    `,
  ],
})
export class GruposListaComponent implements OnInit {
  private readonly servicio = inject(GrupoService);
  protected readonly esEstudiante = inject(AuthService).tieneRol('ESTUDIANTE');
  protected readonly base = this.esEstudiante ? '/mis-notas' : '/notas';

  private readonly grupos = signal<Grupo[]>([]);
  readonly cargado = signal(false);
  readonly error = signal('');

  /** Los grupos agrupados por asignatura, en el orden en que llegan del servidor. */
  readonly asignaturas = computed(() => {
    const mapa = new Map<number, { id: number; nombre: string; grupos: Grupo[] }>();
    for (const g of this.grupos()) {
      const entrada = mapa.get(g.asignaturaId) ?? { id: g.asignaturaId, nombre: g.asignaturaNombre ?? '', grupos: [] };
      entrada.grupos.push(g);
      mapa.set(g.asignaturaId, entrada);
    }
    return [...mapa.values()];
  });

  ngOnInit(): void {
    this.servicio.listar().subscribe({
      next: (lista) => { this.grupos.set(lista); this.cargado.set(true); },
      error: (err) => { this.error.set(mensajeError(err, 'No se pudieron cargar los grupos.')); this.cargado.set(true); },
    });
  }
}
