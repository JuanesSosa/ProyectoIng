import { validarFilas, FilaNota } from './registro-notas.component';

describe('validarFilas', () => {
  const f = (evaluacion: string, peso: number | null, valor: number | null): FilaNota => ({ evaluacion, peso, valor });

  it('acepta pesos que suman 100', () => {
    expect(validarFilas([f('Parcial', 30, 4.2), f('Taller', 30, 3.8), f('Final', 40, 4.5)])).toBe('');
  });
  it('acepta notas sin pesos', () => {
    expect(validarFilas([f('N1', null, 4), f('N2', null, 5)])).toBe('');
  });
  it('rechaza notas fuera de 0–5', () => {
    expect(validarFilas([f('N1', null, 5.1)])).toContain('entre 0.0 y 5.0');
    expect(validarFilas([f('N1', null, -1)])).toContain('entre 0.0 y 5.0');
  });
  it('rechaza más de 2 decimales', () => {
    expect(validarFilas([f('N1', null, 4.123)])).toContain('2 decimales');
  });
  it('rechaza pesos solo en algunas filas', () => {
    expect(validarFilas([f('A', 50, 4), f('B', null, 4)])).toContain('todas las evaluaciones o en ninguna');
  });
  it('rechaza pesos que suman más de 100', () => {
    expect(validarFilas([f('A', 60, 4), f('B', 50, 4)])).toContain('más de 100');
  });
  it('rechaza nombres repetidos y filas vacías', () => {
    expect(validarFilas([f('A', null, 4), f(' a ', null, 3)])).toContain('repetida');
    expect(validarFilas([])).toContain('al menos una');
  });
  it('exige la nota', () => {
    expect(validarFilas([f('A', null, null)])).toContain('Escribe la nota');
  });
});
