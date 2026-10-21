package com.fullstack.fullstack.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de integración de punta a punta: arranca la aplicación completa con H2 y los datos de ejemplo
 * (Ingeniería de Software, Grupo 1, con la rúbrica del Parcial 1 calificada para Juan), inicia sesión
 * por la API y comprueba el flujo grupo → actividades → rúbrica → notas y los permisos de cada rol.
 */
@SpringBootTest
class SeguridadIntegracionTest {

    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    // ---------- utilidades ----------

    private String login(String correo, String password) throws Exception {
        String body = mvc.perform(post("/api/auth/login")
                        .contentType(JSON)
                        .content("{\"correo\":\"" + correo + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return extraer(body, "\"token\"\\s*:\\s*\"([^\"]+)\"");
    }

    private String admin() throws Exception {
        return login("admin@gestor.com", "Admin123*");
    }

    private String docente() throws Exception {
        return login("docente@gestor.com", "Docente123*");
    }

    private String juan() throws Exception {
        return login("juan.perez@gestor.com", "Estudiante123*");
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String extraer(String texto, String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(texto);
        assertTrue(matcher.find(), "No se encontró " + regex + " en: " + texto);
        return matcher.group(1);
    }

    /** Objeto de una lista JSON que contiene todos los textos indicados (no depende del orden de los campos). */
    private String objetoCon(String listaJson, String... textos) {
        for (String objeto : listaJson.split("\\},\\s*\\{")) {
            boolean todos = true;
            for (String texto : textos) {
                todos &= objeto.replaceAll("\\s", "").contains(texto.replaceAll("\\s", ""));
            }
            if (todos) {
                return objeto;
            }
        }
        fail("No se encontró un objeto con " + String.join(" y ", textos) + " en: " + listaJson);
        return null;
    }

    private String leer(String ruta, String token) throws Exception {
        return mvc.perform(get(ruta).header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

    /** Id del grupo de ejemplo "Ingeniería de Software - Grupo 1". */
    private String grupoSoftware1(String token) throws Exception {
        String lista = leer("/api/grupos", token);
        return extraer(objetoCon(lista, "\"asignaturaCodigo\":\"ISW101\"", "\"nombre\":\"Grupo 1\""),
                "\"id\"\\s*:\\s*(\\d+)");
    }

    private String grupoSoftware2(String token) throws Exception {
        String lista = leer("/api/grupos", token);
        return extraer(objetoCon(lista, "\"asignaturaCodigo\":\"ISW101\"", "\"nombre\":\"Grupo 2\""),
                "\"id\"\\s*:\\s*(\\d+)");
    }

    private String actividadId(String grupoId, String nombre, String token) throws Exception {
        String lista = leer("/api/grupos/" + grupoId + "/actividades", token);
        return extraer(objetoCon(lista, "\"nombre\":\"" + nombre + "\""), "\"id\"\\s*:\\s*(\\d+)");
    }

    private String estudianteId(String grupoId, String nombre, String token) throws Exception {
        String lista = leer("/api/grupos/" + grupoId + "/estudiantes", token);
        return extraer(objetoCon(lista, "\"nombre\":\"" + nombre + "\""), "\"estudianteId\"\\s*:\\s*(\\d+)");
    }

    // ---------- login ----------

    @Test
    void loginCorrectoDevuelveToken() throws Exception {
        assertTrue(admin().length() > 20);
    }

    @Test
    void loginConContrasenaIncorrectaDevuelve401() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(JSON)
                        .content("{\"correo\":\"admin@gestor.com\",\"password\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void sinTokenLaApiDevuelve401() throws Exception {
        mvc.perform(get("/api/grupos")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/estudiantes")).andExpect(status().isUnauthorized());
    }

    // ---------- permisos por rol ----------

    @Test
    void elEstudianteNoPuedeListarEstudiantes() throws Exception {
        mvc.perform(get("/api/estudiantes").header("Authorization", bearer(juan())))
                .andExpect(status().isForbidden());
    }

    @Test
    void elDocenteNoPuedeCrearEstudiantesNiGrupos() throws Exception {
        String token = docente();
        mvc.perform(post("/api/estudiantes").header("Authorization", bearer(token)).contentType(JSON)
                        .content("{\"documento\":\"777\",\"nombre\":\"X\",\"curso\":\"10A\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/grupos").header("Authorization", bearer(token)).contentType(JSON)
                        .content("{\"asignaturaId\":1,\"nombre\":\"Grupo 9\",\"periodo\":\"2026-2\",\"docenteId\":1}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void elAdministradorCreaEstudianteYUnDocumentoRepetidoDa409() throws Exception {
        String token = admin();
        String cuerpo = "{\"documento\":\"555111\",\"nombre\":\"Laura\",\"curso\":\"10A\"}";
        mvc.perform(post("/api/estudiantes").header("Authorization", bearer(token)).contentType(JSON).content(cuerpo))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/estudiantes").header("Authorization", bearer(token)).contentType(JSON).content(cuerpo))
                .andExpect(status().isConflict());
    }

    // ---------- flujo del docente ----------

    @Test
    void elDocenteVeSusGruposYLaRubricaDelEstudiante() throws Exception {
        String token = docente();
        String lista = leer("/api/grupos", token);
        assertTrue(lista.contains("Ingeniería de Software"));
        assertTrue(lista.contains("Cálculo I"));

        String grupo = grupoSoftware1(token);
        String actividades = leer("/api/grupos/" + grupo + "/actividades", token);
        assertTrue(actividades.contains("Parcial 1") && actividades.contains("Talleres")
                && actividades.contains("Proyecto"));

        String parcial1 = actividadId(grupo, "Parcial 1", token);
        String juanId = estudianteId(grupo, "Juan Pérez", token);
        String rubrica = leer("/api/actividades/" + parcial1 + "/rubrica?estudianteId=" + juanId, token);
        assertTrue(rubrica.contains("Teoría") && rubrica.contains("Sustentación"));
        assertTrue(rubrica.contains("4.6"), "El promedio del Parcial 1 de Juan debe ser 4.60: " + rubrica);

        String estudiantes = leer("/api/actividades/" + parcial1 + "/estudiantes", token);
        assertTrue(estudiantes.contains("Ana López") && estudiantes.contains("Juan Pérez"));
    }

    @Test
    void elDocenteCalificaUnCriterioYLaNotaSeActualiza() throws Exception {
        String token = docente();
        String grupo = grupoSoftware1(token);
        String parcial1 = actividadId(grupo, "Parcial 1", token);
        String anaId = estudianteId(grupo, "Ana López", token);
        String rubrica = leer("/api/actividades/" + parcial1 + "/rubrica?estudianteId=" + anaId, token);
        String practica = extraer(objetoCon(rubrica, "\"nombre\":\"Práctica\""), "\"criterioId\"\\s*:\\s*(\\d+)");

        // Nota fuera de rango: 400
        mvc.perform(put("/api/actividades/" + parcial1 + "/estudiantes/" + anaId + "/calificaciones")
                        .header("Authorization", bearer(token)).contentType(JSON)
                        .content("{\"calificaciones\":[{\"criterioId\":" + practica + ",\"valor\":5.5}]}"))
                .andExpect(status().isBadRequest());

        // Nota válida: 200 y la rúbrica refleja la nota
        String respuesta = mvc.perform(put("/api/actividades/" + parcial1 + "/estudiantes/" + anaId + "/calificaciones")
                        .header("Authorization", bearer(token)).contentType(JSON)
                        .content("{\"calificaciones\":[{\"criterioId\":" + practica + ",\"valor\":4.0}]}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertTrue(respuesta.contains("4.0"), respuesta);

        // Un criterio que no es de la actividad: 400
        mvc.perform(put("/api/actividades/" + parcial1 + "/estudiantes/" + anaId + "/calificaciones")
                        .header("Authorization", bearer(token)).contentType(JSON)
                        .content("{\"calificaciones\":[{\"criterioId\":999999,\"valor\":3.0}]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void soloElDocenteCalifica() throws Exception {
        String token = docente();
        String grupo = grupoSoftware1(token);
        String parcial1 = actividadId(grupo, "Parcial 1", token);
        String juanId = estudianteId(grupo, "Juan Pérez", token);
        String cuerpo = "{\"calificaciones\":[{\"criterioId\":1,\"valor\":3.0}]}";
        String ruta = "/api/actividades/" + parcial1 + "/estudiantes/" + juanId + "/calificaciones";

        mvc.perform(put(ruta).header("Authorization", bearer(admin())).contentType(JSON).content(cuerpo))
                .andExpect(status().isForbidden());
        mvc.perform(put(ruta).header("Authorization", bearer(juan())).contentType(JSON).content(cuerpo))
                .andExpect(status().isForbidden());
    }

    @Test
    void lasActividadesNoPuedenSumarMasDe100YElNombreNoSeRepite() throws Exception {
        String token = docente();
        String grupo2 = grupoSoftware2(token);
        String ruta = "/api/grupos/" + grupo2 + "/actividades";

        mvc.perform(post(ruta).header("Authorization", bearer(token)).contentType(JSON)
                        .content("{\"nombre\":\"Parcial A\",\"peso\":60}"))
                .andExpect(status().isCreated());
        mvc.perform(post(ruta).header("Authorization", bearer(token)).contentType(JSON)
                        .content("{\"nombre\":\"Parcial B\",\"peso\":50}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post(ruta).header("Authorization", bearer(token)).contentType(JSON)
                        .content("{\"nombre\":\"parcial a\",\"peso\":10}"))
                .andExpect(status().isConflict());
    }

    // ---------- el estudiante ----------

    @Test
    void elEstudianteVeSoloSusGruposYSuSeguimiento() throws Exception {
        String token = juan();
        String lista = leer("/api/grupos", token);
        assertTrue(lista.contains("Ingeniería de Software"));
        assertTrue(lista.contains("Matemáticas Discretas"));
        assertFalse(lista.contains("Cálculo I"), "Juan no está matriculado en Cálculo I: " + lista);

        String grupo = grupoSoftware1(token);
        String resumen = leer("/api/grupos/" + grupo + "/resumen", token);
        assertTrue(resumen.contains("4.6"), "Promedio de lo evaluado: " + resumen);
        assertTrue(resumen.contains("1.38"), "Acumulado 4.60 x 30 %: " + resumen);
    }

    @Test
    void elEstudianteNoVeLosDatosDeOtrosNiLosDeOtroGrupo() throws Exception {
        String tokenDocente = docente();
        String grupo = grupoSoftware1(tokenDocente);
        String anaId = estudianteId(grupo, "Ana López", tokenDocente);
        String token = juan();

        mvc.perform(get("/api/grupos/" + grupo + "/resumen?estudianteId=" + anaId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/grupos/" + grupo + "/estudiantes").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/grupos/" + grupo + "/progreso").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/grupos/" + grupo + "/actividades").header("Authorization", bearer(token))
                        .contentType(JSON).content("{\"nombre\":\"Hack\",\"peso\":1}"))
                .andExpect(status().isForbidden());

        // Pedro está en el Grupo 2 de Ingeniería de Software: Juan no puede entrar
        String grupo2 = grupoSoftware2(tokenDocente);
        mvc.perform(get("/api/grupos/" + grupo2 + "/actividades").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    // ---------- el administrador: grupos y matrículas ----------

    @Test
    void elAdministradorCreaGrupoYMatriculaSinRepetirEstudiantes() throws Exception {
        String token = admin();
        String asignaturas = leer("/api/asignaturas", token);
        String asignatura = extraer(objetoCon(asignaturas, "\"codigo\":\"CAL101\""), "\"id\"\\s*:\\s*(\\d+)");
        String usuarios = leer("/api/usuarios", token);
        String docenteId = extraer(objetoCon(usuarios, "\"correo\":\"docente@gestor.com\""), "\"id\"\\s*:\\s*(\\d+)");
        String estudiantes = leer("/api/estudiantes", token);
        String pedro = extraer(objetoCon(estudiantes, "\"nombre\":\"Pedro Gómez\""), "\"id\"\\s*:\\s*(\\d+)");

        String cuerpo = "{\"asignaturaId\":" + asignatura + ",\"nombre\":\"Grupo 7\",\"periodo\":\"2027-1\",\"docenteId\":"
                + docenteId + "}";
        String creado = mvc.perform(post("/api/grupos").header("Authorization", bearer(token)).contentType(JSON)
                        .content(cuerpo))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String grupoId = extraer(creado, "\"id\"\\s*:\\s*(\\d+)");

        mvc.perform(post("/api/grupos").header("Authorization", bearer(token)).contentType(JSON).content(cuerpo))
                .andExpect(status().isConflict());

        mvc.perform(post("/api/grupos/" + grupoId + "/estudiantes/" + pedro).header("Authorization", bearer(token)))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/grupos/" + grupoId + "/estudiantes/" + pedro).header("Authorization", bearer(token)))
                .andExpect(status().isConflict());

        // Con estudiantes matriculados el grupo no se puede eliminar; vacío sí
        mvc.perform(delete("/api/grupos/" + grupoId).header("Authorization", bearer(token)))
                .andExpect(status().isConflict());
        mvc.perform(delete("/api/grupos/" + grupoId + "/estudiantes/" + pedro).header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());
        mvc.perform(delete("/api/grupos/" + grupoId).header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());
    }

    @Test
    void elPanelSoloLoVenAdminYDocente() throws Exception {
        mvc.perform(get("/api/panel").header("Authorization", bearer(admin()))).andExpect(status().isOk());
        mvc.perform(get("/api/panel").header("Authorization", bearer(docente()))).andExpect(status().isOk());
        mvc.perform(get("/api/panel").header("Authorization", bearer(juan()))).andExpect(status().isForbidden());
    }
}
