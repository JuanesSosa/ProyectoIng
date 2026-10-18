import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Asignatura, Estudiante } from '../../core/api.models';
import { mensajeError } from '../../core/errores';
import { NOTA_MINIMA_APROBATORIA, calcularPromedio, pesoTotal } from '../../core/promedio';
import { AsignaturaService } from '../../services/asignatura.service';
import { EstudianteService } from '../../services/estudiante.service';
import { NotaService } from '../../services/nota.service';

/** Fila editable del registro. Peso y valor son null mientras el docente no los escribe. */
export interface FilaNota {
  evaluacion: string;
  peso: number | null;
  valor: number | null;
}

export const FILAS_POR_DEFECTO: FilaNota[] = [
  { evaluacion: 'Parcial', peso: 30, valor: null },
  { evaluacion: 'Taller', peso: 30, valor: null },
  { evaluacion: 'Final', peso: 40, valor: null },
];

/**
 * Valida las filas antes de enviar (el backend vuelve a validar). Devuelve el primer error o ''.
 * Reglas: nombre y nota obligatorios, nota 0.0–5.0 con máx. 2 decimales, pesos todos o ninguno,
 * cada peso 1–100 y suma ≤ 100, nombres sin repetir.
 */
export function validarFilas(filas: FilaNota[]): string {
  if (filas.length === 0) {
    return 'Agrega al menos una evaluación.';
  }
  const nombres = new Set<string>();
  for (const [i, f] of filas.entries()) {
    const n = i + 1;
    const nombre = f.evaluacion.trim().toLowerCase();
    if (!nombre) {
      return `La evaluación ${n} necesita un nombre.`;
    }
    if (nombres.has(nombre)) {
      return `La evaluación "${f.evaluacion.trim()}" está repetida.`;
    }
    nombres.add(nombre);
    if (f.valor === null || !Number.isFinite(f.valor)) {
      return `Escribe la nota de "${f.evaluacion.trim()}".`;
    }
    if (f.valor < 0 || f.valor > 5) {
      return `La nota de "${f.evaluacion.trim()}" debe estar entre 0.0 y 5.0.`;
    }
    if (Math.abs(f.valor * 100 - Math.round(f.valor * 100)) > 1e-6) {
      return `La nota de "${f.evaluacion.trim()}" admite máximo 2 decimales.`;
    }
  }
  const conPeso = filas.filter((f) => f.peso !== null).length;
  if (conPeso !== 0 && conPeso !== filas.length) {
    return 'Los porcentajes se escriben en todas las evaluaciones o en ninguna.';
  }
  if (conPeso > 0) {
    if (filas.some((f) => !Number.isInteger(f.peso) || (f.peso as number) < 1 || (f.peso as number) > 100)) {
      return 'Cada porcentaje debe ser un entero entre 1 y 100.';
    }
    if (pesoTotal(filas) > 100) {
      return 'Los porcentajes no pueden sumar más de 100.';
    }
  }
  return '';
}

/** Registro y edición de calificaciones (RF-07, RF-08, mock-up 5.4). Solo el docente. */
@Component({
  selector: 'app-registro-notas',
  standalone: true,
  imports: [FormsModule],
  template: `
    <h1>Registrar notas</h1>
    <div class="tarjeta">
      <div class="fila">
        <div class="campo"><label for="est">Estudiante</label>
          <select id="est" name="estudiante" [ngModel]="estudianteId()" (ngModelChange)="elegir('estudiante', $event)">
            <option [ngValue]="null">— Selecciona —</option>
            @for (e of estudiantes(); track e.id) { <option [ngValue]="e.id">{{ e.nombre }}</option> }
          </select></div>
        <div class="campo"><label for="asi">Asignatura</label>
          <select id="asi" name="asignatura" [ngModel]="asignaturaId()" (ngModelChange)="elegir('asignatura', $event)">
            <option [ngValue]="null">— Selecciona —</option>
            @for (a of asignaturas(); track a.id) { <option [ngValue]="a.id">{{ a.nombre }}</option> }
          </select></div>
        <div class="campo"><label for="per">Periodo (AAAA-1 o AAAA-2)</label>
          <input id="per" name="periodo" [ngModel]="periodo()" (ngModelChange)="elegir('periodo', $event)" placeholder="2026-2" maxlength="6" /></div>
      </div>
    </div>

    @if (listo()) {
      <div class="tarjeta">
        <div class="scroll">
          <table>
            <thead><tr><th>Evaluación</th><th class="num">Peso (%)</th><th class="num">Nota (0.0–5.0)</th><th></th></tr></thead>
            <tbody>
              @for (f of filas(); track $index) {
                <tr>
                  <td><input [name]="'ev' + $index" [(ngModel)]="f.evaluacion" (ngModelChange)="tocar()" maxlength="100" aria-label="Evaluación" /></td>
                  <td class="num"><input type="number" [name]="'pe' + $index" [(ngModel)]="f.peso" (ngModelChange)="tocar()" min="1" max="100" step="1" style="width: 90px" aria-label="Peso" /></td>
                  <td class="num"><input type="number" [name]="'va' + $index" [(ngModel)]="f.valor" (ngModelChange)="tocar()" min="0" max="5" step="0.1" style="width: 90px" aria-label="Nota" /></td>
                  <td class="acciones"><button type="button" class="btn btn-secundario btn-pequeno" (click)="quitar($index)">Quitar</button></td>
                </tr>
              }
            </tbody>
          </table>
        </div>
        <div class="fila" style="margin-top: 1rem; justify-content: space-between">
          <button type="button" class="btn btn-secundario" (click)="agregar()">+ Agregar evaluación</button>
          <div>
            Peso total: <strong>{{ peso() }}%</strong> ·
            Promedio:
            <strong [class.reprobado]="promedio() !== null && promedio()! < minimo"
                    [class.aprobado]="promedio() !== null && promedio()! >= minimo">{{ promedio() ?? '–' }}</strong>
          </div>
        </div>
        @if (error()) { <p class="error" role="alert">{{ error() }}</p> }
        @if (mensaje()) { <p class="ok" role="status">{{ mensaje() }}</p> }
        <button type="button" class="btn" style="margin-top: 1rem" [disabled]="guardando()" (click)="guardar()">GUARDAR NOTAS</button>
      </div>
    }
  `,
})
export class RegistroNotasComponent implements OnInit {
  private readonly estudianteService = inject(EstudianteService);
  private readonly asignaturaService = inject(AsignaturaService);
  private readonly notaService = inject(NotaService);

