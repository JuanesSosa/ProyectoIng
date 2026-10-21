import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { Actividad, Grupo, ResumenGrupo } from '../../core/api.models';
import { mensajeError } from '../../core/errores';
import { NOTA_MINIMA_APROBATORIA } from '../../core/promedio';
import { ActividadService } from '../../services/actividad.service';
import { GrupoService } from '../../services/grupo.service';

/**
 * Actividades de un grupo (boceto 2) y seguimiento de sus estudiantes.
 * El docente define actividades con su %; al abrir una se llega a su rúbrica para calificar.
 */
@Component({
  selector: 'app-grupo-actividades',
  standalone: true,
  imports: [FormsModule, RouterLink],
  template: `
    <p><a routerLink="/notas">← Asignaturas y grupos</a></p>
    @if (grupo(); as g) {
      <h1>{{ g.asignaturaNombre }} · {{ g.nombre }} <span class="suave">({{ g.periodo }})</span></h1>
    }
    @if (error()) { <p class="error" role="alert">{{ error() }}</p> }

    <div class="tarjeta">
      <div class="cabecera">
        <h2>Actividades a calificar</h2>
        @if (puedeEditar && !formAbierto()) {
          <button type="button" class="btn btn-pequeno" (click)="nueva()">+ Agregar actividad</button>
        }
      </div>

      @if (formAbierto()) {
        <form class="fila formulario" (ngSubmit)="guardar()" #f="ngForm">
          <div class="campo"><label for="nombre">Nombre</label>
            <input id="nombre" name="nombre" [(ngModel)]="form.nombre" required maxlength="100" placeholder="Parcial 1" /></div>
          <div class="campo"><label for="peso">Porcentaje (%)</label>
            <input id="peso" name="peso" type="number" [(ngModel)]="form.peso" required min="1" max="100" step="1" /></div>
          <button class="btn" type="submit" [disabled]="f.invalid">Guardar</button>
          <button class="btn btn-secundario" type="button" (click)="cerrar()">Cancelar</button>
        </form>
        @if (errorForm()) { <p class="error" role="alert">{{ errorForm() }}</p> }
      }

      <div class="scroll">
        <table>
          <thead><tr><th>Actividad</th><th class="num">Peso</th><th class="num">Rúbrica</th><th></th></tr></thead>
          <tbody>
            @for (a of actividades(); track a.id) {
              <tr>
                <td><a [routerLink]="['/notas/actividad', a.id]">{{ a.nombre }}</a></td>
                <td class="num">{{ a.peso }}%</td>
                <td class="num">
                  @if (a.criterios.length === 0) { <span class="reprobado">Sin definir</span> }
                  @else { {{ a.criterios.length }} criterios ({{ a.pesoCriterios }}%) }
                </td>
                <td class="acciones">
                  <a class="btn btn-pequeno" [routerLink]="['/notas/actividad', a.id]">{{ puedeEditar ? 'Calificar' : 'Ver' }}</a>
                  @if (puedeEditar) {
                    <button type="button" class="btn btn-secundario btn-pequeno" (click)="editar(a)">Editar</button>
                    <button type="button" class="btn btn-peligro btn-pequeno" (click)="eliminar(a)">Eliminar</button>
                  }
                </td>
              </tr>
            } @empty {
              <tr><td colspan="4" class="suave">Aún no hay actividades.</td></tr>
            }
          </tbody>
          @if (actividades().length > 0) {
            <tfoot><tr><td><strong>Total</strong></td>
              <td class="num"><strong [class.reprobado]="pesoTotal() !== 100">{{ pesoTotal() }}%</strong></td>
              <td colspan="2" class="suave">@if (pesoTotal() !== 100) { Los porcentajes deben sumar 100 para llegar a la nota final. }</td></tr></tfoot>
          }
        </table>
      </div>
    </div>

    @if (progreso().length > 0) {
      <div class="tarjeta">
        <h2>Progreso de los estudiantes</h2>
        <p class="suave">Acumulado = puntos ganados sobre 5.0 (lo no calificado cuenta 0). Promedio = nota de lo evaluado hasta ahora.</p>
        <div class="scroll">
          <table>
            <thead>
              <tr>
                <th>Estudiante</th>
                @for (a of actividades(); track a.id) { <th class="num">{{ a.nombre }}</th> }
                <th class="num">Avance</th><th class="num">Acumulado</th><th class="num">Promedio</th>
              </tr>
            </thead>
            <tbody>
              @for (r of progreso(); track r.estudianteId) {
                <tr>
                  <td>{{ r.estudianteNombre }}</td>
                  @for (ar of r.actividades; track ar.actividadId) {
                    <td class="num" [class.reprobado]="ar.nota !== null && ar.nota < minimo">{{ ar.nota ?? '–' }}</td>
                  }
                  <td class="num">{{ r.avance }}%</td>
                  <td class="num"><strong>{{ r.acumulado }}</strong></td>
                  <td class="num" [class.reprobado]="r.promedio !== null && r.promedio < minimo">{{ r.promedio ?? '–' }}</td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      </div>
    }
  `,
  styles: [
    `
      h2 { margin: 0; font-size: 1.1rem; }
      .cabecera { margin-bottom: 0.75rem; }
      .formulario { margin-bottom: 0.75rem; }
      a { color: var(--color-primario); }
      a.btn { color: #fff; text-decoration: none; }
    `,
  ],
})
export class GrupoActividadesComponent implements OnInit {
  private readonly ruta = inject(ActivatedRoute);
  private readonly grupos = inject(GrupoService);
  private readonly actividadService = inject(ActividadService);
  /** Solo el docente define actividades; el administrador las consulta. */
  protected readonly puedeEditar = inject(AuthService).tieneRol('DOCENTE');
  protected readonly minimo = NOTA_MINIMA_APROBATORIA;

