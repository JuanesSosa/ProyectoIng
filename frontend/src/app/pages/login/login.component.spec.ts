import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router, provideRouter } from '@angular/router';
import { LoginComponent } from './login.component';
import { environment } from '../../../environments/environment';

describe('LoginComponent', () => {
  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
  });

  it('muestra el error del backend con credenciales incorrectas', () => {
    const fixture = TestBed.createComponent(LoginComponent);
    const http = TestBed.inject(HttpTestingController);
    fixture.componentInstance.correo = 'x@x.com';
    fixture.componentInstance.password = 'mala';
    fixture.componentInstance.entrar();
    http.expectOne(`${environment.apiUrl}/auth/login`).flush(
      { message: 'Correo o contraseña incorrectos' }, { status: 401, statusText: 'Unauthorized' });
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Correo o contraseña incorrectos');
  });

  it('navega a inicio al ingresar bien', () => {
    const fixture = TestBed.createComponent(LoginComponent);
    const http = TestBed.inject(HttpTestingController);
    const navegar = spyOn(TestBed.inject(Router), 'navigate').and.resolveTo(true);
    fixture.componentInstance.correo = 'a@a.com';
    fixture.componentInstance.password = 'x';
    fixture.componentInstance.entrar();
    http.expectOne(`${environment.apiUrl}/auth/login`).flush({
      token: 't', tipo: 'Bearer', expiraEnSegundos: 60, nombre: 'A', correo: 'a@a.com', rol: 'ADMIN', estudianteId: null,
    });
    expect(navegar).toHaveBeenCalledWith(['/inicio']);
  });
});
