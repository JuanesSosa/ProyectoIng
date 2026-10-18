import { HttpErrorResponse } from '@angular/common/http';

/**
 * Convierte un error HTTP en un mensaje legible para el usuario.
 * Usa lo que responde el backend: "errors" (por campo, en validaciones) o "message".
 */
export function mensajeError(err: unknown, porDefecto: string): string {
  if (!(err instanceof HttpErrorResponse)) {
    return porDefecto;
  }
  if (err.status === 0) {
    return 'No se pudo conectar con el servidor. Revisa que el backend esté encendido.';
  }
  const cuerpo = err.error as { message?: string; errors?: Record<string, string> } | null;
  if (cuerpo && typeof cuerpo === 'object') {
    if (cuerpo.errors && Object.keys(cuerpo.errors).length > 0) {
      return Object.values(cuerpo.errors).join(' ');
    }
    if (cuerpo.message) {
      return cuerpo.message;
    }
  }
  if (err.status === 403) {
    return 'No tienes permiso para realizar esta acción.';
  }
  return porDefecto;
}
