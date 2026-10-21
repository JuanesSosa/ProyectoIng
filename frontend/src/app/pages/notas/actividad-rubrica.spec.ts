import { validarCalificaciones, validarRubrica } from './actividad-rubrica.component';

describe('validarCalificaciones', () => {
  const f = (valor: number | null) => ({ criterioId: 1, nombre: 'Teoría', peso: 50, valor });

  it('acepta notas vacías y notas entre 0.0 y 5.0', () => {
    expect(validarCalificaciones([f(null), f(0), f(5), f(4.25)])).toBe('');
  });
  it('rechaza notas fuera de rango o con más de 2 decimales', () => {
    expect(validarCalificaciones([f(5.1)])).toContain('entre 0.0 y 5.0');
    expect(validarCalificaciones([f(-0.5)])).toContain('entre 0.0 y 5.0');
    expect(validarCalificaciones([f(4.123)])).toContain('2 decimales');
  });
});

describe('validarRubrica', () => {
  it('acepta una rúbrica que suma 100 o menos', () => {
    expect(validarRubrica([{ nombre: 'Teoría', peso: 50 }, { nombre: 'Práctica', peso: 30 }, { nombre: 'Sustentación', peso: 20 }])).toBe('');
    expect(validarRubrica([{ nombre: 'Teoría', peso: 60 }])).toBe('');
  });
  it('rechaza rúbricas vacías, sin nombre, repetidas o con porcentajes inválidos', () => {
    expect(validarRubrica([])).toContain('al menos un criterio');
    expect(validarRubrica([{ nombre: ' ', peso: 10 }])).toContain('nombre');
    expect(validarRubrica([{ nombre: 'A', peso: 10 }, { nombre: ' a ', peso: 10 }])).toContain('repetido');
    expect(validarRubrica([{ nombre: 'A', peso: null }])).toContain('entero entre 1 y 100');
    expect(validarRubrica([{ nombre: 'A', peso: 0 }])).toContain('entero entre 1 y 100');
  });
  it('rechaza porcentajes que suman más de 100', () => {
    expect(validarRubrica([{ nombre: 'A', peso: 60 }, { nombre: 'B', peso: 50 }])).toContain('más de 100');
  });
});
