package com.fullstack.fullstack.service;

import com.fullstack.fullstack.dto.EstudianteDtos.EstudianteRequest;
import com.fullstack.fullstack.dto.EstudianteDtos.EstudianteResponse;
import com.fullstack.fullstack.exception.ConflictoException;
import com.fullstack.fullstack.exception.ResourceNotFoundException;
import com.fullstack.fullstack.model.entity.Estudiante;
import com.fullstack.fullstack.repository.EstudianteRepository;
import com.fullstack.fullstack.repository.NotaRepository;
import com.fullstack.fullstack.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Gestión de estudiantes (RF-03, RF-04, RF-05, HU-02, HU-08). */
@Service
@Transactional
public class EstudianteService {

    private final EstudianteRepository estudianteRepository;
    private final NotaRepository notaRepository;
    private final UsuarioRepository usuarioRepository;

    public EstudianteService(EstudianteRepository estudianteRepository,
                             NotaRepository notaRepository,
                             UsuarioRepository usuarioRepository) {
        this.estudianteRepository = estudianteRepository;
        this.notaRepository = notaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /** Lista todos los estudiantes o, si llega texto, los que coincidan por nombre o documento. */
    @Transactional(readOnly = true)
    public List<EstudianteResponse> listar(String texto) {
        List<Estudiante> estudiantes;
        if (texto == null || texto.isBlank()) {
            estudiantes = estudianteRepository.findAllByOrderByNombreAsc();
        } else {
            String q = texto.trim();
            estudiantes = estudianteRepository
                    .findByNombreContainingIgnoreCaseOrDocumentoContainingIgnoreCaseOrderByNombreAsc(q, q);
        }
        return estudiantes.stream().map(EstudianteResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public EstudianteResponse obtener(Long id) {
        return EstudianteResponse.from(buscar(id));
    }

    public EstudianteResponse crear(EstudianteRequest request) {
        String documento = request.documento().trim();
        if (estudianteRepository.existsByDocumento(documento)) {
            throw new ConflictoException("Ya existe un estudiante con el documento " + documento);
        }
        Estudiante estudiante = Estudiante.builder()
                .documento(documento)
                .nombre(request.nombre().trim())
                .curso(request.curso().trim())
                .build();
        return EstudianteResponse.from(estudianteRepository.save(estudiante));
    }

    public EstudianteResponse actualizar(Long id, EstudianteRequest request) {
        Estudiante estudiante = buscar(id);
        String documento = request.documento().trim();
        if (estudianteRepository.existsByDocumentoAndIdNot(documento, id)) {
            throw new ConflictoException("Ya existe otro estudiante con el documento " + documento);
        }
        estudiante.setDocumento(documento);
        estudiante.setNombre(request.nombre().trim());
        estudiante.setCurso(request.curso().trim());
        return EstudianteResponse.from(estudianteRepository.save(estudiante));
    }

    /** Elimina un estudiante. Si tiene notas o un usuario vinculado no se permite, para no perder información académica. */
    public void eliminar(Long id) {
        Estudiante estudiante = buscar(id);
        if (notaRepository.existsByEstudianteId(id)) {
            throw new ConflictoException("No se puede eliminar a " + estudiante.getNombre() + " porque tiene notas registradas");
        }
        if (usuarioRepository.existsByEstudianteId(id)) {
            throw new ConflictoException("No se puede eliminar a " + estudiante.getNombre() + " porque tiene un usuario asociado");
        }
        estudianteRepository.delete(estudiante);
    }

    private Estudiante buscar(Long id) {
        return estudianteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante no encontrado con id " + id));
    }
}
