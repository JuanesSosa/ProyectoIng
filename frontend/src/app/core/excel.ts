import { Criterio, EstudianteActividad, ResumenGrupo } from './api.models';
import { guardarBlob } from './archivos';

/** Una celda de las hojas que se exportan o se leen. */
export type Celda = string | number | null;

export interface Hoja {
  nombre: string;
  filas: Celda[][];
}

/** Calificaciones leídas de la plantilla para un estudiante (solo los criterios con nota). */
export interface NotasImportadas {
  estudianteId: number;
  nombre: string;
  calificaciones: { criterioId: number; valor: number }[];
}

export interface ResultadoImportacion {
  notas: NotasImportadas[];
  errores: string[];
}

const ENCABEZADOS_BASE = ['Documento', 'Código', 'Nombre'];

// ---------- Exportar notas del grupo ----------

/** Hojas "Notas" (una fila por estudiante) y "Detalle" (una fila por estudiante, actividad y criterio). */
export function hojasNotasGrupo(resumenes: ResumenGrupo[]): Hoja[] {
  const actividades = resumenes[0]?.actividades ?? [];
  const notas: Celda[][] = [
    [
      'Código', 'Documento', 'Nombre', 'Correo',
      ...actividades.map((a) => `${a.nombre} (${a.peso}%)`),
      'Avance (%)', 'Acumulado (sobre 5.0)', 'Promedio de lo evaluado',
    ],
    ...resumenes.map((r) => [
      r.estudianteCodigo, r.estudianteDocumento, r.estudianteNombre, r.estudianteCorreo,
      ...r.actividades.map((a) => a.nota),
      r.avance, r.acumulado, r.promedio,
    ]),
  ];
  const detalle: Celda[][] = [['Código', 'Nombre', 'Actividad', 'Criterio', 'Peso del criterio (%)', 'Nota']];
  for (const r of resumenes) {
    for (const a of r.actividades) {
      for (const c of a.criterios) {
        detalle.push([r.estudianteCodigo, r.estudianteNombre, a.nombre, c.nombre, c.peso, c.valor]);
      }
    }
  }
  return [{ nombre: 'Notas', filas: notas }, { nombre: 'Detalle', filas: detalle }];
}

// ---------- Plantilla para importar notas de una actividad ----------

export function encabezadoCriterio(c: { nombre: string; peso: number }): string {
  return `${c.nombre} (${c.peso}%)`;
}

/** Plantilla: una fila por estudiante y una columna por criterio de la rúbrica, vacías para llenar. */
export function filasPlantilla(criterios: Criterio[], estudiantes: EstudianteActividad[]): Celda[][] {
  return [
    [...ENCABEZADOS_BASE, ...criterios.map(encabezadoCriterio)],
    ...estudiantes.map((e) => [e.documento, e.codigo, e.nombre, ...criterios.map(() => null)]),
  ];
}

/** Texto de una celda leída: números sin ".0" de más y espacios recortados. */
function texto(celda: unknown): string {
  return celda === null || celda === undefined ? '' : String(celda).trim();
}

/** Convierte una celda de nota a número (acepta coma decimal). Devuelve null si está vacía; NaN si no es número. */
function aNota(celda: unknown): number | null {
  const t = texto(celda).replace(',', '.');
  if (t === '') {
    return null;
  }
  return /^-?\d+(\.\d+)?$/.test(t) ? Number(t) : Number.NaN;
}

/**
 * Interpreta la plantilla llenada. Los estudiantes se buscan por documento (o, si falta, por código).
 * Una celda vacía significa "no cambiar esa nota". Si hay cualquier error no se importa nada.
 */
