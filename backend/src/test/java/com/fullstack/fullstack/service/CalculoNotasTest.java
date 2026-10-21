package com.fullstack.fullstack.service;

import com.fullstack.fullstack.service.CalculoNotas.Actividad;
import com.fullstack.fullstack.service.CalculoNotas.Criterio;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** Pruebas del cálculo de notas por rúbrica, actividad y grupo. */
class CalculoNotasTest {

    private static Criterio c(String valor, int peso) {
        return new Criterio(valor == null ? null : new BigDecimal(valor), peso);
    }

    @Test
    void rubricaCompletaDaElPromedioPonderado() {
        // 5.0 x 50 % + 4.0 x 30 % + 4.5 x 20 % = 4.60
        var r = CalculoNotas.actividad(List.of(c("5.0", 50), c("4.0", 30), c("4.5", 20)));
        assertEquals(new BigDecimal("4.60"), r.nota());
        assertEquals(100, r.avance());
    }

    @Test
    void conUnSoloCriterioCalificadoElAvanceEsSuPeso() {
        var r = CalculoNotas.actividad(List.of(c("5.0", 50), c(null, 30), c(null, 20)));
        assertEquals(new BigDecimal("5.00"), r.nota());
        assertEquals(50, r.avance());
    }

    @Test
    void sinCalificarLaNotaEsNulaYElAvanceCero() {
        var r = CalculoNotas.actividad(List.of(c(null, 50), c(null, 50)));
        assertNull(r.nota());
        assertEquals(0, r.avance());
        assertNull(CalculoNotas.actividad(List.of()).nota());
        assertEquals(0, CalculoNotas.actividad(List.of()).avance());
    }

    @Test
    void unaRubricaQueNoSuma100SeNormalizaSobreSusPropiosPesos() {
        var r = CalculoNotas.actividad(List.of(c("4.0", 40), c("3.0", 40)));
        assertEquals(new BigDecimal("3.50"), r.nota());
        assertEquals(100, r.avance());
    }

    @Test
    void redondeaMitadHaciaArriba() {
        assertEquals(new BigDecimal("4.13"), CalculoNotas.actividad(List.of(c("4.0", 75), c("4.5", 25))).nota());
        assertEquals(new BigDecimal("3.99"),
                CalculoNotas.actividad(List.of(c("5.0", 33), c("4.0", 33), c("3.0", 34))).nota());
    }

    @Test
    void grupoConSoloElParcial1CalificadoLlevaElProgreso() {
        List<Criterio> parcial1 = List.of(c("5.0", 50), c("4.0", 30), c("4.5", 20));
        List<Criterio> sinNota = List.of(c(null, 50), c(null, 50));
        var r = CalculoNotas.grupo(List.of(new Actividad(30, parcial1), new Actividad(30, sinNota),
                new Actividad(30, sinNota), new Actividad(10, sinNota)));

        assertEquals(new BigDecimal("4.60"), r.promedio());   // promedio de lo evaluado
        assertEquals(new BigDecimal("1.38"), r.acumulado());  // 4.60 x 30 %
        assertEquals(30, r.avance());
    }

    @Test
    void grupoCompletoDaLaNotaFinal() {
        var r = CalculoNotas.grupo(List.of(
                new Actividad(30, List.of(c("5.0", 50), c("4.0", 30), c("4.5", 20))),   // 4.60
                new Actividad(30, List.of(c("3.0", 50), c("4.0", 50))),                  // 3.50
                new Actividad(30, List.of(c("5.0", 50), c("5.0", 50))),                  // 5.00
                new Actividad(10, List.of(c("4.0", 60), c("3.0", 40)))));                // 3.60
        // 0.3 x 4.6 + 0.3 x 3.5 + 0.3 x 5 + 0.1 x 3.6 = 4.29
        assertEquals(new BigDecimal("4.29"), r.promedio());
        assertEquals(new BigDecimal("4.29"), r.acumulado());
        assertEquals(100, r.avance());
    }

    @Test
    void actividadACadaMedias() {
        var r = CalculoNotas.grupo(List.of(
                new Actividad(30, List.of(c("5.0", 50), c(null, 30), c(null, 20))),
                new Actividad(70, List.of(c(null, 100)))));
        assertEquals(new BigDecimal("5.00"), r.promedio());
        assertEquals(new BigDecimal("0.75"), r.acumulado());
        assertEquals(15, r.avance());
    }

    @Test
    void sinNadaCalificado() {
        var r = CalculoNotas.grupo(List.of(new Actividad(100, List.of(c(null, 100)))));
        assertNull(r.promedio());
        assertEquals(new BigDecimal("0.00"), r.acumulado());
        assertEquals(0, r.avance());
        assertNull(CalculoNotas.grupo(List.of()).promedio());
    }

    @Test
    void siLasActividadesNoSuman100ElPromedioSeNormalizaPeroElAcumuladoNo() {
        var r = CalculoNotas.grupo(List.of(new Actividad(60, List.of(c("5.0", 100)))));
        assertEquals(new BigDecimal("5.00"), r.promedio());
        assertEquals(new BigDecimal("3.00"), r.acumulado());
        assertEquals(60, r.avance());
    }
}
