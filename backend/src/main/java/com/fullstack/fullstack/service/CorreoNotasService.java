package com.fullstack.fullstack.service;

import com.fullstack.fullstack.dto.ActividadDtos.ActividadResumen;
import com.fullstack.fullstack.dto.ActividadDtos.CriterioNota;
import com.fullstack.fullstack.dto.ActividadDtos.ResumenGrupoResponse;
import com.fullstack.fullstack.dto.CorreoDtos.EnvioResponse;
import com.fullstack.fullstack.exception.ReglaNegocioException;
import com.fullstack.fullstack.security.Actor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Envía a los estudiantes un correo con sus notas del curso (por actividad y criterio) usando MailService (Brevo).
 * Se escribe al correo registrado en el estudiante. Si el servidor de correo no está configurado
 * (BREVO_API_KEY y MAIL_FROM), responde con un mensaje claro en lugar de fallar.
 */
@Service
public class CorreoNotasService {

    private final ProgresoService progresoService;
    private final MailService mailService;

    public CorreoNotasService(ProgresoService progresoService, MailService mailService) {
        this.progresoService = progresoService;
        this.mailService = mailService;
    }

    /** Envía las notas de todo el grupo o, si se indica estudianteId, solo de ese estudiante. */
    public EnvioResponse enviar(Long grupoId, Long estudianteId, Actor actor) {
        if (!mailService.estaConfigurado()) {
            throw new ReglaNegocioException("El envío de correos no está configurado en el servidor. "
                    + "Define BREVO_API_KEY y MAIL_FROM (ver README.md)");
        }
        // Valida que el actor sea el docente del grupo (o admin) y calcula las notas
        List<ResumenGrupoResponse> resumenes = progresoService.resumenDelGrupo(grupoId, actor);
        if (estudianteId != null) {
            resumenes = resumenes.stream().filter(r -> estudianteId.equals(r.estudianteId())).toList();
            if (resumenes.isEmpty()) {
                throw new ReglaNegocioException("El estudiante no está matriculado en este grupo");
            }
        }
        if (resumenes.isEmpty()) {
            throw new ReglaNegocioException("El grupo no tiene estudiantes matriculados");
        }

        int enviados = 0;
        List<String> detalles = new ArrayList<>();
        for (ResumenGrupoResponse r : resumenes) {
            String correo = r.estudianteCorreo();
            if (correo == null || correo.isBlank()) {
                detalles.add(r.estudianteNombre() + ": no tiene correo registrado");
                continue;
            }
            String asunto = "Tus notas de " + r.asignaturaNombre() + " - " + r.grupoNombre();
            if (mailService.enviar(correo.trim(), r.estudianteNombre(), asunto, redactar(r))) {
                enviados++;
            } else {
                detalles.add(r.estudianteNombre() + ": no se pudo enviar el correo");
            }
        }
        return new EnvioResponse(enviados, detalles.size(), detalles);
    }

    /** Cuerpo HTML del correo (visible en el paquete para probarlo sin enviar nada). */
    static String redactar(ResumenGrupoResponse r) {
        StringBuilder h = new StringBuilder();
        h.append("<div style=\"font-family:Arial,sans-serif;max-width:600px\">");
        h.append("<h2>Informe de notas</h2>");
        h.append("<p>Hola ").append(esc(r.estudianteNombre())).append(",</p>");
        h.append("<p>Estas son tus notas de <b>").append(esc(r.asignaturaNombre())).append("</b> (")
                .append(esc(r.grupoNombre())).append(", ").append(esc(r.periodo())).append(").</p>");
        if (r.actividades().isEmpty()) {
            h.append("<p>Todavía no hay actividades definidas.</p>");
        }
        for (ActividadResumen a : r.actividades()) {
            h.append("<p><b>").append(esc(a.nombre())).append(" (").append(a.peso()).append("%): ")
                    .append(a.nota() == null ? "sin calificar" : a.nota().toPlainString()).append("</b></p>");
            if (!a.criterios().isEmpty()) {
                h.append("<ul>");
                for (CriterioNota c : a.criterios()) {
                    h.append("<li>").append(esc(c.nombre())).append(" (").append(c.peso()).append("%): ")
                            .append(c.valor() == null ? "pendiente" : c.valor().toPlainString()).append("</li>");
                }
                h.append("</ul>");
            }
        }
        BigDecimal promedio = r.promedio();
        h.append("<p><b>Promedio de lo calificado:</b> ").append(promedio == null ? "-" : promedio.toPlainString())
                .append("<br><b>Acumulado:</b> ").append(r.acumulado().toPlainString()).append(" / 5.0")
                .append("<br><b>Avance de la asignatura:</b> ").append(r.avance()).append("%</p>");
        h.append("<p style=\"color:#666\">Mensaje generado automáticamente por el Gestor de Notas Académicas.</p></div>");
        return h.toString();
    }

    /** Escapa texto para HTML (los nombres los escribe el docente). */
    private static String esc(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
