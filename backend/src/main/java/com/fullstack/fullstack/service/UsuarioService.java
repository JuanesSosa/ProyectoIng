package com.fullstack.fullstack.service;

import com.fullstack.fullstack.dto.UsuarioDtos.UsuarioRequest;
import com.fullstack.fullstack.dto.UsuarioDtos.UsuarioResponse;
import com.fullstack.fullstack.exception.ConflictoException;
import com.fullstack.fullstack.exception.ReglaNegocioException;
import com.fullstack.fullstack.exception.ResourceNotFoundException;
import com.fullstack.fullstack.model.entity.Estudiante;
import com.fullstack.fullstack.model.entity.Rol;
import com.fullstack.fullstack.model.entity.Usuario;
import com.fullstack.fullstack.repository.EstudianteRepository;
import com.fullstack.fullstack.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

/** Gestión de usuarios (RF-02, solo administrador). Los usuarios se desactivan, no se borran. */
@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EstudianteRepository estudianteRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          EstudianteRepository estudianteRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.estudianteRepository = estudianteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAllByOrderByNombreAsc().stream().map(UsuarioResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long id) {
        return UsuarioResponse.from(buscar(id));
    }

    public UsuarioResponse crear(UsuarioRequest request) {
        if (request.password() == null || request.password().isBlank()) {
            throw new ReglaNegocioException("La contraseña es obligatoria al crear un usuario");
        }
        String correo = request.correo().trim().toLowerCase(Locale.ROOT);
        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw new ConflictoException("Ya existe un usuario con el correo " + correo);
        }
        Usuario usuario = Usuario.builder()
                .nombre(request.nombre().trim())
                .correo(correo)
                .password(passwordEncoder.encode(request.password()))
                .rol(request.rol())
                .activo(request.activo() == null || request.activo())
                .estudiante(resolverEstudiante(request))
                .build();
        return UsuarioResponse.from(usuarioRepository.save(usuario));
    }

    /**
     * Edita un usuario. Si la contraseña viene vacía se conserva la actual.
     *
     * @param correoActual correo del administrador que hace el cambio: no puede quitarse su propio rol de
     *                     administrador ni desactivarse, para no quedar fuera del sistema.
     */
    public UsuarioResponse actualizar(Long id, UsuarioRequest request, String correoActual) {
        Usuario usuario = buscar(id);
        String correo = request.correo().trim().toLowerCase(Locale.ROOT);
        if (usuarioRepository.existsByCorreoIgnoreCaseAndIdNot(correo, id)) {
            throw new ConflictoException("Ya existe otro usuario con el correo " + correo);
        }
        boolean activo = request.activo() == null ? usuario.isActivo() : request.activo();
        if (esElMismo(usuario, correoActual) && (request.rol() != Rol.ADMIN || !activo)) {
            throw new ReglaNegocioException("No puedes quitarte el rol de administrador ni desactivar tu propia cuenta");
        }

        usuario.setNombre(request.nombre().trim());
        usuario.setCorreo(correo);
        usuario.setRol(request.rol());
        usuario.setActivo(activo);
        usuario.setEstudiante(resolverEstudiante(request));
        if (request.password() != null && !request.password().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(request.password()));
        }
        return UsuarioResponse.from(usuarioRepository.save(usuario));
    }

    /** "Eliminar" un usuario lo desactiva: ya no puede iniciar sesión, pero su registro se conserva. */
    public void desactivar(Long id, String correoActual) {
        Usuario usuario = buscar(id);
        if (esElMismo(usuario, correoActual)) {
            throw new ReglaNegocioException("No puedes desactivar tu propia cuenta");
        }
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }

    /** El rol ESTUDIANTE exige un estudiante existente; los demás roles no se vinculan a ninguno. */
    private Estudiante resolverEstudiante(UsuarioRequest request) {
        if (request.rol() != Rol.ESTUDIANTE) {
            return null;
        }
        if (request.estudianteId() == null) {
            throw new ReglaNegocioException("Un usuario con rol ESTUDIANTE debe estar asociado a un estudiante");
        }
        return estudianteRepository.findById(request.estudianteId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Estudiante no encontrado con id " + request.estudianteId()));
    }

    private boolean esElMismo(Usuario usuario, String correoActual) {
        return correoActual != null && usuario.getCorreo().equalsIgnoreCase(correoActual);
    }

    private Usuario buscar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id " + id));
    }
}
