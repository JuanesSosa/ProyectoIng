import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/auth.service';
import { Estudiante } from '../../core/api.models';
import { mensajeError } from '../../core/errores';
import { EstudianteService } from '../../services/estudiante.service';

/** Gestión de estudiantes (RF-04, mock-up 5.3). Docente: solo consulta; administrador: todo. */
@Component({
  selector: 'app-estudiantes',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="cabecera">
      <h1>Estudiantes</h1>
      @if (esAdmin && !formAbierto()) {
        <button type="button" class="btn" (click)="nuevo()">+ Agregar</button>
      }
    </div>

    @if (formAbierto()) {
      <form class="tarjeta" (ngSubmit)="guardar()" #f="ngForm">
        <h2>{{ editandoId() === null ? 'Nuevo estudiante' : 'Editar estudiante' }}</h2>
        <div class="fila">
          <div class="campo"><label for="doc">Documento</label>
            <input id="doc" name="documento" [(ngModel)]="form.documento" required maxlength="30" /></div>
          <div class="campo"><label for="nom">Nombre</label>
            <input id="nom" name="nombre" [(ngModel)]="form.nombre" required maxlength="120" /></div>
          <div class="campo"><label for="cur">Curso</label>
            <input id="cur" name="curso" [(ngModel)]="form.curso" required maxlength="30" /></div>
        </div>
        @if (errorForm()) { <p class="error" role="alert">{{ errorForm() }}</p> }
        <div class="fila" style="margin-top: 1rem">
          <button class="btn" type="submit" [disabled]="f.invalid">Guardar</button>
          <button class="btn btn-secundario" type="button" (click)="cerrar()">Cancelar</button>
        </div>
      </form>
    }

    <div class="tarjeta">
      <div class="fila" style="margin-bottom: 1rem">
        <div class="campo"><label for="q">Buscar por nombre o documento</label>
          <input id="q" name="q" [(ngModel)]="texto" (ngModelChange)="buscar()" placeholder="Ej: Juan o 123456" /></div>
      </div>
      @if (error()) { <p class="error" role="alert">{{ error() }}</p> }
      <div class="scroll">
        <table>
          <thead><tr><th>Documento</th><th>Nombre</th><th>Curso</th>@if (esAdmin) { <th></th> }</tr></thead>
          <tbody>
            @for (e of estudiantes(); track e.id) {
              <tr>
                <td>{{ e.documento }}</td><td>{{ e.nombre }}</td><td>{{ e.curso }}</td>
                @if (esAdmin) {
                  <td class="acciones">
                    <button type="button" class="btn btn-secundario btn-pequeno" (click)="editar(e)">Editar</button>
                    <button type="button" class="btn btn-peligro btn-pequeno" (click)="eliminar(e)">Eliminar</button>
                  </td>
                }
              </tr>
            } @empty {
              <tr><td [attr.colspan]="esAdmin ? 4 : 3" class="suave">No hay estudiantes.</td></tr>
            }
          </tbody>
        </table>
      </div>
    </div>
  `,
  styles: ['h2 { margin-top: 0; font-size: 1.1rem; }'],
})
export class EstudiantesComponent implements OnInit {
  private readonly servicio = inject(EstudianteService);
  protected readonly esAdmin = inject(AuthService).tieneRol('ADMIN');

  readonly estudiantes = signal<Estudiante[]>([]);
  readonly error = signal('');
  readonly errorForm = signal('');
  readonly formAbierto = signal(false);
  readonly editandoId = signal<number | null>(null);
  texto = '';
  form: Estudiante = { documento: '', nombre: '', curso: '' };
  private temporizador: ReturnType<typeof setTimeout> | undefined;

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.servicio.listar(this.texto).subscribe({
      next: (lista) => { this.estudiantes.set(lista); this.error.set(''); },
      error: (err) => this.error.set(mensajeError(err, 'No se pudieron cargar los estudiantes.')),
    });
  }

  /** Espera un instante tras escribir para no consultar en cada tecla. */
  buscar(): void {
    clearTimeout(this.temporizador);
    this.temporizador = setTimeout(() => this.cargar(), 250);
  }

  nuevo(): void {
    this.form = { documento: '', nombre: '', curso: '' };
    this.editandoId.set(null);
    this.errorForm.set('');
    this.formAbierto.set(true);
  }

  editar(e: Estudiante): void {
    this.form = { documento: e.documento, nombre: e.nombre, curso: e.curso };
    this.editandoId.set(e.id ?? null);
    this.errorForm.set('');
    this.formAbierto.set(true);
  }

  cerrar(): void {
    this.formAbierto.set(false);
  }

  guardar(): void {
    const id = this.editandoId();
    const peticion = id === null ? this.servicio.crear(this.form) : this.servicio.actualizar(id, this.form);
    peticion.subscribe({
      next: () => { this.cerrar(); this.cargar(); },
      error: (err) => this.errorForm.set(mensajeError(err, 'No se pudo guardar el estudiante.')),
    });
  }

  eliminar(e: Estudiante): void {
    if (e.id === undefined || !confirm(`¿Eliminar al estudiante ${e.nombre}?`)) {
      return;
    }
    this.servicio.eliminar(e.id).subscribe({
      next: () => this.cargar(),
      error: (err) => this.error.set(mensajeError(err, 'No se pudo eliminar el estudiante.')),
    });
  }
}
