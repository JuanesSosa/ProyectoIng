package com.fullstack.fullstack.service;

import com.fullstack.fullstack.dto.NotaDtos.EvaluacionRequest;
import com.fullstack.fullstack.dto.NotaDtos.RegistroNotasRequest;
import com.fullstack.fullstack.dto.NotaDtos.RegistroNotasResponse;
import com.fullstack.fullstack.dto.NotaDtos.ResumenNotasResponse;
import com.fullstack.fullstack.exception.ReglaNegocioException;
import com.fullstack.fullstack.model.entity.Asignatura;
import com.fullstack.fullstack.model.entity.Estudiante;
import com.fullstack.fullstack.model.entity.Nota;
import com.fullstack.fullstack.repository.AsignaturaRepository;
import com.fullstack.fullstack.repository.EstudianteRepository;
import com.fullstack.fullstack.repository.NotaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Pruebas de las reglas de negocio de las notas. El repositorio se simula con Mockito (sin base de datos). */
@ExtendWith(MockitoExtension.class)
class NotaServiceTest {

    @Mock
    private NotaRepository notaRepository;

    @Mock
    private EstudianteRepository estudianteRepository;

    @Mock
    private AsignaturaRepository asignaturaRepository;

    @InjectMocks
    private NotaService service;

    private final Estudiante juan = Estudiante.builder().id(1L).documento("123456").nombre("Juan").curso("10A").build();
    private final Asignatura mate = Asignatura.builder().id(2L).codigo("MAT101").nombre("Matematicas").activa(true).build();

    private static EvaluacionRequest evaluacion(String nombre, Integer peso, String valor) {
        return new EvaluacionRequest(nombre, peso, new BigDecimal(valor));
    }

    private static RegistroNotasRequest registro(EvaluacionRequest... evaluaciones) {
        return new RegistroNotasRequest(1L, 2L, "2026-2", List.of(evaluaciones));
    }

    private void estudianteYAsignaturaExisten() {
        when(estudianteRepository.findById(1L)).thenReturn(Optional.of(juan));
        when(asignaturaRepository.findById(2L)).thenReturn(Optional.of(mate));
    }

    @Test
    void rechazaPesosQueSumanMasDe100() {
        estudianteYAsignaturaExisten();

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> service.registrar(registro(evaluacion("Parcial", 60, "4.0"), evaluacion("Final", 50, "4.0"))));

        assertTrue(ex.getMessage().contains("100"));
        verify(notaRepository, never()).saveAll(anyList());
    }

    @Test
    void rechazaMezclarEvaluacionesConYSinPeso() {
        estudianteYAsignaturaExisten();

        assertThrows(ReglaNegocioException.class,
                () -> service.registrar(registro(evaluacion("Parcial", 50, "4.0"), evaluacion("Final", null, "4.0"))));
    }

    @Test
    void rechazaEvaluacionesRepetidasSinImportarMayusculas() {
        estudianteYAsignaturaExisten();

        assertThrows(ReglaNegocioException.class,
                () -> service.registrar(registro(evaluacion("Parcial", 50, "4.0"), evaluacion(" parcial ", 50, "3.0"))));
    }

    @Test
    void rechazaRegistrarNotasEnUnaAsignaturaDesactivada() {
        mate.setActiva(false);
        estudianteYAsignaturaExisten();

        assertThrows(ReglaNegocioException.class,
                () -> service.registrar(registro(evaluacion("Parcial", 100, "4.0"))));
    }

    @Test
    void actualizaLasExistentesCreaLasNuevasYEliminaLasQueYaNoVienen() {
        estudianteYAsignaturaExisten();
        Nota parcial = Nota.builder().id(10L).estudiante(juan).asignatura(mate).periodo("2026-2")
                .evaluacion("Parcial").peso(100).valor(new BigDecimal("3.00")).build();
        Nota viejo = Nota.builder().id(11L).estudiante(juan).asignatura(mate).periodo("2026-2")
                .evaluacion("Viejo").peso(null).valor(new BigDecimal("2.00")).build();
        when(notaRepository.findByEstudianteIdAndAsignaturaIdAndPeriodoOrderByIdAsc(1L, 2L, "2026-2"))
                .thenReturn(List.of(parcial, viejo));
        when(notaRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        RegistroNotasResponse respuesta = service.registrar(
                registro(evaluacion("PARCIAL", 50, "4.0"), evaluacion("Final", 50, "5.0")));

        verify(notaRepository).deleteAll(List.of(viejo));
        assertEquals(50, parcial.getPeso());
        assertEquals(new BigDecimal("4.00"), parcial.getValor());
        assertEquals(2, respuesta.notas().size());
        assertEquals(new BigDecimal("4.50"), respuesta.promedio());
        assertEquals(100, respuesta.pesoTotal());
    }

    @Test
    void resumenCalculaPromedioPorAsignaturaYGeneral() {
        Asignatura ingles = Asignatura.builder().id(3L).codigo("ING101").nombre("Ingles").activa(true).build();
        when(estudianteRepository.findById(1L)).thenReturn(Optional.of(juan));
        when(notaRepository.findDelPeriodo(1L, "2026-2")).thenReturn(List.of(
                nota(mate, "Parcial", 30, "4.2"), nota(mate, "Taller", 30, "3.8"), nota(mate, "Final", 40, "4.5"),
                nota(ingles, "Nota 1", null, "4.0"), nota(ingles, "Nota 2", null, "4.3"), nota(ingles, "Nota 3", null, "4.1")));

        ResumenNotasResponse resumen = service.resumen(1L, "2026-2");

        assertEquals(2, resumen.asignaturas().size());
        assertEquals(new BigDecimal("4.20"), resumen.asignaturas().get(0).promedio());
        assertEquals(new BigDecimal("4.13"), resumen.asignaturas().get(1).promedio());
        assertEquals(new BigDecimal("4.17"), resumen.promedioGeneral());
    }

    private Nota nota(Asignatura asignatura, String evaluacion, Integer peso, String valor) {
        return Nota.builder().estudiante(juan).asignatura(asignatura).periodo("2026-2")
                .evaluacion(evaluacion).peso(peso).valor(new BigDecimal(valor)).build();
    }
}
