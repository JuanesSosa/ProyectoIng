import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { GruposListaComponent } from './grupos-lista.component';
import { environment } from '../../../environments/environment';

describe('GruposListaComponent', () => {
  function sesion(rol: string): void {
    sessionStorage.setItem(
      'gestor-notas.sesion',
      JSON.stringify({ token: 't', nombre: 'N', correo: 'c', rol, estudianteId: null, expiraEn: Date.now() + 60000 })
    );
  }

  beforeEach(() => {
    sessionStorage.clear();
  });

  it('agrupa los grupos por asignatura y enlaza al detalle del docente', () => {
    sesion('DOCENTE');
    TestBed.configureTestingModule({ providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()] });
    const fixture = TestBed.createComponent(GruposListaComponent);
    const http = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
    http.expectOne(`${environment.apiUrl}/grupos`).flush([
      { id: 1, asignaturaId: 10, asignaturaNombre: 'Ingeniería de Software', nombre: 'Grupo 1', periodo: '2026-2', docenteId: 5, estudiantes: 2 },
      { id: 2, asignaturaId: 10, asignaturaNombre: 'Ingeniería de Software', nombre: 'Grupo 2', periodo: '2026-2', docenteId: 5, estudiantes: 1 },
      { id: 3, asignaturaId: 11, asignaturaNombre: 'Cálculo I', nombre: 'Grupo 1', periodo: '2026-2', docenteId: 5, estudiantes: 0 },
    ]);
    fixture.detectChanges();

    const el: HTMLElement = fixture.nativeElement;
    expect(el.querySelectorAll('h2').length).toBe(2);
    const enlaces = Array.from(el.querySelectorAll('a.grupo')).map((a) => a.getAttribute('href'));
    expect(enlaces).toEqual(['/notas/grupo/1', '/notas/grupo/2', '/notas/grupo/3']);
  });

  it('para el estudiante enlaza a /mis-notas', () => {
    sesion('ESTUDIANTE');
    TestBed.configureTestingModule({ providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()] });
    const fixture = TestBed.createComponent(GruposListaComponent);
    const http = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
    http.expectOne(`${environment.apiUrl}/grupos`).flush([
      { id: 7, asignaturaId: 10, asignaturaNombre: 'Cálculo I', nombre: 'Grupo 1', periodo: '2026-2', docenteId: 5, estudiantes: 3 },
    ]);
    fixture.detectChanges();

    const href = fixture.nativeElement.querySelector('a.grupo').getAttribute('href');
    expect(href).toBe('/mis-notas/grupo/7');
  });
});
