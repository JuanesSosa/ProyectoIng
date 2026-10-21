import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Asignatura, Estudiante, Grupo, Matriculado, Usuario } from '../../core/api.models';
import { mensajeError } from '../../core/errores';
import { AsignaturaService } from '../../services/asignatura.service';
import { EstudianteService } from '../../services/estudiante.service';
import { GrupoService } from '../../services/grupo.service';
import { UsuarioService } from '../../services/usuario.service';

/** Gestión de grupos y matrícula de estudiantes (solo administrador). */
@Component({
  selector: 'app-grupos',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="cabecera">
      <h1>Grupos</h1>
      @if (!formAbierto()) { <button type="button" class="btn" (click)="nuevo()">+ Agregar</button> }
    </div>

    @if (formAbierto()) {
      <form class="tarjeta" (ngSubmit)="guardar()" #f="ngForm">
        <h2>{{ editandoId() === null ? 'Nuevo grupo' : 'Editar grupo' }}</h2>
        <div class="fila">
          <div class="campo"><label for="asi">Asignatura</label>
            <select id="asi" name="asignaturaId" [(ngModel)]="form.asignaturaId" required>
              <option [ngValue]="null">— Selecciona —</option>
              @for (a of asignaturas(); track a.id) { <option [ngValue]="a.id">{{ a.nombre }}</option> }
            </select></div>
          <div class="campo"><label for="nom">Nombre del grupo</label>
            <input id="nom" name="nombre" [(ngModel)]="form.nombre" required maxlength="50" placeholder="Grupo 1" /></div>
          <div class="campo"><label for="per">Periodo (AAAA-1 o AAAA-2)</label>
            <input id="per" name="periodo" [(ngModel)]="form.periodo" required maxlength="6" placeholder="2026-2" /></div>
          <div class="campo"><label for="doc">Docente</label>
            <select id="doc" name="docenteId" [(ngModel)]="form.docenteId" required>
              <option [ngValue]="null">— Selecciona —</option>
              @for (d of docentes(); track d.id) { <option [ngValue]="d.id">{{ d.nombre }}</option> }
            </select></div>
        </div>
        @if (errorForm()) { <p class="error" role="alert">{{ errorForm() }}</p> }
        <div class="fila" style="margin-top: 1rem">
          <button class="btn" type="submit" [disabled]="f.invalid">Guardar</button>
          <button class="btn btn-secundario" type="button" (click)="cerrar()">Cancelar</button>
        </div>
      </form>
    }

    <div class="tarjeta">
      @if (error()) { <p class="error" role="alert">{{ error() }}</p> }
      <div class="scroll">
        <table>
          <thead><tr><th>Asignatura</th><th>Grupo</th><th>Periodo</th><th>Docente</th><th class="num">Estudiantes</th><th></th></tr></thead>
          <tbody>
            @for (g of grupos(); track g.id) {
              <tr>
                <td>{{ g.asignaturaNombre }}</td><td>{{ g.nombre }}</td><td>{{ g.periodo }}</td><td>{{ g.docenteNombre }}</td>
                <td class="num">{{ g.estudiantes }}</td>
                <td class="acciones">
                  <button type="button" class="btn btn-pequeno" (click)="abrirMatricula(g)">Estudiantes</button>
                  <button type="button" class="btn btn-secundario btn-pequeno" (click)="editar(g)">Editar</button>
                  <button type="button" class="btn btn-peligro btn-pequeno" (click)="eliminar(g)">Eliminar</button>
                </td>
              </tr>
            } @empty {
              <tr><td colspan="6" class="suave">No hay grupos.</td></tr>
            }
          </tbody>
        </table>
      </div>
    </div>

    @if (seleccionado(); as g) {
      <div class="tarjeta">
        <div class="cabecera">
          <h2>Estudiantes de {{ g.asignaturaNombre }} · {{ g.nombre }} ({{ g.periodo }})</h2>
          <button type="button" class="btn btn-secundario btn-pequeno" (click)="seleccionado.set(null)">Cerrar</button>
        </div>
        <div class="fila" style="margin-bottom: 1rem">
          <div class="campo"><label for="nuevo">Matricular estudiante</label>
            <select id="nuevo" name="nuevo" [(ngModel)]="aMatricular">
              <option [ngValue]="null">— Selecciona —</option>
              @for (e of disponibles(); track e.id) { <option [ngValue]="e.id">{{ e.nombre }} ({{ e.documento }})</option> }
            </select></div>
          <button type="button" class="btn" [disabled]="aMatricular === null" (click)="matricular()">Matricular</button>
        </div>
        @if (errorMatricula()) { <p class="error" role="alert">{{ errorMatricula() }}</p> }
        <table>
          <thead><tr><th>Documento</th><th>Nombre</th><th>Curso</th><th></th></tr></thead>
          <tbody>
            @for (m of matriculados(); track m.estudianteId) {
              <tr>
                <td>{{ m.documento }}</td><td>{{ m.nombre }}</td><td>{{ m.curso }}</td>
                <td class="acciones"><button type="button" class="btn btn-peligro btn-pequeno" (click)="quitar(m)">Quitar</button></td>
              </tr>
            } @empty {
              <tr><td colspan="4" class="suave">Aún no hay estudiantes matriculados.</td></tr>
            }
          </tbody>
        </table>
      </div>
    }
  `,
  styles: ['h2 { margin: 0 0 0.75rem; font-size: 1.1rem; } .cabecera h2 { margin: 0; }'],
})
export class GruposComponent implements OnInit {
  private readonly servicio = inject(GrupoService);
  private readonly asignaturaService = inject(AsignaturaService);
  private readonly usuarioService = inject(UsuarioService);
  private readonly estudianteService = inject(EstudianteService);

  readonly grupos = signal<Grupo[]>([]);
  readonly asignaturas = signal<Asignatura[]>([]);
  readonly docentes = signal<Usuario[]>([]);
  readonly estudiantes = signal<Estudiante[]>([]);
  readonly matriculados = signal<Matriculado[]>([]);
  readonly seleccionado = signal<Grupo | null>(null);
  readonly error = signal('');
  readonly errorForm = signal('');
  readonly errorMatricula = signal('');
  readonly formAbierto = signal(false);
  readonly editandoId = signal<number | null>(null);
  form: { asignaturaId: number | null; nombre: string; periodo: string; docenteId: number | null } = this.vacio();
  aMatricular: number | null = null;

  /** Estudiantes que aún no están en el grupo seleccionado. */
  readonly disponibles = computed(() => {
    const ids = new Set(this.matriculados().map((m) => m.estudianteId));
    return this.estudiantes().filter((e) => e.id !== undefined && !ids.has(e.id));
  });

  ngOnInit(): void {
    this.cargar();
    this.asignaturaService.listar(true).subscribe({ next: (l) => this.asignaturas.set(l), error: () => undefined });
    this.usuarioService.listar().subscribe({
      next: (l) => this.docentes.set(l.filter((u) => u.rol === 'DOCENTE' && u.activo)),
      error: () => undefined,
    });
    this.estudianteService.listar().subscribe({ next: (l) => this.estudiantes.set(l), error: () => undefined });
  }

  private vacio() {
    return { asignaturaId: null as number | null, nombre: '', periodo: '', docenteId: null as number | null };
  }

  cargar(): void {
    this.servicio.listar().subscribe({
      next: (lista) => { this.grupos.set(lista); this.error.set(''); },
      error: (err) => this.error.set(mensajeError(err, 'No se pudieron cargar los grupos.')),
    });
  }

  nuevo(): void {
    this.form = this.vacio();
    this.editandoId.set(null);
    this.errorForm.set('');
    this.formAbierto.set(true);
  }

  editar(g: Grupo): void {
    this.form = { asignaturaId: g.asignaturaId, nombre: g.nombre, periodo: g.periodo, docenteId: g.docenteId };
    this.editandoId.set(g.id ?? null);
    this.errorForm.set('');
    this.formAbierto.set(true);
  }

  cerrar(): void {
    this.formAbierto.set(false);
  }

  guardar(): void {
    const datos: Grupo = {
      asignaturaId: this.form.asignaturaId as number,
      nombre: this.form.nombre.trim(),
      periodo: this.form.periodo.trim(),
      docenteId: this.form.docenteId as number,
    };
    const id = this.editandoId();
    const peticion = id === null ? this.servicio.crear(datos) : this.servicio.actualizar(id, datos);
    peticion.subscribe({
      next: () => { this.cerrar(); this.cargar(); },
      error: (err) => this.errorForm.set(mensajeError(err, 'No se pudo guardar el grupo.')),
    });
  }

  eliminar(g: Grupo): void {
    if (g.id === undefined || !confirm(`¿Eliminar ${g.asignaturaNombre} · ${g.nombre}?`)) {
      return;
    }
    this.servicio.eliminar(g.id).subscribe({
      next: () => { this.cargar(); if (this.seleccionado()?.id === g.id) { this.seleccionado.set(null); } },
      error: (err) => this.error.set(mensajeError(err, 'No se pudo eliminar el grupo.')),
    });
  }

  // ---------- matrícula ----------

  abrirMatricula(g: Grupo): void {
    this.seleccionado.set(g);
    this.aMatricular = null;
    this.errorMatricula.set('');
    this.cargarMatriculados();
  }

  private cargarMatriculados(): void {
    const g = this.seleccionado();
    if (g?.id === undefined) {
      return;
    }
    this.servicio.estudiantes(g.id).subscribe({
      next: (lista) => this.matriculados.set(lista),
      error: (err) => this.errorMatricula.set(mensajeError(err, 'No se pudieron cargar los estudiantes del grupo.')),
    });
  }

  matricular(): void {
    const g = this.seleccionado();
    if (g?.id === undefined || this.aMatricular === null) {
      return;
    }
    this.servicio.matricular(g.id, this.aMatricular).subscribe({
      next: () => { this.aMatricular = null; this.errorMatricula.set(''); this.cargarMatriculados(); this.cargar(); },
      error: (err) => this.errorMatricula.set(mensajeError(err, 'No se pudo matricular al estudiante.')),
    });
  }

  quitar(m: Matriculado): void {
    const g = this.seleccionado();
    if (g?.id === undefined || !confirm(`¿Quitar a ${m.nombre} del grupo?`)) {
      return;
    }
    this.servicio.desmatricular(g.id, m.estudianteId).subscribe({
      next: () => { this.errorMatricula.set(''); this.cargarMatriculados(); this.cargar(); },
      error: (err) => this.errorMatricula.set(mensajeError(err, 'No se pudo quitar al estudiante.')),
    });
  }
}
