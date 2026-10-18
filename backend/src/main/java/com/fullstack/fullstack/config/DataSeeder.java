package com.fullstack.fullstack.config;

import com.fullstack.fullstack.model.entity.Asignatura;
import com.fullstack.fullstack.model.entity.Estudiante;
import com.fullstack.fullstack.model.entity.Nota;
import com.fullstack.fullstack.model.entity.Rol;
import com.fullstack.fullstack.model.entity.Usuario;
import com.fullstack.fullstack.repository.AsignaturaRepository;
import com.fullstack.fullstack.repository.EstudianteRepository;
import com.fullstack.fullstack.repository.NotaRepository;
import com.fullstack.fullstack.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Carga datos de ejemplo (los de los mock-ups) la primera vez que se arranca con la base de datos vacía,
 * para poder iniciar sesión y probar el sistema. Solo se ejecuta si app.seed.enabled=true y no hay usuarios.
 *
 * ¡Solo para desarrollo! Las contraseñas de ejemplo son públicas: en un despliegue real
 * desactiva el seeder (SEED_ENABLED=false) y crea los usuarios reales desde el rol administrador.
 */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private static final String PERIODO = "2026-2";

    private final UsuarioRepository usuarioRepository;
    private final EstudianteRepository estudianteRepository;
    private final AsignaturaRepository asignaturaRepository;
    private final NotaRepository notaRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UsuarioRepository usuarioRepository,
                      EstudianteRepository estudianteRepository,
                      AsignaturaRepository asignaturaRepository,
                      NotaRepository notaRepository,
                      PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.estudianteRepository = estudianteRepository;
        this.asignaturaRepository = asignaturaRepository;
        this.notaRepository = notaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            return;
        }

        Estudiante juan = estudianteRepository.save(
                Estudiante.builder().documento("123456").nombre("Juan Pérez").curso("10A").build());
        estudianteRepository.save(Estudiante.builder().documento("456789").nombre("Ana López").curso("10B").build());
        estudianteRepository.save(Estudiante.builder().documento("987654").nombre("Pedro Gómez").curso("11A").build());

        Asignatura matematicas = asignaturaRepository.save(
                Asignatura.builder().codigo("MAT101").nombre("Matemáticas").build());
        Asignatura ingles = asignaturaRepository.save(
                Asignatura.builder().codigo("ING101").nombre("Inglés").build());
        Asignatura programacion = asignaturaRepository.save(
                Asignatura.builder().codigo("PRG101").nombre("Programación").build());

        usuarioRepository.save(usuario("Administrador", "admin@gestor.com", "Admin123*", Rol.ADMIN, null));
        usuarioRepository.save(usuario("Docente de prueba", "docente@gestor.com", "Docente123*", Rol.DOCENTE, null));
        usuarioRepository.save(usuario("Juan Pérez", "juan.perez@gestor.com", "Estudiante123*", Rol.ESTUDIANTE, juan));

        // Matemáticas con pesos 30/30/40 (mock-up 5.4); Inglés y Programación sin pesos (promedio simple)
        notaRepository.save(nota(juan, matematicas, "Parcial", 30, "4.2"));
        notaRepository.save(nota(juan, matematicas, "Taller", 30, "3.8"));
        notaRepository.save(nota(juan, matematicas, "Final", 40, "4.5"));
        notaRepository.save(nota(juan, ingles, "Nota 1", null, "4.0"));
        notaRepository.save(nota(juan, ingles, "Nota 2", null, "4.3"));
        notaRepository.save(nota(juan, ingles, "Nota 3", null, "4.1"));
        notaRepository.save(nota(juan, programacion, "Nota 1", null, "4.5"));
        notaRepository.save(nota(juan, programacion, "Nota 2", null, "4.7"));
        notaRepository.save(nota(juan, programacion, "Nota 3", null, "4.6"));

        log.info("Datos de ejemplo cargados. Usuarios: admin@gestor.com / Admin123*, "
                + "docente@gestor.com / Docente123*, juan.perez@gestor.com / Estudiante123*");
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

    private Nota nota(Estudiante estudiante, Asignatura asignatura, String evaluacion, Integer peso, String valor) {
        return Nota.builder()
                .estudiante(estudiante)
                .asignatura(asignatura)
                .periodo(PERIODO)
                .evaluacion(evaluacion)
                .peso(peso)
                .valor(new BigDecimal(valor).setScale(2))
                .build();
    }
}
