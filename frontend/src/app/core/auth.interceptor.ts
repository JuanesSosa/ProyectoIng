import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthService } from './auth.service';

/**
 * Interceptor HTTP:
 * - Añade "Authorization: Bearer <token>" a las peticiones hacia la API.
 * - Si el backend responde 401 en una petición autenticada (token vencido o inválido), cierra la sesión.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const esApi = req.url.startsWith(environment.apiUrl);
  const esLogin = req.url.endsWith('/auth/login');
  const token = auth.token();

  const peticion =
    esApi && !esLogin && token
      ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
      : req;

  return next(peticion).pipe(
    catchError((err: unknown) => {
      if (esApi && !esLogin && err instanceof HttpErrorResponse && err.status === 401) {
        auth.logout();
      }
      return throwError(() => err);
    })
  );
};
