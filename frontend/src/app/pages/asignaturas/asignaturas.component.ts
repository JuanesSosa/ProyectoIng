import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/auth.service';
import { Asignatura } from '../../core/api.models';
import { mensajeError } from '../../core/errores';
import { AsignaturaService } from '../../services/asignatura.service';

/** Gestión de asignaturas (RF-06). Docente: solo consulta; administrador: crear, editar, desactivar. */
@Component({
  selector: 'app-asignaturas',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="cabecera">
      <h1>Asignaturas</h1>
      @if (esAdmin && !formAbierto()) {
        <button type="button" class="btn" (click)="nueva()">+ Agregar</button>
      }
    </div>

    @if (formAbierto()) {
      <form class="tarjeta" (ngSubmit)="guardar()" #f="ngForm">
        <h2>{{ editandoId() === null ? 'Nueva asignatura' : 'Editar asignatura' }}</h2>
        <div class="fila">
          <div class="campo"><label for="cod">Código</label>
            <input id="cod" name="codigo" [(ngModel)]="form.codigo" required maxlength="20" /></div>
          <div class="campo"><label for="nom">Nombre</label>
            <input id="nom" name="nombre" [(ngModel)]="form.nombre" required maxlength="120" /></div>
          @if (editandoId() !== null) {
            <div class="campo"><label for="act">Estado</label>
              <select id="act" name="activa" [(ngModel)]="form.activa">
                <option [ngValue]="true">Activa</option>
                <option [ngValue]="false">Inactiva</option>
              </select></div>
          }
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
          <thead><tr><th>Código</th><th>Nombre</th><th>Estado</th>@if (esAdmin) { <th></th> }</tr></thead>
          <tbody>
            @for (a of asignaturas(); track a.id) {
              <tr>
                <td>{{ a.codigo }}</td><td>{{ a.nombre }}</td>
                <td><span class="etiqueta" [class.inactivo]="!a.activa">{{ a.activa ? 'Activa' : 'Inactiva' }}</span></td>
                @if (esAdmin) {
                  <td class="acciones">
                    <button type="button" class="btn btn-secundario btn-pequeno" (click)="editar(a)">Editar</button>
                    @if (a.activa) {
                      <button type="button" class="btn btn-peligro btn-pequeno" (click)="desactivar(a)">Desactivar</button>
                    }
                  </td>
                }
              </tr>
            } @empty {
              <tr><td [attr.colspan]="esAdmin ? 4 : 3" class="suave">No hay asignaturas.</td></tr>
            }
          </tbody>
        </table>
      </div>
    </div>
  `,
  styles: ['h2 { margin-top: 0; font-size: 1.1rem; }'],
})
export class AsignaturasComponent implements OnInit {
  private readonly servicio = inject(AsignaturaService);
  protected readonly esAdmin = inject(AuthService).tieneRol('ADMIN');

  readonly asignaturas = signal<Asignatura[]>([]);
  readonly error = signal('');
  readonly errorForm = signal('');
  readonly formAbierto = signal(false);
  readonly editandoId = signal<number | null>(null);
  form: Asignatura = { codigo: '', nombre: '', activa: true };

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.servicio.listar().subscribe({
      next: (lista) => { this.asignaturas.set(lista); this.error.set(''); },
      error: (err) => this.error.set(mensajeError(err, 'No se pudieron cargar las asignaturas.')),
    });
  }

  nueva(): void {
    this.form = { codigo: '', nombre: '', activa: true };
    this.editandoId.set(null);
    this.errorForm.set('');
    this.formAbierto.set(true);
  }

  editar(a: Asignatura): void {
    this.form = { codigo: a.codigo, nombre: a.nombre, activa: a.activa ?? true };
    this.editandoId.set(a.id ?? null);
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
      error: (err) => this.errorForm.set(mensajeError(err, 'No se pudo guardar la asignatura.')),
    });
  }

  desactivar(a: Asignatura): void {
    if (a.id === undefined || !confirm(`¿Desactivar la asignatura ${a.nombre}?`)) {
      return;
    }
    this.servicio.desactivar(a.id).subscribe({
      next: () => this.cargar(),
      error: (err) => this.error.set(mensajeError(err, 'No se pudo desactivar la asignatura.')),
    });
  }
}
