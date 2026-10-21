package com.fullstack.fullstack.service;

import com.fullstack.fullstack.dto.GrupoDtos.GrupoRequest;
import com.fullstack.fullstack.dto.GrupoDtos.GrupoResponse;
import com.fullstack.fullstack.dto.GrupoDtos.MatriculadoResponse;
import com.fullstack.fullstack.dto.GrupoDtos.PanelResponse;
import com.fullstack.fullstack.exception.ConflictoException;
import com.fullstack.fullstack.exception.ReglaNegocioException;
import com.fullstack.fullstack.exception.ResourceNotFoundException;
import com.fullstack.fullstack.model.entity.Asignatura;
import com.fullstack.fullstack.model.entity.Estudiante;
import com.fullstack.fullstack.model.entity.Grupo;
import com.fullstack.fullstack.model.entity.Matricula;
import com.fullstack.fullstack.model.entity.Rol;
import com.fullstack.fullstack.model.entity.Usuario;
import com.fullstack.fullstack.repository.ActividadRepository;
import com.fullstack.fullstack.repository.AsignaturaRepository;
import com.fullstack.fullstack.repository.CalificacionRepository;
import com.fullstack.fullstack.repository.EstudianteRepository;
import com.fullstack.fullstack.repository.GrupoRepository;
import com.fullstack.fullstack.repository.MatriculaRepository;
import com.fullstack.fullstack.repository.UsuarioRepository;
import com.fullstack.fullstack.security.Actor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Grupos de cada asignatura y matrícula de estudiantes. Crear, editar y matricular: solo ADMIN. */
@Service
@Transactional
public class GrupoService {

    private final GrupoRepository grupoRepository;
    private final MatriculaRepository matriculaRepository;
    private final AsignaturaRepository asignaturaRepository;
    private final EstudianteRepository estudianteRepository;
    private final UsuarioRepository usuarioRepository;
    private final ActividadRepository actividadRepository;
    private final CalificacionRepository calificacionRepository;
    private final Permisos permisos;

    public GrupoService(GrupoRepository grupoRepository,
                        MatriculaRepository matriculaRepository,
                        AsignaturaRepository asignaturaRepository,
                        EstudianteRepository estudianteRepository,
                        UsuarioRepository usuarioRepository,
                        ActividadRepository actividadRepository,
                        CalificacionRepository calificacionRepository,
                        Permisos permisos) {
        this.grupoRepository = grupoRepository;
        this.matriculaRepository = matriculaRepository;
        this.asignaturaRepository = asignaturaRepository;
        this.estudianteRepository = estudianteRepository;
        this.usuarioRepository = usuarioRepository;
        this.actividadRepository = actividadRepository;
        this.calificacionRepository = calificacionRepository;
        this.permisos = permisos;
    }

