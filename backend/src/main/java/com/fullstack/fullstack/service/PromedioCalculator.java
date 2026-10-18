package com.fullstack.fullstack.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Cálculo de promedios (RF-10, HU-07). Clase de utilidad sin dependencias de Spring.
 *
 * Reglas:
 * - Si todas las evaluaciones tienen peso, el promedio es ponderado: suma(nota x peso) / suma(pesos).
 *   Mientras los pesos no lleguen a 100 %, el resultado es el promedio de lo evaluado hasta el momento.
 * - Si alguna evaluación no tiene peso, se usa el promedio simple.
 * - El resultado se redondea a 2 decimales (mitad hacia arriba). Sin notas, el promedio es null.
 */
public final class PromedioCalculator {

    private static final int DECIMALES = 2;

    private PromedioCalculator() {
    }

    /** Nota y peso (opcional) de una evaluación. */
    public record Item(BigDecimal valor, Integer peso) {
    }

    /** Promedio de las evaluaciones de una asignatura; null si no hay evaluaciones. */
    public static BigDecimal promedio(List<Item> items) {
        if (items == null || items.isEmpty()) {
            return null;
        }
        boolean ponderado = items.stream().allMatch(i -> i.peso() != null && i.peso() > 0);

        BigDecimal suma = BigDecimal.ZERO;
        BigDecimal divisor;
        if (ponderado) {
            for (Item item : items) {
                suma = suma.add(item.valor().multiply(BigDecimal.valueOf(item.peso())));
            }
            divisor = BigDecimal.valueOf(pesoTotal(items));
        } else {
            for (Item item : items) {
                suma = suma.add(item.valor());
            }
            divisor = BigDecimal.valueOf(items.size());
        }
        return suma.divide(divisor, DECIMALES, RoundingMode.HALF_UP);
    }

    /** Suma de los pesos definidos (las evaluaciones sin peso cuentan 0). */
    public static int pesoTotal(List<Item> items) {
        if (items == null) {
            return 0;
        }
        return items.stream().mapToInt(i -> i.peso() == null ? 0 : i.peso()).sum();
    }

    /** Promedio general: promedio simple de los promedios por asignatura (ignora los null); null si no hay ninguno. */
    public static BigDecimal promedioGeneral(List<BigDecimal> promediosPorAsignatura) {
        if (promediosPorAsignatura == null) {
            return null;
        }
        List<BigDecimal> validos = promediosPorAsignatura.stream().filter(p -> p != null).toList();
        if (validos.isEmpty()) {
            return null;
        }
        BigDecimal suma = BigDecimal.ZERO;
        for (BigDecimal p : validos) {
            suma = suma.add(p);
        }
        return suma.divide(BigDecimal.valueOf(validos.size()), DECIMALES, RoundingMode.HALF_UP);
    }
}
