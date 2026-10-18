import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { mensajeError } from '../../core/errores';

/** Pantalla de inicio de sesión (RF-01, mock-up 5.1). */
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="pantalla">
      <form class="tarjeta caja" (ngSubmit)="entrar()" #f="ngForm">
        <h1>Gestor de Notas</h1>
        <p class="suave">Ingresa con tu correo y contraseña.</p>
        <div class="campo">
          <label for="correo">Correo</label>
          <input id="correo" name="correo" type="email" [(ngModel)]="correo" required autocomplete="username" />
        </div>
        <div class="campo" style="margin-top: 0.75rem">
          <label for="password">Contraseña</label>
          <input id="password" name="password" type="password" [(ngModel)]="password" required autocomplete="current-password" />
        </div>
        @if (error()) {
          <p class="error" role="alert">{{ error() }}</p>
        }
        <button class="btn" type="submit" style="margin-top: 1rem; width: 100%" [disabled]="f.invalid || cargando()">
          {{ cargando() ? 'Ingresando...' : 'Ingresar' }}
        </button>
      </form>
    </div>
  `,
  styles: [
    `
      .pantalla { min-height: 100vh; display: grid; place-items: center; padding: 1rem; }
      .caja { width: 100%; max-width: 380px; }
    `,
  ],
})
export class LoginComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  correo = '';
  password = '';
  readonly error = signal('');
  readonly cargando = signal(false);

  entrar(): void {
    if (!this.correo || !this.password) {
      return;
    }
    this.error.set('');
    this.cargando.set(true);
    this.auth.login(this.correo.trim(), this.password).subscribe({
      next: () => {
        this.cargando.set(false);
        void this.router.navigate(['/inicio']);
      },
      error: (err) => {
        this.cargando.set(false);
        this.error.set(mensajeError(err, 'No se pudo iniciar sesión.'));
      },
    });
  }
}
