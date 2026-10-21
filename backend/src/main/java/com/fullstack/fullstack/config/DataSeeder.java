package com.fullstack.fullstack.config;

import com.fullstack.fullstack.model.entity.Actividad;
import com.fullstack.fullstack.model.entity.Asignatura;
import com.fullstack.fullstack.model.entity.Calificacion;
import com.fullstack.fullstack.model.entity.Criterio;
import com.fullstack.fullstack.model.entity.Estudiante;
import com.fullstack.fullstack.model.entity.Grupo;
import com.fullstack.fullstack.model.entity.Matricula;
import com.fullstack.fullstack.model.entity.Rol;
import com.fullstack.fullstack.model.entity.Usuario;
import com.fullstack.fullstack.repository.ActividadRepository;
import com.fullstack.fullstack.repository.AsignaturaRepository;
import com.fullstack.fullstack.repository.CalificacionRepository;
import com.fullstack.fullstack.repository.CriterioRepository;
import com.fullstack.fullstack.repository.EstudianteRepository;
import com.fullstack.fullstack.repository.GrupoRepository;
import com.fullstack.fullstack.repository.MatriculaRepository;
import com.fullstack.fullstack.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Carga datos de ejemplo para poder iniciar sesión y probar el sistema. Solo se ejecuta si
 * app.seed.enabled=true: crea los 3 usuarios si no hay ninguno y, si no hay grupos, un ejemplo académico
 * (Ingeniería de Software con Grupo 1 y Grupo 2, rúbrica del Parcial 1 y notas de Juan).
 *
 * ¡Solo para desarrollo! Las contraseñas de ejemplo son públicas: en un despliegue real
 * desactiva el seeder (SEED_ENABLED=false) y crea los usuarios reales desde el rol administrador.
 */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private static final String PERIODO = "2026-2";
    private static final String CORREO_DOCENTE = "docente@gestor.com";

    private final UsuarioRepository usuarioRepository;
    private final EstudianteRepository estudianteRepository;
    private final AsignaturaRepository asignaturaRepository;
    private final GrupoRepository grupoRepository;
    private final MatriculaRepository matriculaRepository;
    private final ActividadRepository actividadRepository;
    private final CriterioRepository criterioRepository;
    private final CalificacionRepository calificacionRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UsuarioRepository usuarioRepository,
                      EstudianteRepository estudianteRepository,
                      AsignaturaRepository asignaturaRepository,
                      GrupoRepository grupoRepository,
                      MatriculaRepository matriculaRepository,
                      ActividadRepository actividadRepository,
                      CriterioRepository criterioRepository,
                      CalificacionRepository calificacionRepository,
                      PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.estudianteRepository = estudianteRepository;
        this.asignaturaRepository = asignaturaRepository;
        this.grupoRepository = grupoRepository;
        this.matriculaRepository = matriculaRepository;
        this.actividadRepository = actividadRepository;
        this.criterioRepository = criterioRepository;
        this.calificacionRepository = calificacionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarioRepository.count() == 0) {
            Estudiante juan = estudiante("123456", "Juan Pérez", "10A");
            usuarioRepository.save(usuario("Administrador", "admin@gestor.com", "Admin123*", Rol.ADMIN, null));
            usuarioRepository.save(usuario("Docente de prueba", CORREO_DOCENTE, "Docente123*", Rol.DOCENTE, null));
            usuarioRepository.save(usuario("Juan Pérez", "juan.perez@gestor.com", "Estudiante123*", Rol.ESTUDIANTE, juan));
            log.info("Usuarios de ejemplo creados: admin@gestor.com / Admin123*, "
                    + "docente@gestor.com / Docente123*, juan.perez@gestor.com / Estudiante123*");
        }
        if (grupoRepository.count() == 0) {
            sembrarAcademico();
        }
    }

    /** Ejemplo del boceto: Ingeniería de Software (Grupo 1 y 2), Matemáticas Discretas y Cálculo I. */
    private void sembrarAcademico() {
        Optional<Usuario> docente = usuarioRepository.findByCorreoIgnoreCase(CORREO_DOCENTE);
        if (docente.isEmpty()) {
            log.info("No se cargó el ejemplo académico: no existe el usuario {}", CORREO_DOCENTE);
            return;
        }
        Usuario profe = docente.get();
        Estudiante juan = estudiante("123456", "Juan Pérez", "10A");
        Estudiante ana = estudiante("456789", "Ana López", "10B");
        Estudiante pedro = estudiante("987654", "Pedro Gómez", "11A");

        Asignatura software = asignatura("ISW101", "Ingeniería de Software");
        Asignatura discretas = asignatura("MDI101", "Matemáticas Discretas");
        Asignatura calculo = asignatura("CAL101", "Cálculo I");

        Grupo softwareG1 = grupoRepository.save(grupo(software, "Grupo 1", profe));
        Grupo softwareG2 = grupoRepository.save(grupo(software, "Grupo 2", profe));
        Grupo discretasG1 = grupoRepository.save(grupo(discretas, "Grupo 1", profe));
        grupoRepository.save(grupo(calculo, "Grupo 1", profe));

        matricular(softwareG1, juan);
        matricular(softwareG1, ana);
        matricular(softwareG2, pedro);
        matricular(discretasG1, juan);

        // Ingeniería de Software, Grupo 1: Parcial 1 (30 %), Parcial 2 (30 %), Talleres (30 %), Proyecto (10 %)
        Actividad parcial1 = actividad(softwareG1, "Parcial 1", 30);
        Criterio teoria = criterio(parcial1, "Teoría", 50);
        Criterio practica = criterio(parcial1, "Práctica", 30);
        Criterio sustentacion = criterio(parcial1, "Sustentación", 20);
        Actividad parcial2 = actividad(softwareG1, "Parcial 2", 30);
        criterio(parcial2, "Teoría", 50);
        criterio(parcial2, "Práctica", 50);
        Actividad talleres = actividad(softwareG1, "Talleres", 30);
        criterio(talleres, "Taller 1", 50);
        criterio(talleres, "Taller 2", 50);
        Actividad proyecto = actividad(softwareG1, "Proyecto", 10);
        criterio(proyecto, "Entrega", 60);
        criterio(proyecto, "Sustentación", 40);

        // Juan completó el Parcial 1: 5.0 x 50 % + 4.0 x 30 % + 4.5 x 20 % = 4.60. Ana solo tiene la teoría.
        calificar(juan, teoria, "5.0");
        calificar(juan, practica, "4.0");
        calificar(juan, sustentacion, "4.5");
        calificar(ana, teoria, "3.5");

        // Matemáticas Discretas, Grupo 1
        Actividad parcialDiscretas = actividad(discretasG1, "Parcial 1", 50);
        criterio(parcialDiscretas, "Teoría", 60);
        criterio(parcialDiscretas, "Práctica", 40);

        log.info("Ejemplo académico cargado (periodo {}).", PERIODO);
    }

    private Estudiante estudiante(String documento, String nombre, String curso) {
        return estudianteRepository.findByDocumento(documento).orElseGet(() -> estudianteRepository.save(
                Estudiante.builder().documento(documento).nombre(nombre).curso(curso).build()));
    }

    private Asignatura asignatura(String codigo, String nombre) {
        return asignaturaRepository.findByCodigoIgnoreCase(codigo).orElseGet(() -> asignaturaRepository.save(
                Asignatura.builder().codigo(codigo).nombre(nombre).build()));
    }

    private Grupo grupo(Asignatura asignatura, String nombre, Usuario docente) {
        return Grupo.builder().asignatura(asignatura).nombre(nombre).periodo(PERIODO).docente(docente).build();
    }

    private void matricular(Grupo grupo, Estudiante estudiante) {
        matriculaRepository.save(Matricula.builder().grupo(grupo).estudiante(estudiante).build());
    }

    private Actividad actividad(Grupo grupo, String nombre, int peso) {
        return actividadRepository.save(Actividad.builder().grupo(grupo).nombre(nombre).peso(peso).build());
    }

    private Criterio criterio(Actividad actividad, String nombre, int peso) {
        return criterioRepository.save(Criterio.builder().actividad(actividad).nombre(nombre).peso(peso).build());
    }

    private void calificar(Estudiante estudiante, Criterio criterio, String valor) {
        calificacionRepository.save(Calificacion.builder()
                .estudiante(estudiante)
                .criterio(criterio)
                .valor(new BigDecimal(valor).setScale(2))
                .build());
    }

    private Usuario usuario(String nombre, String correo, String password, Rol rol, Estudiante estudiante) {
        return Usuario.builder()
                .nombre(nombre)
                .correo(correo)
                .password(passwordEncoder.encode(password))
                .rol(rol)
                .estudiante(estudiante)
                .build();
    }
}
