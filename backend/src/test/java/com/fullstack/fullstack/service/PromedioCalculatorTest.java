package com.fullstack.fullstack.service;

import com.fullstack.fullstack.service.PromedioCalculator.Item;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** Pruebas del cálculo de promedios (RF-10, HU-07). No usan Spring ni base de datos. */
class PromedioCalculatorTest {

    private static BigDecimal n(String valor) {
        return new BigDecimal(valor);
    }

    @Test
    void promedioPonderadoUsaLosPesos() {
        // Mock-up 5.4: Parcial 30 % 4.2, Taller 30 % 3.8, Final 40 % 4.5
        List<Item> items = List.of(new Item(n("4.2"), 30), new Item(n("3.8"), 30), new Item(n("4.5"), 40));

        assertEquals(new BigDecimal("4.20"), PromedioCalculator.promedio(items));
        assertEquals(100, PromedioCalculator.pesoTotal(items));
    }

    @Test
    void sinPesosUsaPromedioSimple() {
        List<Item> items = List.of(new Item(n("4.0"), null), new Item(n("4.3"), null), new Item(n("4.1"), null));

        assertEquals(new BigDecimal("4.13"), PromedioCalculator.promedio(items));
        assertEquals(0, PromedioCalculator.pesoTotal(items));
    }

    @Test
    void conPesosIncompletosPromediaLoEvaluado() {
        List<Item> items = List.of(new Item(n("4.0"), 30), new Item(n("5.0"), 30));

        assertEquals(new BigDecimal("4.50"), PromedioCalculator.promedio(items));
        assertEquals(60, PromedioCalculator.pesoTotal(items));
    }

    @Test
    void siFaltaUnPesoUsaPromedioSimple() {
        List<Item> items = List.of(new Item(n("4.0"), 30), new Item(n("5.0"), null));

        assertEquals(new BigDecimal("4.50"), PromedioCalculator.promedio(items));
    }

    @Test
    void sinNotasElPromedioEsNulo() {
        assertNull(PromedioCalculator.promedio(List.of()));
        assertNull(PromedioCalculator.promedio(null));
    }

    @Test
    void redondeaMitadHaciaArriba() {
        List<Item> items = List.of(new Item(n("3.33"), 50), new Item(n("3.34"), 50));

        assertEquals(new BigDecimal("3.34"), PromedioCalculator.promedio(items));
    }

    @Test
    void promedioGeneralEsElPromedioSimpleDeLasAsignaturas() {
        assertEquals(new BigDecimal("4.30"),
                PromedioCalculator.promedioGeneral(List.of(n("4.17"), n("4.13"), n("4.60"))));
        assertNull(PromedioCalculator.promedioGeneral(List.of()));
    }
}
