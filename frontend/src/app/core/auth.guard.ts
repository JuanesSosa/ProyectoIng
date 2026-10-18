import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Rol } from './api.models';
import { AuthService } from './auth.service';

/** Exige sesión iniciada; si no, redirige al login. */
export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.estaAutenticado() ? true : router.createUrlTree(['/login']);
};

/** Exige que el usuario tenga uno de los roles indicados; si no, lo manda al inicio. */
export const rolGuard = (...roles: Rol[]): CanActivateFn => () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (!auth.estaAutenticado()) {
    return router.createUrlTree(['/login']);
  }
  return auth.tieneRol(...roles) ? true : router.createUrlTree(['/inicio']);
};
