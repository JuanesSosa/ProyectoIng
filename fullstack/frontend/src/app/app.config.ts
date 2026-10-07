import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';

import { routes } from './app.routes';

/**
 * Configuración global de la aplicación Angular.
 * - provideZoneChangeDetection: detección de cambios (zonas).
 * - provideRouter: rutas definidas en app.routes.
 * - provideHttpClient: necesario para que ProductService pueda hacer peticiones HTTP al backend.
 */
export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient()
  ]
};
