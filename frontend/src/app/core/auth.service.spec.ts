import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { AuthService } from './auth.service';
import { environment } from '../../environments/environment';

describe('AuthService', () => {
  let auth: AuthService;
  let http: HttpTestingController;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    auth = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('guarda la sesión al iniciar sesión', () => {
    auth.login('docente@gestor.com', 'x').subscribe();
    http.expectOne(`${environment.apiUrl}/auth/login`).flush({
      token: 't', tipo: 'Bearer', expiraEnSegundos: 3600, nombre: 'Doc', correo: 'docente@gestor.com',
      rol: 'DOCENTE', estudianteId: null,
    });
    expect(auth.estaAutenticado()).toBeTrue();
    expect(auth.token()).toBe('t');
    expect(auth.tieneRol('DOCENTE')).toBeTrue();
    expect(auth.tieneRol('ADMIN')).toBeFalse();
  });

  it('cerrar sesión descarta el token', () => {
    auth.login('a@a.com', 'x').subscribe();
    http.expectOne(`${environment.apiUrl}/auth/login`).flush({
      token: 't', tipo: 'Bearer', expiraEnSegundos: 3600, nombre: 'A', correo: 'a@a.com', rol: 'ADMIN', estudianteId: null,
    });
    auth.logout();
    expect(auth.estaAutenticado()).toBeFalse();
    expect(auth.token()).toBeNull();
  });
});
