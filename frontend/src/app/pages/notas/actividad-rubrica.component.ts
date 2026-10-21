import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { Actividad, EstudianteActividad } from '../../core/api.models';
import { mensajeError } from '../../core/errores';
import { NOTA_MINIMA_APROBATORIA, avanceRubrica, calcularPromedio } from '../../core/promedio';
import { ActividadService } from '../../services/actividad.service';

/** Fila de la rúbrica con la nota que se está escribiendo (null = sin calificar). */
export interface FilaCalificacion {
  criterioId: number;
  nombre: string;
  peso: number;
  valor: number | null;
}

/** Fila de la rúbrica en edición (id undefined = criterio nuevo). */
export interface FilaCriterio {
  id?: number;
  nombre: string;
  peso: number | null;
}

/** Valida las notas escritas: vacías o entre 0.0 y 5.0 con máximo 2 decimales. Devuelve el primer error o ''. */
export function validarCalificaciones(filas: FilaCalificacion[]): string {
  for (const f of filas) {
    if (f.valor === null) {
      continue;
    }
    if (!Number.isFinite(f.valor) || f.valor < 0 || f.valor > 5) {
      return `La nota de "${f.nombre}" debe estar entre 0.0 y 5.0.`;
    }
    if (Math.abs(f.valor * 100 - Math.round(f.valor * 100)) > 1e-6) {
      return `La nota de "${f.nombre}" admite máximo 2 decimales.`;
    }
  }
  return '';
}

/** Valida la rúbrica en edición: al menos un criterio, nombres únicos, % enteros de 1 a 100 y suma máxima 100. */
export function validarRubrica(filas: FilaCriterio[]): string {
  if (filas.length === 0) {
    return 'La rúbrica necesita al menos un criterio.';
  }
  const nombres = new Set<string>();
  let total = 0;
  for (const f of filas) {
    const nombre = f.nombre.trim();
    if (!nombre) {
      return 'Todos los criterios necesitan un nombre.';
    }
    if (nombres.has(nombre.toLowerCase())) {
      return `El criterio "${nombre}" está repetido.`;
    }
    nombres.add(nombre.toLowerCase());
    if (f.peso === null || !Number.isInteger(f.peso) || f.peso < 1 || f.peso > 100) {
      return `El porcentaje de "${nombre}" debe ser un entero entre 1 y 100.`;
    }
    total += f.peso;
  }
  if (total > 100) {
    return `Los porcentajes de la rúbrica no pueden sumar más de 100 (suman ${total}).`;
  }
  return '';
}

/**
 * Rúbrica de una actividad (boceto 3): se elige un estudiante, se ve la rúbrica de evaluación y se escribe
 * la nota de cada criterio; el total se calcula solo. El docente también puede editar los criterios.
 */
