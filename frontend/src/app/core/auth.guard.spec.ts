import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot, provideRouter } from '@angular/router';
import { authGuard, rolGuard } from './auth.guard';

describe('guards', () => {
  const ruta = {} as ActivatedRouteSnapshot;
  const estado = {} as RouterStateSnapshot;

  function sesion(rol: string | null): void {
    sessionStorage.clear();
    if (rol) {
      sessionStorage.setItem(
        'gestor-notas.sesion',
        JSON.stringify({ token: 't', nombre: 'N', correo: 'c', rol, estudianteId: null, expiraEn: Date.now() + 60000 })
      );
    }
    TestBed.configureTestingModule({ providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()] });
  }

  it('authGuard redirige al login sin sesión', () => {
    sesion(null);
    const r = TestBed.runInInjectionContext(() => authGuard(ruta, estado));
    expect(TestBed.inject(Router).serializeUrl(r as never)).toBe('/login');
  });

  it('authGuard deja pasar con sesión', () => {
    sesion('ADMIN');
    expect(TestBed.runInInjectionContext(() => authGuard(ruta, estado))).toBeTrue();
  });

  it('rolGuard permite el rol correcto y redirige a inicio si no', () => {
    sesion('ESTUDIANTE');
    expect(TestBed.runInInjectionContext(() => rolGuard('ESTUDIANTE')(ruta, estado))).toBeTrue();
    const r = TestBed.runInInjectionContext(() => rolGuard('ADMIN')(ruta, estado));
    expect(TestBed.inject(Router).serializeUrl(r as never)).toBe('/inicio');
  });
});
