import { avanceRubrica, calcularPromedio, pesoTotal, promedioGeneral } from './promedio';

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

describe('rúbrica', () => {
  const completa = [
    { valor: 5.0, peso: 50 },
    { valor: 4.0, peso: 30 },
    { valor: 4.5, peso: 20 },
  ];

  it('la rúbrica del boceto (5.0/4.0/4.5 con 50/30/20) da 4.60 y avance 100', () => {
    expect(calcularPromedio(completa)).toBe(4.6);
    expect(avanceRubrica(completa)).toBe(100);
  });

  it('con solo la teoría calificada el promedio es 5.0 y el avance 50', () => {
    const parcial = [
      { valor: 5.0, peso: 50 },
      { valor: null, peso: 30 },
      { valor: null, peso: 20 },
    ];
    expect(calcularPromedio(parcial)).toBe(5);
    expect(avanceRubrica(parcial)).toBe(50);
  });

  it('sin notas: promedio nulo y avance 0', () => {
    const vacia = [{ valor: null, peso: 100 }];
    expect(calcularPromedio(vacia)).toBeNull();
    expect(avanceRubrica(vacia)).toBe(0);
    expect(avanceRubrica([])).toBe(0);
  });
});
