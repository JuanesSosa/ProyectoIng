package com.fullstack.fullstack.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Cálculo de notas y progreso. Clase de utilidad sin dependencias de Spring.
 *
 * Una actividad tiene una rúbrica (criterios con peso) y el grupo tiene actividades (con peso).
 * Mientras no se haya calificado todo, se informa:
 * - nota / promedio: promedio ponderado de lo calificado hasta ahora (lo no calificado no cuenta);
 * - avance: porcentaje evaluado (de la rúbrica, o del 100 % de la asignatura);
 * - acumulado (solo del grupo): puntos ya ganados sobre 5.0, contando lo no calificado como 0.
 * Cuando todo está calificado y los pesos suman 100, promedio y acumulado coinciden con la nota final.
 * Todos los resultados se redondean a 2 decimales (mitad hacia arriba).
 */
public final class CalculoNotas {

    private static final int ESCALA_INTERNA = 12;
    private static final int DECIMALES = 2;
    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    private CalculoNotas() {
    }

    /** Criterio de una rúbrica; valor es null mientras no esté calificado. */
    public record Criterio(BigDecimal valor, int peso) {
    }

    /** Actividad de un grupo con el estado de su rúbrica para un estudiante. */
    public record Actividad(int peso, List<Criterio> criterios) {
    }

    /** nota es null si no hay ningún criterio calificado; avance es un porcentaje entero (0 a 100). */
    public record ResultadoActividad(BigDecimal nota, int avance) {
    }

    /** promedio es null si aún no hay nada calificado; acumulado en 0.00 a 5.00; avance como porcentaje entero. */
    public record ResultadoGrupo(BigDecimal promedio, BigDecimal acumulado, int avance) {
    }

    /** Nota de una actividad: promedio ponderado de los criterios ya calificados. */
    public static ResultadoActividad actividad(List<Criterio> criterios) {
        Parcial p = parcial(criterios);
        BigDecimal nota = p.pesoCalificado == 0 ? null : redondear(p.suma.divide(
                BigDecimal.valueOf(p.pesoCalificado), ESCALA_INTERNA, RoundingMode.HALF_UP));
        int avance = p.pesoTotal == 0 ? 0 : BigDecimal.valueOf(100L * p.pesoCalificado)
                .divide(BigDecimal.valueOf(p.pesoTotal), 0, RoundingMode.HALF_UP).intValue();
        return new ResultadoActividad(nota, avance);
    }

    /** Resultado de un grupo a partir de sus actividades. */
    public static ResultadoGrupo grupo(List<Actividad> actividades) {
        BigDecimal puntos = BigDecimal.ZERO;   // suma(peso actividad x nota de la actividad sobre la rúbrica completa)
        BigDecimal evaluado = BigDecimal.ZERO; // suma(peso actividad x fracción de rúbrica calificada)
        for (Actividad actividad : actividades) {
            Parcial p = parcial(actividad.criterios());
            if (p.pesoTotal == 0) {
                continue;
            }
            BigDecimal peso = BigDecimal.valueOf(actividad.peso());
            BigDecimal pesoTotal = BigDecimal.valueOf(p.pesoTotal);
            puntos = puntos.add(peso.multiply(p.suma).divide(pesoTotal, ESCALA_INTERNA, RoundingMode.HALF_UP));
            evaluado = evaluado.add(peso.multiply(BigDecimal.valueOf(p.pesoCalificado))
                    .divide(pesoTotal, ESCALA_INTERNA, RoundingMode.HALF_UP));
        }
        BigDecimal promedio = evaluado.signum() == 0 ? null
                : redondear(puntos.divide(evaluado, ESCALA_INTERNA, RoundingMode.HALF_UP));
        BigDecimal acumulado = redondear(puntos.divide(CIEN, ESCALA_INTERNA, RoundingMode.HALF_UP));
        int avance = evaluado.setScale(0, RoundingMode.HALF_UP).intValue();
        return new ResultadoGrupo(promedio, acumulado, avance);
    }

    /** Suma de pesos y de (nota x peso) de una rúbrica, solo con los criterios calificados. */
    private static Parcial parcial(List<Criterio> criterios) {
        Parcial p = new Parcial();
        for (Criterio criterio : criterios) {
            p.pesoTotal += criterio.peso();
            if (criterio.valor() != null) {
                p.pesoCalificado += criterio.peso();
                p.suma = p.suma.add(criterio.valor().multiply(BigDecimal.valueOf(criterio.peso())));
            }
        }
        return p;
    }

    private static final class Parcial {
        int pesoTotal;
        int pesoCalificado;
        BigDecimal suma = BigDecimal.ZERO;
    }

    /** Redondea en dos pasos para que un empate exacto (por ejemplo 4.125) no se pierda por errores de división. */
    private static BigDecimal redondear(BigDecimal valor) {
        return valor.setScale(8, RoundingMode.HALF_UP).setScale(DECIMALES, RoundingMode.HALF_UP);
    }
}
