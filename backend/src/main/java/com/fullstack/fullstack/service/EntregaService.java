package com.fullstack.fullstack.service;

import com.fullstack.fullstack.dto.EntregaDtos.EntregaResponse;
import com.fullstack.fullstack.exception.AccesoDenegadoException;
import com.fullstack.fullstack.exception.ReglaNegocioException;
import com.fullstack.fullstack.exception.ResourceNotFoundException;
import com.fullstack.fullstack.model.entity.Actividad;
import com.fullstack.fullstack.model.entity.Entrega;
import com.fullstack.fullstack.model.entity.Estudiante;
import com.fullstack.fullstack.model.entity.Rol;
import com.fullstack.fullstack.repository.ActividadRepository;
import com.fullstack.fullstack.repository.EntregaRepository;
import com.fullstack.fullstack.repository.EstudianteRepository;
import com.fullstack.fullstack.security.Actor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Entregas de actividades: el estudiante matriculado sube archivos (máximo 5 por actividad); el docente del
 * grupo y el administrador los descargan; el estudiante puede borrar los suyos.
 */
@Service
@Transactional
public class EntregaService {

    /** Archivos máximos por estudiante y actividad. */
    static final int MAX_ARCHIVOS = 5;
    /** Tamaño máximo por archivo: 10 MB (el mismo límite está en application.properties). */
    static final long MAX_BYTES = 10L * 1024 * 1024;
    private static final Set<String> EXTENSIONES = Set.of(
            "pdf", "doc", "docx", "ppt", "pptx", "xls", "xlsx", "txt", "zip", "png", "jpg", "jpeg");

    /** Archivo listo para descargar. */
    public record Descarga(String nombre, Path ruta) {
    }

    private final EntregaRepository entregaRepository;
    private final ActividadRepository actividadRepository;
    private final EstudianteRepository estudianteRepository;
    private final AlmacenArchivos almacen;
    private final Permisos permisos;

    public EntregaService(EntregaRepository entregaRepository,
                          ActividadRepository actividadRepository,
                          EstudianteRepository estudianteRepository,
                          AlmacenArchivos almacen,
                          Permisos permisos) {
        this.entregaRepository = entregaRepository;
        this.actividadRepository = actividadRepository;
        this.estudianteRepository = estudianteRepository;
        this.almacen = almacen;
        this.permisos = permisos;
    }

    /** Sube un archivo como entrega del estudiante que hace la petición (rol ESTUDIANTE, matriculado en el grupo). */
    public EntregaResponse subir(Long actividadId, MultipartFile archivo, Actor actor) {
        Actividad actividad = buscarActividad(actividadId);
        if (actor.rol() != Rol.ESTUDIANTE || actor.estudianteId() == null) {
            throw new AccesoDenegadoException("Solo los estudiantes pueden subir entregas");
        }
        permisos.puedeVerGrupo(actividad.getGrupo(), actor); // debe estar matriculado

        if (archivo == null || archivo.isEmpty()) {
            throw new ReglaNegocioException("Selecciona un archivo para subir");
        }
        if (archivo.getSize() > MAX_BYTES) {
            throw new ReglaNegocioException("El archivo supera el tamaño máximo de 10 MB");
        }
        String nombre = limpiarNombre(archivo.getOriginalFilename());
        String extension = extension(nombre);
        if (!EXTENSIONES.contains(extension)) {
            throw new ReglaNegocioException("Tipo de archivo no permitido. Se aceptan: pdf, doc, docx, ppt, pptx, "
                    + "xls, xlsx, txt, zip, png, jpg, jpeg");
        }
        if (entregaRepository.countByActividadIdAndEstudianteId(actividadId, actor.estudianteId()) >= MAX_ARCHIVOS) {
            throw new ReglaNegocioException("Ya subiste el máximo de " + MAX_ARCHIVOS
                    + " archivos para esta actividad; borra alguno para subir otro");
        }

        Estudiante estudiante = estudianteRepository.findById(actor.estudianteId())
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante no encontrado"));
        String guardado = almacen.guardar(archivo, extension);
        try {
            Entrega entrega = Entrega.builder()
                    .actividad(actividad)
                    .estudiante(estudiante)
                    .nombre(nombre)
                    .tamano(archivo.getSize())
                    .archivo(guardado)
                    .fecha(Instant.now())
                    .build();
            return EntregaResponse.from(entregaRepository.saveAndFlush(entrega));
        } catch (RuntimeException e) {
            almacen.borrar(guardado); // si no se pudo registrar, no dejar el archivo huérfano
            throw e;
        }
    }

    /** Entregas de un estudiante en una actividad: el estudiante ve las suyas; admin y docente indican el estudiante. */
    @Transactional(readOnly = true)
    public List<EntregaResponse> listar(Long actividadId, Long estudianteSolicitado, Actor actor) {
        Actividad actividad = buscarActividad(actividadId);
        permisos.puedeVerGrupo(actividad.getGrupo(), actor);
        Long estudianteId = permisos.estudianteConsultado(actor, estudianteSolicitado);
        return entregaRepository.findByActividadIdAndEstudianteIdOrderByIdAsc(actividadId, estudianteId).stream()
                .map(EntregaResponse::from)
                .toList();
    }

    /** Archivo de una entrega: su dueño, el docente del grupo o el administrador. */
    @Transactional(readOnly = true)
    public Descarga descargar(Long entregaId, Actor actor) {
        Entrega entrega = buscar(entregaId);
        boolean esDueno = actor.rol() == Rol.ESTUDIANTE && actor.estudianteId() != null
                && actor.estudianteId().equals(entrega.getEstudiante().getId());
        if (!esDueno) {
            permisos.puedeVerDetalleGrupo(entrega.getActividad().getGrupo(), actor);
        }
        return new Descarga(entrega.getNombre(), almacen.ruta(entrega.getArchivo()));
    }

    /** Borra una entrega; solo su dueño. */
    public void eliminar(Long entregaId, Actor actor) {
        Entrega entrega = buscar(entregaId);
        if (actor.rol() != Rol.ESTUDIANTE || actor.estudianteId() == null
                || !actor.estudianteId().equals(entrega.getEstudiante().getId())) {
            throw new AccesoDenegadoException("Solo puedes borrar tus propias entregas");
        }
        entregaRepository.delete(entrega);
        entregaRepository.flush();
        almacen.borrar(entrega.getArchivo());
    }

    // ---------- utilidades ----------

    /** Deja solo el nombre del archivo (sin carpetas ni caracteres de control) y lo recorta a 150 caracteres. */
    static String limpiarNombre(String original) {
        String nombre = original == null ? "" : original;
        nombre = nombre.substring(Math.max(nombre.lastIndexOf('/'), nombre.lastIndexOf('\\')) + 1);
        nombre = nombre.replaceAll("\\p{Cntrl}", "").trim();
        if (nombre.length() > 150) {
            String ext = extension(nombre);
            nombre = nombre.substring(0, 150 - ext.length() - 1) + "." + ext;
        }
        if (nombre.isEmpty() || nombre.equals(".") || nombre.equals("..")) {
            throw new ReglaNegocioException("El archivo no tiene un nombre válido");
        }
        return nombre;
    }

    static String extension(String nombre) {
        int punto = nombre.lastIndexOf('.');
        return punto < 0 ? "" : nombre.substring(punto + 1).toLowerCase(Locale.ROOT);
    }

    private Entrega buscar(Long id) {
        return entregaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entrega no encontrada con id " + id));
    }

    private Actividad buscarActividad(Long id) {
        return actividadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Actividad no encontrada con id " + id));
    }
}
