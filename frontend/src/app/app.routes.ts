import { Routes } from '@angular/router';
import { authGuard, rolGuard } from './core/auth.guard';

/**
 * Rutas de la aplicación. Todo lo que está dentro del shell exige sesión iniciada;
 * además cada pantalla restringe los roles que pueden entrar (RNF-01).
 */
export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./pages/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: '',
    loadComponent: () => import('./layout/shell.component').then((m) => m.ShellComponent),
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'inicio' },
      {
        path: 'inicio',
        loadComponent: () => import('./pages/inicio/inicio.component').then((m) => m.InicioComponent),
      },
      {
        path: 'estudiantes',
        canActivate: [rolGuard('ADMIN', 'DOCENTE')],
        loadComponent: () =>
          import('./pages/estudiantes/estudiantes.component').then((m) => m.EstudiantesComponent),
      },
      {
        path: 'asignaturas',
        canActivate: [rolGuard('ADMIN', 'DOCENTE')],
        loadComponent: () =>
          import('./pages/asignaturas/asignaturas.component').then((m) => m.AsignaturasComponent),
      },
      {
        path: 'usuarios',
        canActivate: [rolGuard('ADMIN')],
        loadComponent: () =>
          import('./pages/usuarios/usuarios.component').then((m) => m.UsuariosComponent),
      },
      {
        path: 'notas/registro',
        canActivate: [rolGuard('DOCENTE')],
        loadComponent: () =>
          import('./pages/registro-notas/registro-notas.component').then((m) => m.RegistroNotasComponent),
      },
      {
        path: 'notas/consulta',
        loadComponent: () =>
          import('./pages/consulta-notas/consulta-notas.component').then((m) => m.ConsultaNotasComponent),
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
