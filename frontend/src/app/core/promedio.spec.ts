import { calcularPromedio, pesoTotal, promedioGeneral } from './promedio';

describe('promedio', () => {
  it('calcula el promedio ponderado del mock-up 5.4 (30/30/40) = 4.20', () => {
    const items = [
      { valor: 4.2, peso: 30 },
      { valor: 3.8, peso: 30 },
      { valor: 4.5, peso: 40 },
    ];
    expect(calcularPromedio(items)).toBe(4.2);
    expect(pesoTotal(items)).toBe(100);
  });

  it('usa promedio simple cuando no hay pesos', () => {
    expect(calcularPromedio([
      { valor: 4.0, peso: null },
      { valor: 4.3, peso: null },
      { valor: 4.1, peso: null },
    ])).toBe(4.13);
  });

  it('con pesos incompletos promedia lo evaluado', () => {
    expect(calcularPromedio([{ valor: 4.0, peso: 30 }, { valor: 5.0, peso: 30 }])).toBe(4.5);
  });

  it('si falta algún peso usa promedio simple', () => {
    expect(calcularPromedio([{ valor: 4.0, peso: 30 }, { valor: 5.0, peso: null }])).toBe(4.5);
  });

  it('devuelve null sin notas y no cuenta las evaluaciones sin nota', () => {
    expect(calcularPromedio([])).toBeNull();
    expect(calcularPromedio([{ valor: null, peso: 50 }])).toBeNull();
    expect(calcularPromedio([{ valor: 4.0, peso: 50 }, { valor: null, peso: 50 }])).toBe(4);
  });

  it('redondea la mitad hacia arriba (3.33 y 3.34 -> 3.34)', () => {
    expect(calcularPromedio([{ valor: 3.33, peso: 50 }, { valor: 3.34, peso: 50 }])).toBe(3.34);
  });

  it('calcula el promedio general', () => {
    expect(promedioGeneral([4.17, 4.13, 4.6])).toBe(4.3);
    expect(promedioGeneral([null, 4.0])).toBe(4);
    expect(promedioGeneral([])).toBeNull();
  });
});
