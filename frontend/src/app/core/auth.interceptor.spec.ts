import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { AuthService } from './auth.service';
import { authInterceptor } from './auth.interceptor';
import { environment } from '../../environments/environment';

describe('authInterceptor', () => {
  let http: HttpClient;
  let controller: HttpTestingController;
  let auth: AuthService;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    http = TestBed.inject(HttpClient);
    controller = TestBed.inject(HttpTestingController);
    auth = TestBed.inject(AuthService);
    auth.login('a@a.com', 'x').subscribe();
    controller.expectOne(`${environment.apiUrl}/auth/login`).flush({
      token: 'tok', tipo: 'Bearer', expiraEnSegundos: 3600, nombre: 'A', correo: 'a@a.com', rol: 'ADMIN', estudianteId: null,
    });
  });

  afterEach(() => controller.verify());

  it('agrega el token a las peticiones a la API', () => {
    http.get(`${environment.apiUrl}/estudiantes`).subscribe();
    const req = controller.expectOne(`${environment.apiUrl}/estudiantes`);
    expect(req.request.headers.get('Authorization')).toBe('Bearer tok');
    req.flush([]);
  });

  it('no agrega el token a otras direcciones', () => {
    http.get('https://otro.sitio/datos').subscribe();
    const req = controller.expectOne('https://otro.sitio/datos');
    expect(req.request.headers.has('Authorization')).toBeFalse();
    req.flush({});
  });

  it('cierra la sesión si la API responde 401', () => {
    http.get(`${environment.apiUrl}/estudiantes`).subscribe({ error: () => undefined });
    controller.expectOne(`${environment.apiUrl}/estudiantes`).flush({}, { status: 401, statusText: 'Unauthorized' });
    expect(auth.estaAutenticado()).toBeFalse();
  });
});
