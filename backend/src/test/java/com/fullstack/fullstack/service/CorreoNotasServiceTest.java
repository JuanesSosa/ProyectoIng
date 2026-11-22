package com.fullstack.fullstack.service;

import com.fullstack.fullstack.dto.ActividadDtos.ActividadResumen;
import com.fullstack.fullstack.dto.ActividadDtos.CriterioNota;
import com.fullstack.fullstack.dto.ActividadDtos.ResumenGrupoResponse;
import com.fullstack.fullstack.dto.CorreoDtos.EnvioResponse;
import com.fullstack.fullstack.exception.ReglaNegocioException;
import com.fullstack.fullstack.model.entity.Rol;
import com.fullstack.fullstack.security.Actor;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Pruebas del envío de notas por correo (MailService simulado, no se envía nada). */
class CorreoNotasServiceTest {

    private final Actor docente = new Actor(2L, Rol.DOCENTE, null);

    private ResumenGrupoResponse resumen(Long id, String nombre, String correo) {
        CriterioNota teoria = new CriterioNota(1L, "Teoría", 60, new BigDecimal("4.50"));
        CriterioNota practica = new CriterioNota(2L, "Práctica", 40, null);
        ActividadResumen parcial = new ActividadResumen(1L, "Parcial 1", 30, List.of(teoria, practica),
                new BigDecimal("4.50"), 60);
        return new ResumenGrupoResponse(5L, 1L, "Ingeniería de Software", "Grupo 1", "2026-2", id, "123",
                "2220242001", nombre, correo, List.of(parcial), 30, new BigDecimal("4.50"),
                new BigDecimal("1.35"), 18);
    }

    private MailService correo(boolean configurado) {
        MailService mail = mock(MailService.class);
        when(mail.estaConfigurado()).thenReturn(configurado);
        when(mail.enviar(any(), any(), any(), any())).thenReturn(true);
        return mail;
    }

    @Test
    void sinServidorDeCorreoConfiguradoExplicaQueFalta() {
        ProgresoService progreso = mock(ProgresoService.class);
        CorreoNotasService service = new CorreoNotasService(progreso, correo(false));

        assertThrows(ReglaNegocioException.class, () -> service.enviar(5L, null, docente));
        verify(progreso, never()).resumenDelGrupo(any(), any());
    }

    @Test
    void enviaUnCorreoPorEstudianteYReportaLosQueNoTienenCorreo() {
        ProgresoService progreso = mock(ProgresoService.class);
        when(progreso.resumenDelGrupo(5L, docente)).thenReturn(List.of(
                resumen(1L, "Juan Pérez", "juan@x.com"), resumen(2L, "Ana López", null)));
        MailService mail = correo(true);
        CorreoNotasService service = new CorreoNotasService(progreso, mail);

        EnvioResponse respuesta = service.enviar(5L, null, docente);

        assertEquals(1, respuesta.enviados());
        assertEquals(1, respuesta.fallidos());
        assertTrue(respuesta.detalles().get(0).contains("Ana López"));
        verify(mail).enviar(eq("juan@x.com"), eq("Juan Pérez"), any(), any());
    }

    @Test
    void puedeEnviarSoloAUnEstudianteDelGrupo() {
        ProgresoService progreso = mock(ProgresoService.class);
        when(progreso.resumenDelGrupo(5L, docente)).thenReturn(List.of(
                resumen(1L, "Juan Pérez", "juan@x.com"), resumen(2L, "Ana López", "ana@x.com")));
        CorreoNotasService service = new CorreoNotasService(progreso, correo(true));

        assertEquals(1, service.enviar(5L, 2L, docente).enviados());
        assertThrows(ReglaNegocioException.class, () -> service.enviar(5L, 99L, docente));
    }

    @Test
    void elTextoDelCorreoIncluyeActividadesCriteriosYTotales() {
        String texto = CorreoNotasService.redactar(resumen(1L, "Juan Pérez", "juan@x.com"));

        assertTrue(texto.contains("Hola Juan Pérez,"));
        assertTrue(texto.contains("Parcial 1 (30%): 4.50"), texto);
        assertTrue(texto.contains("Teoría (60%): 4.50"));
        assertTrue(texto.contains("Práctica (40%): pendiente"));
        assertTrue(texto.contains("<b>Acumulado:</b> 1.35 / 5.0"));
        assertTrue(texto.contains("<b>Avance de la asignatura:</b> 18%"));
    }
}