  protected readonly minimo = NOTA_MINIMA_APROBATORIA;

  readonly estudiantes = signal<Estudiante[]>([]);
  readonly asignaturas = signal<Asignatura[]>([]);
  readonly estudianteId = signal<number | null>(null);
  readonly asignaturaId = signal<number | null>(null);
  readonly periodo = signal('');
  readonly filas = signal<FilaNota[]>(structuredClone(FILAS_POR_DEFECTO));
  readonly error = signal('');
  readonly mensaje = signal('');
  readonly guardando = signal(false);

  /** Se muestra la tabla cuando hay estudiante, asignatura y un periodo con formato válido. */
  readonly listo = computed(
    () => this.estudianteId() !== null && this.asignaturaId() !== null && /^\d{4}-[12]$/.test(this.periodo())
  );

  // Los inputs modifican los objetos de las filas; este contador fuerza el recálculo.
  private readonly version = signal(0);
  readonly promedio = computed(() => { this.version(); return calcularPromedio(this.filas()); });
  readonly peso = computed(() => { this.version(); return pesoTotal(this.filas()); });

  ngOnInit(): void {
    this.estudianteService.listar().subscribe({
      next: (l) => this.estudiantes.set(l),
      error: (err) => this.error.set(mensajeError(err, 'No se pudieron cargar los estudiantes.')),
    });
    this.asignaturaService.listar(true).subscribe({
      next: (l) => this.asignaturas.set(l),
      error: (err) => this.error.set(mensajeError(err, 'No se pudieron cargar las asignaturas.')),
    });
  }

  tocar(): void {
    this.version.update((v) => v + 1);
    this.mensaje.set('');
  }

  elegir(campo: 'estudiante' | 'asignatura' | 'periodo', valor: unknown): void {
    if (campo === 'estudiante') {
      this.estudianteId.set(valor as number | null);
    } else if (campo === 'asignatura') {
      this.asignaturaId.set(valor as number | null);
    } else {
      this.periodo.set(String(valor ?? '').trim());
    }
    this.mensaje.set('');
    this.error.set('');
    if (this.listo()) {
      this.cargarExistentes();
    }
  }

  /** Si ya hay notas para la combinación, se cargan para editarlas; si no, filas por defecto. */
  private cargarExistentes(): void {
    this.notaService.listar(this.estudianteId()!, this.asignaturaId()!, this.periodo()).subscribe({
      next: (notas) => {
        this.filas.set(
          notas.length > 0
            ? notas.map((n) => ({ evaluacion: n.evaluacion, peso: n.peso, valor: n.valor }))
            : structuredClone(FILAS_POR_DEFECTO)
        );
        this.tocar();
      },
      error: (err) => this.error.set(mensajeError(err, 'No se pudieron cargar las notas.')),
    });
  }

  agregar(): void {
    this.filas.update((f) => [...f, { evaluacion: '', peso: null, valor: null }]);
    this.tocar();
  }

  quitar(indice: number): void {
    this.filas.update((f) => f.filter((_, i) => i !== indice));
    this.tocar();
  }

  guardar(): void {
    this.mensaje.set('');
    const problema = validarFilas(this.filas());
    this.error.set(problema);
    if (problema) {
      return;
    }
    this.guardando.set(true);
    this.notaService
      .registrar({
        estudianteId: this.estudianteId()!,
        asignaturaId: this.asignaturaId()!,
        periodo: this.periodo(),
        evaluaciones: this.filas().map((f) => ({
          evaluacion: f.evaluacion.trim(),
          peso: f.peso,
          valor: f.valor as number,
        })),
      })
      .subscribe({
        next: (r) => {
          this.guardando.set(false);
          this.filas.set(r.notas.map((n) => ({ evaluacion: n.evaluacion, peso: n.peso, valor: n.valor })));
          this.tocar();
          this.mensaje.set(`Notas guardadas. Promedio: ${r.promedio ?? '–'}`);
        },
        error: (err) => {
          this.guardando.set(false);
          this.error.set(mensajeError(err, 'No se pudieron guardar las notas.'));
        },
      });
  }
}
