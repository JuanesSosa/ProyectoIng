import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/auth.service';
import { Estudiante, ResumenNotas } from '../../core/api.models';
import { mensajeError } from '../../core/errores';
import { NOTA_MINIMA_APROBATORIA } from '../../core/promedio';
import { EstudianteService } from '../../services/estudiante.service';
import { NotaService } from '../../services/nota.service';

/**
 * Consulta de notas y promedios (RF-09, RF-10, RF-11, mock-up 5.5).
 * El estudiante ve solo las suyas; administrador y docente eligen un estudiante.
 */
@Component({
  selector: 'app-consulta-notas',
  standalone: true,
  imports: [FormsModule],
  template: `
    <h1>Consultar notas</h1>
    <div class="tarjeta">
      <div class="fila">
        @if (!esEstudiante) {
          <div class="campo"><label for="est">Estudiante</label>
            <select id="est" name="estudiante" [ngModel]="estudianteId()" (ngModelChange)="estudianteId.set($event); consultar()">
              <option [ngValue]="null">— Selecciona —</option>
              @for (e of estudiantes(); track e.id) { <option [ngValue]="e.id">{{ e.nombre }}</option> }
            </select></div>
        }
        <div class="campo"><label for="per">Periodo</label>
          <select id="per" name="periodo" [ngModel]="periodo()" (ngModelChange)="periodo.set($event); consultar()">
            <option value="">— Selecciona —</option>
            @for (p of periodos(); track p) { <option [value]="p">{{ p }}</option> }
          </select></div>
      </div>
    </div>

    @if (error()) { <p class="error" role="alert">{{ error() }}</p> }

    @if (resumen(); as r) {
      <div class="tarjeta">
        <h2>{{ r.estudianteNombre }} · {{ r.periodo }}</h2>
        @if (r.asignaturas.length === 0) {
          <p class="suave">No hay notas registradas en este periodo.</p>
        } @else {
          <div class="scroll">
            <table>
              <thead>
                <tr>
                  <th>Asignatura</th>
                  @for (n of columnas(); track n) { <th class="num">Nota {{ n }}</th> }
                  <th class="num">Promedio</th>
                </tr>
              </thead>
              <tbody>
                @for (a of r.asignaturas; track a.asignaturaId) {
                  <tr>
                    <td>{{ a.nombre }}</td>
                    @for (n of columnas(); track n) {
                      <td class="num">
                        @if (a.notas[n - 1]; as nota) {
                          {{ nota.valor.toFixed(1) }}@if (nota.peso !== null) { <span class="suave"> ({{ nota.peso }}%)</span> }
                        } @else { – }
                      </td>
                    }
                    <td class="num" [class.reprobado]="a.promedio !== null && a.promedio < minimo">{{ a.promedio ?? '–' }}</td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
          <p style="margin-bottom: 0">Promedio general:
            <strong [class.reprobado]="r.promedioGeneral !== null && r.promedioGeneral < minimo">{{ r.promedioGeneral ?? '–' }}</strong></p>
        }
      </div>
    }
  `,
  styles: ['h2 { margin-top: 0; font-size: 1.1rem; }'],
})
export class ConsultaNotasComponent implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly notaService = inject(NotaService);
  private readonly estudianteService = inject(EstudianteService);

  protected readonly esEstudiante = this.auth.tieneRol('ESTUDIANTE');
  protected readonly minimo = NOTA_MINIMA_APROBATORIA;

  readonly estudiantes = signal<Estudiante[]>([]);
  readonly periodos = signal<string[]>([]);
  readonly estudianteId = signal<number | null>(null);
  readonly periodo = signal('');
  readonly resumen = signal<ResumenNotas | null>(null);
  readonly error = signal('');

  /** Número de columnas "Nota N": la mayor cantidad de evaluaciones entre las asignaturas. */
  readonly columnas = computed(() => {
    const max = Math.max(0, ...(this.resumen()?.asignaturas.map((a) => a.notas.length) ?? []));
    return Array.from({ length: max }, (_, i) => i + 1);
  });

  ngOnInit(): void {
    this.notaService.periodos().subscribe({
      next: (p) => {
        this.periodos.set(p);
        if (p.length > 0) {
          this.periodo.set(p[0]);
          this.consultar();
        }
      },
      error: (err) => this.error.set(mensajeError(err, 'No se pudieron cargar los periodos.')),
    });
    if (!this.esEstudiante) {
      this.estudianteService.listar().subscribe({
        next: (l) => this.estudiantes.set(l),
        error: (err) => this.error.set(mensajeError(err, 'No se pudieron cargar los estudiantes.')),
      });
    }
  }

  consultar(): void {
    this.error.set('');
    this.resumen.set(null);
    const periodo = this.periodo();
    if (!periodo || (!this.esEstudiante && this.estudianteId() === null)) {
      return;
    }
    this.notaService.resumen(periodo, this.esEstudiante ? undefined : this.estudianteId()!).subscribe({
      next: (r) => this.resumen.set(r),
      error: (err) => this.error.set(mensajeError(err, 'No se pudieron consultar las notas.')),
    });
  }
}
