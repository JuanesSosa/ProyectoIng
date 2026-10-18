/**
 * Cálculo de promedios en el cliente. Replica exactamente la regla del backend (PromedioCalculator.java)
 * para poder mostrar el promedio mientras el docente escribe las notas:
 * - Si todas las evaluaciones tienen peso: promedio ponderado = suma(nota x peso) / suma(pesos).
 * - Si alguna no tiene peso: promedio simple.
 * - Redondeo a 2 decimales (mitad hacia arriba). Sin notas: null.
 */
export interface ItemNota {
  valor: number | null;
  peso: number | null;
}

/** Nota mínima para aprobar. */
export const NOTA_MINIMA_APROBATORIA = 3.0;

export function calcularPromedio(items: ItemNota[]): number | null {
  const conNota = items.filter((i) => i.valor !== null && Number.isFinite(i.valor));
  if (conNota.length === 0) {
    return null;
  }
  const ponderado = conNota.every((i) => i.peso !== null && i.peso > 0);

  // Aritmética con enteros (centésimas) para evitar errores de coma flotante al redondear.
  let numerador = 0;
  let divisor = 0;
  for (const item of conNota) {
    const centesimas = Math.round((item.valor as number) * 100);
    const peso = ponderado ? (item.peso as number) : 1;
    numerador += centesimas * peso;
    divisor += peso;
  }
  return Math.floor((2 * numerador + divisor) / (2 * divisor)) / 100;
}

/** Suma de los pesos definidos (las evaluaciones sin peso cuentan 0). */
export function pesoTotal(items: ItemNota[]): number {
  return items.reduce((suma, i) => suma + (i.peso ?? 0), 0);
}

/** Promedio simple de los promedios por asignatura (ignora los null). */
export function promedioGeneral(promedios: (number | null)[]): number | null {
  const validos = promedios.filter((p): p is number => p !== null);
  if (validos.length === 0) {
    return null;
  }
  const suma = validos.reduce((acc, p) => acc + Math.round(p * 100), 0);
  return Math.floor((2 * suma + validos.length) / (2 * validos.length)) / 100;
}
