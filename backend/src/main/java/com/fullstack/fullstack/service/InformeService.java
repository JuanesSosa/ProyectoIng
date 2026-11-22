package com.fullstack.fullstack.service;

import com.fullstack.fullstack.dto.ActividadDtos.ActividadResumen;
import com.fullstack.fullstack.dto.ActividadDtos.ResumenGrupoResponse;
import com.fullstack.fullstack.dto.InformeDtos.EnvioInformeResponse;
import com.fullstack.fullstack.exception.ReglaNegocioException;
import com.fullstack.fullstack.model.entity.Usuario;
import com.fullstack.fullstack.repository.UsuarioRepository;
import com.fullstack.fullstack.security.Actor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Envía por correo el informe de notas de cada estudiante de un grupo. */
@Service
public class InformeService {

    private final ProgresoService progresoService;
    private final UsuarioRepository usuarioRepository;
    private final MailService mailService;

    public InformeService(ProgresoService progresoService, UsuarioRepository usuarioRepository,
                          MailService mailService) {
        this.progresoService = progresoService;
        this.usuarioRepository = usuarioRepository;
        this.mailService = mailService;
    }

    public EnvioInformeResponse enviarInformeGrupo(Long grupoId, Actor actor) {
        if (!mailService.estaConfigurado()) {
            throw new ReglaNegocioException("El envío de correos no está configurado en el servidor");
        }
        // Valida que quien pide sea el docente del grupo (lanza 403 si no).
        List<ResumenGrupoResponse> resumenes = progresoService.resumenDelGrupo(grupoId, actor);
        if (resumenes.isEmpty()) {
            throw new ReglaNegocioException("El grupo no tiene estudiantes matriculados");
        }

        List<Long> ids = resumenes.stream().map(ResumenGrupoResponse::estudianteId).toList();
        Map<Long, Usuario> usuarios = new HashMap<>();
        for (Usuario u : usuarioRepository.findActivosDeEstudiantes(ids)) {
            usuarios.putIfAbsent(u.getEstudiante().getId(), u);
        }

        int enviados = 0;
        int sinCorreo = 0;
        int fallidos = 0;
        for (ResumenGrupoResponse r : resumenes) {
            Usuario usuario = usuarios.get(r.estudianteId());
            if (usuario == null) {
                sinCorreo++;
                continue;
            }
            String asunto = "Informe de notas: " + r.asignaturaNombre() + " (" + r.grupoNombre() + ")";
            if (mailService.enviar(usuario.getCorreo(), usuario.getNombre(), asunto, construirHtml(r))) {
                enviados++;
            } else {
                fallidos++;
            }
        }
        return new EnvioInformeResponse(enviados, sinCorreo, fallidos);
    }

    private String construirHtml(ResumenGrupoResponse r) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div style=\"font-family:Arial,sans-serif;max-width:600px\">");
        sb.append("<h2>Informe de notas</h2>");
        sb.append("<p>Hola ").append(esc(r.estudianteNombre())).append(",</p>");
        sb.append("<p>Este es tu avance en <b>").append(esc(r.asignaturaNombre())).append("</b> (grupo ")
                .append(esc(r.grupoNombre())).append(", periodo ").append(esc(r.periodo())).append(").</p>");
        sb.append("<table style=\"border-collapse:collapse;width:100%\">");
        sb.append("<tr><th style=\"text-align:left;border-bottom:1px solid #ccc\">Actividad</th>")
                .append("<th style=\"border-bottom:1px solid #ccc\">Peso</th>")
                .append("<th style=\"border-bottom:1px solid #ccc\">Nota</th></tr>");
        for (ActividadResumen a : r.actividades()) {
            sb.append("<tr><td>").append(esc(a.nombre())).append("</td>")
                    .append("<td style=\"text-align:center\">").append(a.peso()).append("%</td>")
                    .append("<td style=\"text-align:center\">").append(nota(a.nota())).append("</td></tr>");
        }
        sb.append("</table>");
        sb.append("<p><b>Promedio de lo calificado:</b> ").append(nota(r.promedio())).append("<br>");
        sb.append("<b>Acumulado (sobre 5.0):</b> ").append(nota(r.acumulado())).append("<br>");
        sb.append("<b>Avance evaluado:</b> ").append(r.avance()).append("%</p>");
        sb.append("</div>");
        return sb.toString();
    }

    private static String nota(BigDecimal valor) {
        return valor == null ? "—" : valor.toPlainString();
    }

    /** Escapa texto para HTML (los nombres de actividades los escribe el docente). */
    private static String esc(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}