export function interpretarPlantilla(
  filas: unknown[][],
  criterios: Criterio[],
  estudiantes: EstudianteActividad[],
): ResultadoImportacion {
  const errores: string[] = [];
  const encabezado = (filas[0] ?? []).map(texto);
  if (encabezado.length === 0) {
    return { notas: [], errores: ['El archivo está vacío.'] };
  }
  const columnaDocumento = encabezado.indexOf('Documento');
  const columnaCodigo = encabezado.indexOf('Código');
  if (columnaDocumento < 0 && columnaCodigo < 0) {
    errores.push('Faltan las columnas "Documento" o "Código". Usa la plantilla descargada desde esta pantalla.');
  }
  const columnas = criterios.map((c) => ({ criterio: c, columna: encabezado.indexOf(encabezadoCriterio(c)) }));
  for (const { criterio, columna } of columnas) {
    if (columna < 0) {
      errores.push(
        `No se encontró la columna "${encabezadoCriterio(criterio)}". ¿Cambió la rúbrica? Descarga la plantilla otra vez.`,
      );
    }
  }
  if (errores.length > 0) {
    return { notas: [], errores };
  }

  const notas: NotasImportadas[] = [];
  const vistos = new Set<number>();
  for (let i = 1; i < filas.length; i++) {
    const fila = filas[i] ?? [];
    const numeroFila = i + 1;
    if (fila.every((c) => texto(c) === '')) {
      continue;
    }
    const documento = columnaDocumento >= 0 ? texto(fila[columnaDocumento]) : '';
    const codigo = columnaCodigo >= 0 ? texto(fila[columnaCodigo]) : '';
    const estudiante = estudiantes.find((e) =>
      documento !== '' ? e.documento === documento : codigo !== '' && e.codigo === codigo,
    );
    if (!estudiante) {
      errores.push(`Fila ${numeroFila}: no hay un estudiante del grupo con documento "${documento}" ni código "${codigo}".`);
      continue;
    }
    if (vistos.has(estudiante.estudianteId)) {
      errores.push(`Fila ${numeroFila}: ${estudiante.nombre} aparece repetido.`);
      continue;
    }
    vistos.add(estudiante.estudianteId);

    const calificaciones: { criterioId: number; valor: number }[] = [];
    for (const { criterio, columna } of columnas) {
      const valor = aNota(fila[columna]);
      if (valor === null) {
        continue;
      }
      if (Number.isNaN(valor) || valor < 0 || valor > 5) {
        errores.push(`Fila ${numeroFila}, "${criterio.nombre}": la nota debe estar entre 0.0 y 5.0.`);
      } else if (Math.round(valor * 100) / 100 !== valor) {
        errores.push(`Fila ${numeroFila}, "${criterio.nombre}": la nota admite máximo 2 decimales.`);
      } else {
        calificaciones.push({ criterioId: criterio.id, valor });
      }
    }
    if (calificaciones.length > 0) {
      notas.push({ estudianteId: estudiante.estudianteId, nombre: estudiante.nombre, calificaciones });
    }
  }
  if (errores.length === 0 && notas.length === 0) {
    errores.push('No hay notas para importar: todas las celdas de nota están vacías.');
  }
  return errores.length > 0 ? { notas: [], errores } : { notas, errores };
}

/** Nombre de archivo seguro: sin tildes ni símbolos. */
export function nombreArchivo(...partes: string[]): string {
  const base = partes
    .join('-')
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
    .replace(/[^A-Za-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '');
  return `${base || 'notas'}.xlsx`;
}

// ---------- Leer y escribir archivos .xlsx (ExcelJS se carga solo cuando se usa) ----------

/**
 * Carga ExcelJS solo cuando se necesita. ExcelJS es CommonJS: según el modo de compilación (desarrollo o
 * producción) el constructor llega en el módulo o dentro de `default`, así que se aceptan ambos.
 */
async function cargarExcelJS(): Promise<typeof import('exceljs')> {
  const modulo = (await import('exceljs')) as unknown as { default?: typeof import('exceljs') } & typeof import('exceljs');
  return modulo.Workbook ? modulo : (modulo.default as typeof import('exceljs'));
}

export async function descargarExcel(archivo: string, hojas: Hoja[]): Promise<void> {
  const ExcelJS = await cargarExcelJS();
  const libro = new ExcelJS.Workbook();
  for (const hoja of hojas) {
    const ws = libro.addWorksheet(hoja.nombre);
    hoja.filas.forEach((fila) => ws.addRow(fila));
    ws.getRow(1).font = { bold: true };
    ws.columns.forEach((columna) => {
      let ancho = 10;
      columna.eachCell?.({ includeEmpty: false }, (celda) => {
        ancho = Math.max(ancho, Math.min(40, String(celda.value ?? '').length + 2));
      });
      columna.width = ancho;
    });
  }
  const datos = await libro.xlsx.writeBuffer();
  guardarBlob(
    new Blob([datos], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' }),
    archivo,
  );
}

/** Valor simple de una celda de ExcelJS (resultado de fórmula, texto enriquecido, etc.). */
function valorCelda(valor: unknown): Celda {
  if (valor === null || valor === undefined) {
    return null;
  }
  if (typeof valor === 'number' || typeof valor === 'string') {
    return valor;
  }
  if (typeof valor === 'object') {
    const v = valor as { result?: unknown; richText?: { text: string }[]; text?: string };
    if (v.richText) {
      return v.richText.map((t) => t.text).join('');
    }
    if (v.result !== undefined) {
      return valorCelda(v.result);
    }
    if (v.text !== undefined) {
      return v.text;
    }
  }
  return String(valor);
}

/** Lee la primera hoja de un .xlsx como filas de celdas. */
export async function leerExcel(archivo: File): Promise<Celda[][]> {
  const ExcelJS = await cargarExcelJS();
  const libro = new ExcelJS.Workbook();
  await libro.xlsx.load(await archivo.arrayBuffer());
  const hoja = libro.worksheets[0];
  const filas: Celda[][] = [];
  hoja?.eachRow({ includeEmpty: true }, (fila, numero) => {
    const celdas: Celda[] = [];
    for (let c = 1; c <= hoja.columnCount; c++) {
      celdas.push(valorCelda(fila.getCell(c).value));
    }
    filas[numero - 1] = celdas;
  });
  return Array.from(filas, (f) => f ?? []);
}
