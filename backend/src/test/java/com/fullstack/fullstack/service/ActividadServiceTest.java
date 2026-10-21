package com.fullstack.fullstack.service;

import com.fullstack.fullstack.dto.ActividadDtos.ActividadRequest;
import com.fullstack.fullstack.dto.ActividadDtos.CriterioRequest;
import com.fullstack.fullstack.dto.ActividadDtos.RubricaRequest;
import com.fullstack.fullstack.exception.AccesoDenegadoException;
import com.fullstack.fullstack.exception.ConflictoException;
import com.fullstack.fullstack.exception.ReglaNegocioException;
import com.fullstack.fullstack.model.entity.Actividad;
import com.fullstack.fullstack.model.entity.Criterio;
import com.fullstack.fullstack.model.entity.Grupo;
import com.fullstack.fullstack.model.entity.Rol;
import com.fullstack.fullstack.model.entity.Usuario;
import com.fullstack.fullstack.repository.ActividadRepository;
import com.fullstack.fullstack.repository.CalificacionRepository;
import com.fullstack.fullstack.repository.CriterioRepository;
import com.fullstack.fullstack.repository.GrupoRepository;
import com.fullstack.fullstack.repository.MatriculaRepository;
import com.fullstack.fullstack.security.Actor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Reglas de actividades y rúbricas: pesos, nombres únicos, dueño del grupo y notas que no se pierden. */
@ExtendWith(MockitoExtension.class)
class ActividadServiceTest {

    @Mock
    private ActividadRepository actividadRepository;
    @Mock
    private CriterioRepository criterioRepository;
    @Mock
    private CalificacionRepository calificacionRepository;
    @Mock
    private GrupoRepository grupoRepository;
    @Mock
    private MatriculaRepository matriculaRepository;

    private ActividadService service;

    private final Usuario docente = Usuario.builder().id(7L).nombre("Docente").rol(Rol.DOCENTE).build();
    private final Grupo grupo = Grupo.builder().id(1L).nombre("Grupo 1").docente(docente).build();
    private final Actor actorDocente = new Actor(7L, Rol.DOCENTE, null);

    @BeforeEach
    void setUp() {
        service = new ActividadService(actividadRepository, criterioRepository, calificacionRepository,
                grupoRepository, new Permisos(matriculaRepository));
    }

    private Actividad actividad(long id, String nombre, int peso) {
        return Actividad.builder().id(id).grupo(grupo).nombre(nombre).peso(peso).build();
    }

    @Test
    void noPermiteQueLasActividadesSumenMasDe100() {
        when(grupoRepository.findById(1L)).thenReturn(Optional.of(grupo));
        when(actividadRepository.findByGrupoIdOrderByIdAsc(1L))
                .thenReturn(List.of(actividad(10L, "Parcial 1", 60), actividad(11L, "Parcial 2", 30)));

        assertThrows(ReglaNegocioException.class,
                () -> service.crear(1L, new ActividadRequest("Proyecto", 20), actorDocente));
        verify(actividadRepository, never()).save(any());
    }

    @Test
    void noPermiteDosActividadesConElMismoNombre() {
        when(grupoRepository.findById(1L)).thenReturn(Optional.of(grupo));
        when(actividadRepository.findByGrupoIdOrderByIdAsc(1L)).thenReturn(List.of(actividad(10L, "Parcial 1", 30)));

        assertThrows(ConflictoException.class,
                () -> service.crear(1L, new ActividadRequest(" parcial 1 ", 10), actorDocente));
    }

    @Test
    void soloElDocenteDelGrupoPuedeCrearActividades() {
        when(grupoRepository.findById(1L)).thenReturn(Optional.of(grupo));

        assertThrows(AccesoDenegadoException.class,
                () -> service.crear(1L, new ActividadRequest("Parcial", 30), new Actor(99L, Rol.DOCENTE, null)));
        assertThrows(AccesoDenegadoException.class,
                () -> service.crear(1L, new ActividadRequest("Parcial", 30), new Actor(1L, Rol.ADMIN, null)));
    }

    @Test
    void creaLaActividadCuandoCumpleLasReglas() {
        when(grupoRepository.findById(1L)).thenReturn(Optional.of(grupo));
        when(actividadRepository.findByGrupoIdOrderByIdAsc(1L)).thenReturn(List.of(actividad(10L, "Parcial 1", 30)));
        when(actividadRepository.save(any(Actividad.class))).thenAnswer(i -> i.getArgument(0));

        var respuesta = service.crear(1L, new ActividadRequest(" Taller ", 70), actorDocente);

        assertEquals("Taller", respuesta.nombre());
        assertEquals(70, respuesta.peso());
    }

    @Test
    void laRubricaNoPuedeSumarMasDe100() {
        when(actividadRepository.findById(10L)).thenReturn(Optional.of(actividad(10L, "Parcial 1", 30)));

        var request = new RubricaRequest(List.of(
                new CriterioRequest(null, "Teoría", 60), new CriterioRequest(null, "Práctica", 50)));
        assertThrows(ReglaNegocioException.class, () -> service.guardarRubrica(10L, request, actorDocente));
        verify(criterioRepository, never()).saveAll(any());
    }

    @Test
    void laRubricaNoAdmiteCriteriosRepetidos() {
        when(actividadRepository.findById(10L)).thenReturn(Optional.of(actividad(10L, "Parcial 1", 30)));

        var request = new RubricaRequest(List.of(
                new CriterioRequest(null, "Teoría", 50), new CriterioRequest(null, " teoría ", 50)));
        assertThrows(ReglaNegocioException.class, () -> service.guardarRubrica(10L, request, actorDocente));
    }

    @Test
    void noSePuedeQuitarUnCriterioQueYaTieneCalificaciones() {
        Actividad actividad = actividad(10L, "Parcial 1", 30);
        Criterio teoria = Criterio.builder().id(100L).actividad(actividad).nombre("Teoría").peso(50).build();
        Criterio practica = Criterio.builder().id(101L).actividad(actividad).nombre("Práctica").peso(50).build();
        when(actividadRepository.findById(10L)).thenReturn(Optional.of(actividad));
        when(criterioRepository.findByActividadIdOrderByIdAsc(10L)).thenReturn(List.of(teoria, practica));
        when(calificacionRepository.existeEnCriterio(101L)).thenReturn(true);

        // La nueva rúbrica solo conserva "Teoría": "Práctica" tiene notas, no se puede quitar
        var request = new RubricaRequest(List.of(new CriterioRequest(100L, "Teoría", 100)));
        assertThrows(ConflictoException.class, () -> service.guardarRubrica(10L, request, actorDocente));
        verify(criterioRepository, never()).deleteAll(any());
    }

    @Test
    void noSePuedeEliminarUnaActividadConCalificaciones() {
        when(actividadRepository.findById(10L)).thenReturn(Optional.of(actividad(10L, "Parcial 1", 30)));
        when(calificacionRepository.existeEnActividad(10L)).thenReturn(true);

        assertThrows(ConflictoException.class, () -> service.eliminar(10L, actorDocente));
        verify(actividadRepository, never()).delete(any());
    }
}
