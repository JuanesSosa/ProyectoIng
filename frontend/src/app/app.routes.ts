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
        path: 'grupos',
        canActivate: [rolGuard('ADMIN')],
        loadComponent: () => import('./pages/grupos/grupos.component').then((m) => m.GruposComponent),
      },
      {
        path: 'notas',
        canActivate: [rolGuard('ADMIN', 'DOCENTE')],
        loadComponent: () =>
          import('./pages/notas/grupos-lista.component').then((m) => m.GruposListaComponent),
      },
      {
        path: 'notas/grupo/:id',
        canActivate: [rolGuard('ADMIN', 'DOCENTE')],
        loadComponent: () =>
          import('./pages/notas/grupo-actividades.component').then((m) => m.GrupoActividadesComponent),
      },
      {
        path: 'notas/actividad/:id',
        canActivate: [rolGuard('ADMIN', 'DOCENTE')],
        loadComponent: () =>
          import('./pages/notas/actividad-rubrica.component').then((m) => m.ActividadRubricaComponent),
      },
      {
        path: 'mis-notas',
        canActivate: [rolGuard('ESTUDIANTE')],
        loadComponent: () =>
          import('./pages/notas/grupos-lista.component').then((m) => m.GruposListaComponent),
      },
      {
        path: 'mis-notas/grupo/:id',
        canActivate: [rolGuard('ESTUDIANTE')],
        loadComponent: () =>
          import('./pages/mis-notas/mis-notas-grupo.component').then((m) => m.MisNotasGrupoComponent),
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
