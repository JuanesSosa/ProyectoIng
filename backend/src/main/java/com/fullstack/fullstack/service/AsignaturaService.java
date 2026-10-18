package com.fullstack.fullstack.service;

import com.fullstack.fullstack.dto.AsignaturaDtos.AsignaturaRequest;
import com.fullstack.fullstack.dto.AsignaturaDtos.AsignaturaResponse;
import com.fullstack.fullstack.exception.ConflictoException;
import com.fullstack.fullstack.exception.ResourceNotFoundException;
import com.fullstack.fullstack.model.entity.Asignatura;
import com.fullstack.fullstack.repository.AsignaturaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

/** Gestión del catálogo de asignaturas (RF-06, HU-03). Las asignaturas se desactivan, no se borran. */
@Service
@Transactional
public class AsignaturaService {

    private final AsignaturaRepository asignaturaRepository;

    public AsignaturaService(AsignaturaRepository asignaturaRepository) {
        this.asignaturaRepository = asignaturaRepository;
    }

    @Transactional(readOnly = true)
    public List<AsignaturaResponse> listar(boolean soloActivas) {
        List<Asignatura> asignaturas = soloActivas
                ? asignaturaRepository.findAllByActivaTrueOrderByNombreAsc()
                : asignaturaRepository.findAllByOrderByNombreAsc();
        return asignaturas.stream().map(AsignaturaResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public AsignaturaResponse obtener(Long id) {
        return AsignaturaResponse.from(buscar(id));
    }

    public AsignaturaResponse crear(AsignaturaRequest request) {
        String codigo = normalizarCodigo(request.codigo());
        if (asignaturaRepository.existsByCodigoIgnoreCase(codigo)) {
            throw new ConflictoException("Ya existe una asignatura con el código " + codigo);
        }
        Asignatura asignatura = Asignatura.builder()
                .codigo(codigo)
                .nombre(request.nombre().trim())
                .activa(request.activa() == null || request.activa())
                .build();
        return AsignaturaResponse.from(asignaturaRepository.save(asignatura));
    }

    public AsignaturaResponse actualizar(Long id, AsignaturaRequest request) {
        Asignatura asignatura = buscar(id);
        String codigo = normalizarCodigo(request.codigo());
        if (asignaturaRepository.existsByCodigoIgnoreCaseAndIdNot(codigo, id)) {
            throw new ConflictoException("Ya existe otra asignatura con el código " + codigo);
        }
        asignatura.setCodigo(codigo);
        asignatura.setNombre(request.nombre().trim());
        if (request.activa() != null) {
            asignatura.setActiva(request.activa());
        }
        return AsignaturaResponse.from(asignaturaRepository.save(asignatura));
    }

    /** "Eliminar" una asignatura la desactiva: sus notas históricas se conservan. */
    public void desactivar(Long id) {
        Asignatura asignatura = buscar(id);
        asignatura.setActiva(false);
        asignaturaRepository.save(asignatura);
    }

    private Asignatura buscar(Long id) {
        return asignaturaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asignatura no encontrada con id " + id));
    }

    private String normalizarCodigo(String codigo) {
        return codigo.trim().toUpperCase(Locale.ROOT);
    }
}
