package com.fullstack.fullstack.service;

import com.fullstack.fullstack.dto.EstudianteDtos.EstudianteRequest;
import com.fullstack.fullstack.exception.ConflictoException;
import com.fullstack.fullstack.exception.ResourceNotFoundException;
import com.fullstack.fullstack.model.entity.Estudiante;
import com.fullstack.fullstack.repository.EstudianteRepository;
import com.fullstack.fullstack.repository.NotaRepository;
import com.fullstack.fullstack.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Pruebas del servicio de estudiantes: documento único y protección de estudiantes con notas. */
@ExtendWith(MockitoExtension.class)
class EstudianteServiceTest {

    @Mock
    private EstudianteRepository estudianteRepository;

    @Mock
    private NotaRepository notaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private EstudianteService service;

    private final Estudiante juan = Estudiante.builder().id(1L).documento("123456").nombre("Juan").curso("10A").build();

    @Test
    void noPermiteCrearDosEstudiantesConElMismoDocumento() {
        when(estudianteRepository.existsByDocumento("123456")).thenReturn(true);

        assertThrows(ConflictoException.class,
                () -> service.crear(new EstudianteRequest(" 123456 ", "Otro", "10B")));
        verify(estudianteRepository, never()).save(any());
    }

    @Test
    void noPermiteEliminarUnEstudianteConNotas() {
        when(estudianteRepository.findById(1L)).thenReturn(Optional.of(juan));
        when(notaRepository.existsByEstudianteId(1L)).thenReturn(true);

        assertThrows(ConflictoException.class, () -> service.eliminar(1L));
        verify(estudianteRepository, never()).delete(any());
    }

    @Test
    void eliminaUnEstudianteSinNotasNiUsuario() {
        when(estudianteRepository.findById(1L)).thenReturn(Optional.of(juan));
        when(notaRepository.existsByEstudianteId(1L)).thenReturn(false);
        when(usuarioRepository.existsByEstudianteId(1L)).thenReturn(false);

        service.eliminar(1L);

        verify(estudianteRepository).delete(juan);
    }

    @Test
    void eliminarUnEstudianteInexistenteDaNotFound() {
        when(estudianteRepository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.eliminar(9L));
    }
}