@Component({
  selector: 'app-actividad-rubrica',
  standalone: true,
  imports: [FormsModule, RouterLink],
  template: `
    @if (actividad(); as a) {
      <p><a [routerLink]="['/notas/grupo', a.grupoId]">← Actividades del grupo</a></p>
      <h1>{{ a.nombre }} <span class="suave">({{ a.peso }}% de la nota final)</span></h1>
    }
    @if (error()) { <p class="error" role="alert">{{ error() }}</p> }

    <div class="tarjeta">
      <div class="campo" style="max-width: 420px">
        <label for="est">Seleccionar estudiante</label>
        <select id="est" name="estudiante" [ngModel]="estudianteId()" (ngModelChange)="elegir($event)" [disabled]="editandoRubrica()">
          <option [ngValue]="null">— Selecciona —</option>
          @for (e of estudiantes(); track e.estudianteId) {
            <option [ngValue]="e.estudianteId">{{ e.nombre }} ({{ e.nota === null ? 'sin calificar' : e.nota }})</option>
          }
        </select>
      </div>
    </div>

    <div class="tarjeta">
      <div class="cabecera">
        <h2>Rúbrica de evaluación</h2>
        @if (puedeEditar && !editandoRubrica()) {
          <button type="button" class="btn btn-secundario btn-pequeno" (click)="editarRubrica()">Editar rúbrica</button>
        }
      </div>

      @if (editandoRubrica()) {
        <div class="scroll">
          <table>
            <thead><tr><th>Criterio</th><th class="num">Peso (%)</th><th></th></tr></thead>
            <tbody>
              @for (f of criterios(); track $index) {
                <tr>
                  <td><input [name]="'cn' + $index" [(ngModel)]="f.nombre" maxlength="100" aria-label="Criterio" placeholder="Teoría" /></td>
                  <td class="num"><input type="number" [name]="'cp' + $index" [(ngModel)]="f.peso" min="1" max="100" step="1" style="width: 90px" aria-label="Peso" /></td>
                  <td class="acciones"><button type="button" class="btn btn-secundario btn-pequeno" (click)="quitarCriterio($index)">Quitar</button></td>
                </tr>
              }
            </tbody>
            <tfoot><tr><td><strong>Total</strong></td><td class="num"><strong [class.reprobado]="pesoCriterios() !== 100">{{ pesoCriterios() }}%</strong></td><td></td></tr></tfoot>
          </table>
        </div>
        @if (errorRubrica()) { <p class="error" role="alert">{{ errorRubrica() }}</p> }
        <div class="fila" style="margin-top: 1rem">
          <button type="button" class="btn btn-secundario" (click)="agregarCriterio()">+ Agregar criterio</button>
          <button type="button" class="btn" (click)="guardarRubrica()">Guardar rúbrica</button>
          <button type="button" class="btn btn-secundario" (click)="cancelarRubrica()">Cancelar</button>
        </div>
      } @else if (actividad() && actividad()!.criterios.length === 0) {
        <p class="suave">
          Esta actividad todavía no tiene rúbrica.
          @if (puedeEditar) { Pulsa "Editar rúbrica" para definir los criterios y sus porcentajes. }
        </p>
      } @else if (estudianteId() === null) {
        <p class="suave">Selecciona un estudiante para ver y registrar su calificación.</p>
        <ul>
          @for (c of actividad()?.criterios ?? []; track c.id) { <li>{{ c.nombre }} — {{ c.peso }}%</li> }
        </ul>
      } @else {
        <div class="scroll">
          <table>
            <thead><tr><th>Criterio</th><th class="num">Peso</th><th class="num">Nota (0.0–5.0)</th></tr></thead>
            <tbody>
              @for (f of filas(); track f.criterioId) {
                <tr>
                  <td>{{ f.nombre }}</td>
                  <td class="num">{{ f.peso }}%</td>
                  <td class="num">
                    <input type="number" [name]="'n' + f.criterioId" [(ngModel)]="f.valor" (ngModelChange)="tocar()"
                           min="0" max="5" step="0.1" style="width: 90px" [disabled]="!puedeEditar" [attr.aria-label]="'Nota de ' + f.nombre" />
                  </td>
                </tr>
              }
            </tbody>
            <tfoot>
              <tr>
                <td><strong>Total</strong></td>
                <td class="num">{{ pesoFilas() }}%</td>
                <td class="num">
                  <strong [class.reprobado]="nota() !== null && nota()! < minimo" [class.aprobado]="nota() !== null && nota()! >= minimo">{{ nota() ?? '–' }}</strong>
                </td>
              </tr>
            </tfoot>
          </table>
        </div>
        <p class="suave">Calificado: {{ avance() }}% de la rúbrica. La nota total es el promedio de lo calificado.</p>
        @if (error2()) { <p class="error" role="alert">{{ error2() }}</p> }
        @if (mensaje()) { <p class="ok" role="status">{{ mensaje() }}</p> }
        @if (puedeEditar) {
          <button type="button" class="btn" [disabled]="guardando()" (click)="guardar()">GUARDAR NOTAS</button>
        }
      }
    </div>
  `,
  styles: [
    `
      h2 { margin: 0; font-size: 1.1rem; }
      .cabecera { margin-bottom: 0.75rem; }
      a { color: var(--color-primario); }
    `,
  ],
})
export class ActividadRubricaComponent implements OnInit {
  private readonly ruta = inject(ActivatedRoute);
  private readonly servicio = inject(ActividadService);
  /** Solo el docente califica y edita la rúbrica; el administrador consulta. */
  protected readonly puedeEditar = inject(AuthService).tieneRol('DOCENTE');
  protected readonly minimo = NOTA_MINIMA_APROBATORIA;

  readonly actividad = signal<Actividad | null>(null);
  readonly estudiantes = signal<EstudianteActividad[]>([]);
  readonly estudianteId = signal<number | null>(null);
  readonly filas = signal<FilaCalificacion[]>([]);
  readonly error = signal('');
  readonly error2 = signal('');
  readonly mensaje = signal('');
  readonly guardando = signal(false);

  readonly editandoRubrica = signal(false);
  readonly criterios = signal<FilaCriterio[]>([]);
  readonly errorRubrica = signal('');