  readonly grupo = signal<Grupo | null>(null);
  readonly actividades = signal<Actividad[]>([]);
  readonly progreso = signal<ResumenGrupo[]>([]);
  readonly error = signal('');
  readonly errorForm = signal('');
  readonly formAbierto = signal(false);
  readonly editandoId = signal<number | null>(null);
  form: { nombre: string; peso: number | null } = { nombre: '', peso: null };
  private grupoId = 0;

  ngOnInit(): void {
    this.grupoId = Number(this.ruta.snapshot.paramMap.get('id'));
    this.grupos.obtener(this.grupoId).subscribe({
      next: (g) => this.grupo.set(g),
      error: (err) => this.error.set(mensajeError(err, 'No se pudo cargar el grupo.')),
    });
    this.cargar();
  }

  pesoTotal(): number {
    return this.actividades().reduce((suma, a) => suma + a.peso, 0);
  }

  cargar(): void {
    this.grupos.actividades(this.grupoId).subscribe({
      next: (lista) => this.actividades.set(lista),
      error: (err) => this.error.set(mensajeError(err, 'No se pudieron cargar las actividades.')),
    });
    this.grupos.progreso(this.grupoId).subscribe({
      next: (lista) => this.progreso.set(lista),
      error: () => this.progreso.set([]),
    });
  }

  nueva(): void {
    this.form = { nombre: '', peso: null };
    this.editandoId.set(null);
    this.errorForm.set('');
    this.formAbierto.set(true);
  }

  editar(a: Actividad): void {
    this.form = { nombre: a.nombre, peso: a.peso };
    this.editandoId.set(a.id);
    this.errorForm.set('');
    this.formAbierto.set(true);
  }

  cerrar(): void {
    this.formAbierto.set(false);
  }

  guardar(): void {
    const datos = { nombre: this.form.nombre.trim(), peso: Number(this.form.peso) };
    const id = this.editandoId();
    const peticion = id === null ? this.grupos.crearActividad(this.grupoId, datos) : this.actividadService.actualizar(id, datos);
    peticion.subscribe({
      next: () => { this.cerrar(); this.cargar(); },
      error: (err) => this.errorForm.set(mensajeError(err, 'No se pudo guardar la actividad.')),
    });
  }

  eliminar(a: Actividad): void {
    if (!confirm(`¿Eliminar la actividad ${a.nombre}?`)) {
      return;
    }
    this.actividadService.eliminar(a.id).subscribe({
      next: () => this.cargar(),
      error: (err) => this.error.set(mensajeError(err, 'No se pudo eliminar la actividad.')),
    });
  }
}
