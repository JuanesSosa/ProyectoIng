import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Estudiante, Rol, Usuario, UsuarioRequest } from '../../core/api.models';
import { mensajeError } from '../../core/errores';
import { EstudianteService } from '../../services/estudiante.service';
import { UsuarioService } from '../../services/usuario.service';

/** Gestión de usuarios (RF-03, solo administrador): crear, consultar, editar y desactivar. */
@Component({
  selector: 'app-usuarios',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="cabecera">
      <h1>Usuarios</h1>
      @if (!formAbierto()) {
        <button type="button" class="btn" (click)="nuevo()">+ Agregar</button>
      }
    </div>

    @if (formAbierto()) {
      <form class="tarjeta" (ngSubmit)="guardar()" #f="ngForm">
        <h2>{{ editandoId() === null ? 'Nuevo usuario' : 'Editar usuario' }}</h2>
        <div class="fila">
          <div class="campo"><label for="nom">Nombre</label>
            <input id="nom" name="nombre" [(ngModel)]="form.nombre" required maxlength="120" /></div>
          <div class="campo"><label for="cor">Correo</label>
            <input id="cor" name="correo" type="email" [(ngModel)]="form.correo" required /></div>
          <div class="campo"><label for="pwd">Contraseña {{ editandoId() !== null ? '(vacía = no cambiar)' : '' }}</label>
            <input id="pwd" name="password" type="password" [(ngModel)]="form.password"
                   [required]="editandoId() === null" minlength="8" maxlength="64" autocomplete="new-password" /></div>
        </div>
        <div class="fila" style="margin-top: 0.75rem">
          <div class="campo"><label for="rol">Rol</label>
            <select id="rol" name="rol" [(ngModel)]="form.rol">
              <option value="ADMIN">Administrador</option>
              <option value="DOCENTE">Docente</option>
              <option value="ESTUDIANTE">Estudiante</option>
            </select></div>
          @if (form.rol === 'ESTUDIANTE') {
            <div class="campo"><label for="est">Estudiante vinculado</label>
              <select id="est" name="estudianteId" [(ngModel)]="form.estudianteId" required>
                <option [ngValue]="null">— Selecciona —</option>
                @for (e of estudiantes(); track e.id) {
                  <option [ngValue]="e.id">{{ e.nombre }} ({{ e.documento }})</option>
                }
              </select></div>
          }
          @if (editandoId() !== null) {
            <div class="campo"><label for="act">Estado</label>
              <select id="act" name="activo" [(ngModel)]="form.activo">
                <option [ngValue]="true">Activo</option>
                <option [ngValue]="false">Inactivo</option>
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
          <thead><tr><th>Nombre</th><th>Correo</th><th>Rol</th><th>Estado</th><th></th></tr></thead>
          <tbody>
            @for (u of usuarios(); track u.id) {
              <tr>
                <td>{{ u.nombre }}</td><td>{{ u.correo }}</td><td>{{ u.rol }}</td>
                <td><span class="etiqueta" [class.inactivo]="!u.activo">{{ u.activo ? 'Activo' : 'Inactivo' }}</span></td>
                <td class="acciones">
                  <button type="button" class="btn btn-secundario btn-pequeno" (click)="editar(u)">Editar</button>
                  @if (u.activo) {
                    <button type="button" class="btn btn-peligro btn-pequeno" (click)="desactivar(u)">Desactivar</button>
                  }
                </td>
              </tr>
            } @empty {
              <tr><td colspan="5" class="suave">No hay usuarios.</td></tr>
            }
          </tbody>
        </table>
      </div>
    </div>
  `,
  styles: ['h2 { margin-top: 0; font-size: 1.1rem; }'],
})
export class UsuariosComponent implements OnInit {
  private readonly servicio = inject(UsuarioService);
  private readonly estudianteService = inject(EstudianteService);

  readonly usuarios = signal<Usuario[]>([]);
  readonly estudiantes = signal<Estudiante[]>([]);
  readonly error = signal('');
  readonly errorForm = signal('');
  readonly formAbierto = signal(false);
  readonly editandoId = signal<number | null>(null);
  form: UsuarioRequest = this.vacio();

  ngOnInit(): void {
    this.cargar();
    this.estudianteService.listar().subscribe({ next: (l) => this.estudiantes.set(l), error: () => undefined });
  }

  private vacio(): UsuarioRequest {
    return { nombre: '', correo: '', password: '', rol: 'DOCENTE' as Rol, estudianteId: null, activo: true };
  }

  cargar(): void {
    this.servicio.listar().subscribe({
      next: (lista) => { this.usuarios.set(lista); this.error.set(''); },
      error: (err) => this.error.set(mensajeError(err, 'No se pudieron cargar los usuarios.')),
    });
  }

  nuevo(): void {
    this.form = this.vacio();
    this.editandoId.set(null);
    this.errorForm.set('');
    this.formAbierto.set(true);
  }

  editar(u: Usuario): void {
    this.form = { nombre: u.nombre, correo: u.correo, password: '', rol: u.rol, estudianteId: u.estudianteId, activo: u.activo };
    this.editandoId.set(u.id);
    this.errorForm.set('');
    this.formAbierto.set(true);
  }

  cerrar(): void {
    this.formAbierto.set(false);
  }

  guardar(): void {
    const datos: UsuarioRequest = {
      ...this.form,
      password: this.form.password ? this.form.password : undefined,
      estudianteId: this.form.rol === 'ESTUDIANTE' ? this.form.estudianteId : null,
    };
    const id = this.editandoId();
    const peticion = id === null ? this.servicio.crear(datos) : this.servicio.actualizar(id, datos);
    peticion.subscribe({
      next: () => { this.cerrar(); this.cargar(); },
      error: (err) => this.errorForm.set(mensajeError(err, 'No se pudo guardar el usuario.')),
    });
  }

  desactivar(u: Usuario): void {
    if (!confirm(`¿Desactivar al usuario ${u.nombre}?`)) {
      return;
    }
    this.servicio.desactivar(u.id).subscribe({
      next: () => this.cargar(),
      error: (err) => this.error.set(mensajeError(err, 'No se pudo desactivar el usuario.')),
    });
  }
}