    /** Grupos que la persona puede ver: ADMIN todos, DOCENTE los suyos, ESTUDIANTE aquellos donde está matriculado. */
    @Transactional(readOnly = true)
    public List<GrupoResponse> listar(Actor actor, Long asignaturaId) {
        List<Grupo> grupos = switch (actor.rol()) {
            case ADMIN -> grupoRepository.listarTodos();
            case DOCENTE -> actor.usuarioId() == null ? List.<Grupo>of() : grupoRepository.listarDelDocente(actor.usuarioId());
            case ESTUDIANTE -> actor.estudianteId() == null ? List.<Grupo>of() : grupoRepository.listarDelEstudiante(actor.estudianteId());
        };
        return grupos.stream()
                .filter(g -> asignaturaId == null || g.getAsignatura().getId().equals(asignaturaId))
                .map(this::aRespuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public GrupoResponse obtener(Long id, Actor actor) {
        Grupo grupo = buscar(id);
        permisos.puedeVerGrupo(grupo, actor);
        return aRespuesta(grupo);
    }

    public GrupoResponse crear(GrupoRequest request) {
        Asignatura asignatura = buscarAsignaturaActiva(request.asignaturaId());
        Usuario docente = buscarDocente(request.docenteId());
        String nombre = request.nombre().trim();
        String periodo = request.periodo().trim();
        if (grupoRepository.existsByAsignaturaIdAndPeriodoAndNombreIgnoreCase(asignatura.getId(), periodo, nombre)) {
            throw new ConflictoException("Ya existe el grupo " + nombre + " de " + asignatura.getNombre() + " en " + periodo);
        }
        Grupo grupo = Grupo.builder().asignatura(asignatura).nombre(nombre).periodo(periodo).docente(docente).build();
        return aRespuesta(grupoRepository.save(grupo));
    }

    public GrupoResponse actualizar(Long id, GrupoRequest request) {
        Grupo grupo = buscar(id);
        Asignatura asignatura = buscarAsignaturaActiva(request.asignaturaId());
        Usuario docente = buscarDocente(request.docenteId());
        String nombre = request.nombre().trim();
        String periodo = request.periodo().trim();
        if (grupoRepository.existsByAsignaturaIdAndPeriodoAndNombreIgnoreCaseAndIdNot(
                asignatura.getId(), periodo, nombre, id)) {
            throw new ConflictoException("Ya existe el grupo " + nombre + " de " + asignatura.getNombre() + " en " + periodo);
        }
        grupo.setAsignatura(asignatura);
        grupo.setNombre(nombre);
        grupo.setPeriodo(periodo);
        grupo.setDocente(docente);
        return aRespuesta(grupoRepository.save(grupo));
    }

    /** Solo se elimina un grupo vacío (sin estudiantes ni actividades), para no perder información académica. */
    public void eliminar(Long id) {
        Grupo grupo = buscar(id);
        if (matriculaRepository.existsByGrupoId(id)) {
            throw new ConflictoException("No se puede eliminar el grupo porque tiene estudiantes matriculados");
        }
        if (actividadRepository.existsByGrupoId(id)) {
            throw new ConflictoException("No se puede eliminar el grupo porque tiene actividades");
        }
        grupoRepository.delete(grupo);
    }

    // ---------- matrículas ----------

    /** Estudiantes del grupo: admin o docente del grupo. */
    @Transactional(readOnly = true)
    public List<MatriculadoResponse> estudiantes(Long grupoId, Actor actor) {
        Grupo grupo = buscar(grupoId);
        permisos.puedeVerDetalleGrupo(grupo, actor);
        return matriculaRepository.findByGrupoIdOrderByEstudianteNombreAsc(grupoId).stream()
                .map(m -> MatriculadoResponse.from(m.getEstudiante()))
                .toList();
    }

    public MatriculadoResponse matricular(Long grupoId, Long estudianteId) {
        Grupo grupo = buscar(grupoId);
        Estudiante estudiante = estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante no encontrado con id " + estudianteId));
        if (matriculaRepository.existsByGrupoIdAndEstudianteId(grupoId, estudianteId)) {
            throw new ConflictoException(estudiante.getNombre() + " ya está matriculado en este grupo");
        }
        matriculaRepository.save(Matricula.builder().grupo(grupo).estudiante(estudiante).build());
        return MatriculadoResponse.from(estudiante);
    }

    /** Quita al estudiante del grupo; no se permite si ya tiene calificaciones en él. */
    public void desmatricular(Long grupoId, Long estudianteId) {
        Matricula matricula = matriculaRepository.findByGrupoIdAndEstudianteId(grupoId, estudianteId)
                .orElseThrow(() -> new ResourceNotFoundException("El estudiante no está matriculado en este grupo"));
        if (calificacionRepository.existeDeEstudianteEnGrupo(estudianteId, grupoId)) {
            throw new ConflictoException("No se puede quitar al estudiante porque ya tiene calificaciones en este grupo");
        }
        matriculaRepository.delete(matricula);
    }

    // ---------- panel ----------

    @Transactional(readOnly = true)
    public PanelResponse panel() {
        return new PanelResponse(estudianteRepository.count(), asignaturaRepository.count(),
                grupoRepository.count(), calificacionRepository.count());
    }

    // ---------- utilidades ----------

    private GrupoResponse aRespuesta(Grupo grupo) {
        return GrupoResponse.from(grupo, matriculaRepository.countByGrupoId(grupo.getId()));
    }

    private Grupo buscar(Long id) {
        return grupoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo no encontrado con id " + id));
    }

    private Asignatura buscarAsignaturaActiva(Long id) {
        Asignatura asignatura = asignaturaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asignatura no encontrada con id " + id));
        if (!asignatura.isActiva()) {
            throw new ReglaNegocioException("La asignatura " + asignatura.getNombre() + " está inactiva");
        }
        return asignatura;
    }

    private Usuario buscarDocente(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id " + id));
        if (usuario.getRol() != Rol.DOCENTE || !usuario.isActivo()) {
            throw new ReglaNegocioException("El usuario " + usuario.getNombre() + " no es un docente activo");
        }
        return usuario;
    }
}
