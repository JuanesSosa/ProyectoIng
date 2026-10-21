import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ResumenGrupo } from '../../core/api.models';
import { mensajeError } from '../../core/errores';
import { NOTA_MINIMA_APROBATORIA } from '../../core/promedio';
import { GrupoService } from '../../services/grupo.service';

/** Seguimiento del estudiante en un grupo: nota de cada actividad, su rúbrica y el avance hacia la nota final. */
@Component({
  selector: 'app-mis-notas-grupo',
  standalone: true,
  imports: [RouterLink],
  template: `
    <p><a routerLink="/mis-notas">← Mis asignaturas</a></p>
    @if (error()) { <p class="error" role="alert">{{ error() }}</p> }
    @if (resumen(); as r) {
      <h1>{{ r.asignaturaNombre }} · {{ r.grupoNombre }} <span class="suave">({{ r.periodo }})</span></h1>

      <div class="fila">
        <div class="tarjeta dato"><span class="suave">Acumulado</span><strong>{{ r.acumulado }}</strong><span class="suave">puntos sobre 5.0</span></div>
        <div class="tarjeta dato"><span class="suave">Promedio de lo evaluado</span>
          <strong [class.reprobado]="r.promedio !== null && r.promedio < minimo">{{ r.promedio ?? '–' }}</strong></div>
        <div class="tarjeta dato"><span class="suave">Avance</span><strong>{{ r.avance }}%</strong><span class="suave">de la asignatura evaluado</span></div>
      </div>

      @for (a of r.actividades; track a.actividadId) {
        <div class="tarjeta">
          <div class="cabecera">
            <h2>{{ a.nombre }} <span class="suave">({{ a.peso }}%)</span></h2>
            <strong [class.reprobado]="a.nota !== null && a.nota < minimo">{{ a.nota ?? 'Sin calificar' }}</strong>
          </div>
          @if (a.criterios.length === 0) {
            <p class="suave">La rúbrica todavía no está definida.</p>
          } @else {
            <table>
              <thead><tr><th>Criterio</th><th class="num">Peso</th><th class="num">Nota</th></tr></thead>
              <tbody>
                @for (c of a.criterios; track c.criterioId) {
                  <tr>
                    <td>{{ c.nombre }}</td><td class="num">{{ c.peso }}%</td>
                    <td class="num" [class.reprobado]="c.valor !== null && c.valor < minimo">{{ c.valor ?? '–' }}</td>
                  </tr>
                }
              </tbody>
            </table>
          }
        </div>
      } @empty {
        <p class="suave">Este grupo todavía no tiene actividades.</p>
      }
      @if (r.actividades.length > 0 && r.pesoActividades !== 100) {
        <p class="suave">Las actividades definidas suman {{ r.pesoActividades }}% de la nota final.</p>
      }
    }
  `,
  styles: [
    `
      h2 { margin: 0; font-size: 1.05rem; }
      .cabecera { display: flex; justify-content: space-between; margin-bottom: 0.5rem; }
      .dato { flex: 1; min-width: 180px; display: flex; flex-direction: column; gap: 0.2rem; }
      .dato strong { font-size: 2rem; }
      a { color: var(--color-primario); }
    `,
  ],
})
export class MisNotasGrupoComponent implements OnInit {
  private readonly ruta = inject(ActivatedRoute);
  private readonly grupos = inject(GrupoService);
  protected readonly minimo = NOTA_MINIMA_APROBATORIA;

  readonly resumen = signal<ResumenGrupo | null>(null);
  readonly error = signal('');

  ngOnInit(): void {
    const id = Number(this.ruta.snapshot.paramMap.get('id'));
    this.grupos.resumen(id).subscribe({
      next: (r) => this.resumen.set(r),
      error: (err) => this.error.set(mensajeError(err, 'No se pudieron cargar tus notas.')),
    });
  }
}