  // Los inputs modifican los objetos de las filas; este contador fuerza el recálculo de los totales.
  private readonly version = signal(0);
  readonly nota = computed(() => { this.version(); return calcularPromedio(this.filas()); });
  readonly avance = computed(() => { this.version(); return avanceRubrica(this.filas()); });
  readonly pesoFilas = computed(() => this.filas().reduce((suma, f) => suma + f.peso, 0));
  readonly pesoCriterios = computed(() => {
    this.version();
    return this.criterios().reduce((suma, f) => suma + (Number(f.peso) || 0), 0);
  });

  private actividadId = 0;

  ngOnInit(): void {
    this.actividadId = Number(this.ruta.snapshot.paramMap.get('id'));
    this.cargarActividad();
    this.cargarEstudiantes();
  }

  private cargarActividad(): void {
    this.servicio.obtener(this.actividadId).subscribe({
      next: (a) => this.actividad.set(a),
      error: (err) => this.error.set(mensajeError(err, 'No se pudo cargar la actividad.')),
    });
  }

  private cargarEstudiantes(): void {
    this.servicio.estudiantes(this.actividadId).subscribe({
      next: (lista) => this.estudiantes.set(lista),
      error: (err) => this.error.set(mensajeError(err, 'No se pudieron cargar los estudiantes.')),
    });
  }

  tocar(): void {
    this.version.update((v) => v + 1);
    this.mensaje.set('');
  }

  elegir(id: number | null): void {
    this.estudianteId.set(id);
    this.mensaje.set('');
    this.error2.set('');
    this.filas.set([]);
    if (id === null) {
      return;
    }
    this.servicio.rubrica(this.actividadId, id).subscribe({
      next: (r) => {
        this.filas.set(r.criterios.map((c) => ({ criterioId: c.criterioId, nombre: c.nombre, peso: c.peso, valor: c.valor })));
        this.tocar();
      },
      error: (err) => this.error2.set(mensajeError(err, 'No se pudo cargar la rúbrica del estudiante.')),
    });
  }

  guardar(): void {
    const id = this.estudianteId();
    const problema = validarCalificaciones(this.filas());
    this.error2.set(problema);
    this.mensaje.set('');
    if (problema || id === null) {
      return;
    }
    this.guardando.set(true);
    this.servicio
      .calificar(this.actividadId, id, this.filas().map((f) => ({ criterioId: f.criterioId, valor: f.valor })))
      .subscribe({
        next: (r) => {
          this.guardando.set(false);
          this.filas.set(r.criterios.map((c) => ({ criterioId: c.criterioId, nombre: c.nombre, peso: c.peso, valor: c.valor })));
          this.tocar();
          this.mensaje.set(`Notas guardadas. Nota de la actividad: ${r.nota ?? '–'}`);
          this.cargarEstudiantes();
        },
        error: (err) => {
          this.guardando.set(false);
          this.error2.set(mensajeError(err, 'No se pudieron guardar las notas.'));
        },
      });
  }

  // ---------- edición de la rúbrica (docente) ----------

  editarRubrica(): void {
    const a = this.actividad();
    this.criterios.set(a ? a.criterios.map((c) => ({ id: c.id, nombre: c.nombre, peso: c.peso })) : []);
    if (this.criterios().length === 0) {
      this.agregarCriterio();
    }
    this.errorRubrica.set('');
    this.editandoRubrica.set(true);
    this.tocar();
  }

  agregarCriterio(): void {
    this.criterios.update((c) => [...c, { nombre: '', peso: null }]);
    this.tocar();
  }

  quitarCriterio(indice: number): void {
    this.criterios.update((c) => c.filter((_, i) => i !== indice));
    this.tocar();
  }

  cancelarRubrica(): void {
    this.editandoRubrica.set(false);
  }

  guardarRubrica(): void {
    const problema = validarRubrica(this.criterios());
    this.errorRubrica.set(problema);
    if (problema) {
      return;
    }
    const criterios = this.criterios().map((c) => ({ id: c.id, nombre: c.nombre.trim(), peso: c.peso as number }));
    this.servicio.guardarRubrica(this.actividadId, criterios).subscribe({
      next: (a) => {
        this.actividad.set(a);
        this.editandoRubrica.set(false);
        this.cargarEstudiantes();
        this.elegir(this.estudianteId());
      },
      error: (err) => this.errorRubrica.set(mensajeError(err, 'No se pudo guardar la rúbrica.')),
    });
  }
}
