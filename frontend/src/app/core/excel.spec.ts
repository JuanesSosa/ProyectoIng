import { Criterio, EstudianteActividad, ResumenGrupo } from './api.models';
import { encabezadoCriterio, filasPlantilla, hojasNotasGrupo, interpretarPlantilla, leerExcel, nombreArchivo } from './excel';

const criterios: Criterio[] = [
  { id: 10, nombre: 'Teoría', peso: 60 },
  { id: 11, nombre: 'Práctica', peso: 40 },
];
const estudiantes: EstudianteActividad[] = [
  { estudianteId: 1, documento: '123456', codigo: '2220242001', nombre: 'Juan Pérez', correo: 'j@x.com', nota: null, avance: 0 },
  { estudianteId: 2, documento: '456789', codigo: null, nombre: 'Ana López', correo: null, nota: null, avance: 0 },
];
const cab = ['Documento', 'Código', 'Nombre', encabezadoCriterio(criterios[0]), encabezadoCriterio(criterios[1])];

describe('excel: plantilla de notas', () => {
  it('genera encabezados y una fila por estudiante', () => {
    const filas = filasPlantilla(criterios, estudiantes);
    expect(filas[0]).toEqual(cab);
    expect(filas.length).toBe(3);
    expect(filas[1]).toEqual(['123456', '2220242001', 'Juan Pérez', null, null]);
  });

  it('interpreta notas, acepta coma decimal y deja las celdas vacías sin cambiar', () => {
    const r = interpretarPlantilla(
      [cab, ['123456', '', 'x', '4,5', null], ['456789', '', 'y', 3, 2.75]],
      criterios,
      estudiantes,
    );
    expect(r.errores).toEqual([]);
    expect(r.notas).toEqual([
      { estudianteId: 1, nombre: 'Juan Pérez', calificaciones: [{ criterioId: 10, valor: 4.5 }] },
      { estudianteId: 2, nombre: 'Ana López', calificaciones: [{ criterioId: 10, valor: 3 }, { criterioId: 11, valor: 2.75 }] },
    ]);
  });

  it('busca por código si no hay documento', () => {
    const r = interpretarPlantilla([cab, ['', '2220242001', '', 5, 5]], criterios, estudiantes);
    expect(r.notas[0].estudianteId).toBe(1);
  });

  it('no importa nada si hay notas inválidas o estudiantes desconocidos', () => {
    const r = interpretarPlantilla(
      [cab, ['123456', '', '', 5.5, 'abc'], ['000', '', '', 3, 3], ['456789', '', '', 4.123, null]],
      criterios,
      estudiantes,
    );
    expect(r.notas).toEqual([]);
    expect(r.errores.length).toBe(4);
    expect(r.errores.join(' ')).toContain('Fila 2');
    expect(r.errores.join(' ')).toContain('Fila 3');
    expect(r.errores.join(' ')).toContain('2 decimales');
  });

  it('avisa si la rúbrica cambió, si el archivo no es la plantilla o está sin notas', () => {
    expect(interpretarPlantilla([['Documento', 'Teoría (50%)'], ['123456', 4]], criterios, estudiantes).errores[0]).toContain(
      'No se encontró la columna',
    );
    expect(interpretarPlantilla([['A', 'B']], criterios, estudiantes).errores[0]).toContain('Documento');
    expect(interpretarPlantilla([cab, ['123456', '', '', null, null]], criterios, estudiantes).errores[0]).toContain('vacías');
    expect(interpretarPlantilla([], criterios, estudiantes).errores[0]).toContain('vacío');
  });

  it('rechaza estudiantes repetidos', () => {
    const r = interpretarPlantilla([cab, ['123456', '', '', 4, 4], ['123456', '', '', 3, 3]], criterios, estudiantes);
    expect(r.errores[0]).toContain('repetido');
  });
});

describe('excel: exportar notas del grupo', () => {
  const resumen = {
    estudianteId: 1, estudianteDocumento: '123456', estudianteCodigo: '2220242001', estudianteNombre: 'Juan Pérez',
    estudianteCorreo: 'j@x.com', promedio: 4.5, acumulado: 1.35, avance: 30,
    actividades: [
      { actividadId: 1, nombre: 'Parcial 1', peso: 30, nota: 4.5, avance: 100,
        criterios: [{ criterioId: 10, nombre: 'Teoría', peso: 60, valor: 4.5 }] },
    ],
  } as unknown as ResumenGrupo;

  it('arma la hoja Notas y la hoja Detalle', () => {
    const [notas, detalle] = hojasNotasGrupo([resumen]);
    expect(notas.filas[0]).toEqual([
      'Código', 'Documento', 'Nombre', 'Correo', 'Parcial 1 (30%)', 'Avance (%)', 'Acumulado (sobre 5.0)', 'Promedio de lo evaluado',
    ]);
    expect(notas.filas[1]).toEqual(['2220242001', '123456', 'Juan Pérez', 'j@x.com', 4.5, 30, 1.35, 4.5]);
    expect(detalle.filas[1]).toEqual(['2220242001', 'Juan Pérez', 'Parcial 1', 'Teoría', 60, 4.5]);
  });

  it('funciona con un grupo sin estudiantes', () => {
    expect(hojasNotasGrupo([])[0].filas.length).toBe(1);
  });

  it('crea nombres de archivo seguros', () => {
    expect(nombreArchivo('Ingeniería de Software', 'Grupo 1')).toBe('Ingenieria-de-Software-Grupo-1.xlsx');
  });
});

describe('excel: archivo .xlsx real', () => {
  it('escribe un libro con ExcelJS y la plantilla lo lee de vuelta', async () => {
    const ExcelJS = await import('exceljs');
    const libro = new ExcelJS.Workbook();
    const hoja = libro.addWorksheet('Notas');
    filasPlantilla(criterios, estudiantes).forEach((f) => hoja.addRow(f));
    hoja.getCell('D2').value = 4.5; // Juan, Teoría
    hoja.getCell('E3').value = '3,25'; // Ana, Práctica (texto con coma)
    const datos = await libro.xlsx.writeBuffer();
    const archivo = new File([datos], 'plantilla.xlsx');

    const filas = await leerExcel(archivo);
    const r = interpretarPlantilla(filas, criterios, estudiantes);

    expect(r.errores).toEqual([]);
    expect(r.notas).toEqual([
      { estudianteId: 1, nombre: 'Juan Pérez', calificaciones: [{ criterioId: 10, valor: 4.5 }] },
      { estudianteId: 2, nombre: 'Ana López', calificaciones: [{ criterioId: 11, valor: 3.25 }] },
    ]);
  });
});
