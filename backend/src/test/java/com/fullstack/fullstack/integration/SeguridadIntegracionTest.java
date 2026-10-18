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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de integración de punta a punta: arranca la aplicación completa con H2 y los datos de ejemplo,
 * inicia sesión por la API y comprueba que cada rol solo accede a lo que le corresponde (RNF-01, RNF-08).
 */
@SpringBootTest
class SeguridadIntegracionTest {

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

    private String loginJson(String correo, String password) {
        return "{\"correo\":\"" + correo + "\",\"password\":\"" + password + "\"}";
    }

    private String login(String correo, String password) throws Exception {
        String body = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(correo, password)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return extraer(body, "\"token\"\\s*:\\s*\"([^\"]+)\"");
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    /** Devuelve el primer grupo de la expresión regular o falla si no hay coincidencia. */
    private String extraer(String texto, String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(texto);
        assertTrue(matcher.find(), "No se encontró " + regex + " en: " + texto);
        return matcher.group(1);
    }

    /** Id del objeto de una lista JSON que contiene el texto indicado (no depende del orden de los campos). */
    private String idDe(String listaJson, String clave) {
        for (String objeto : listaJson.split("\\},\\s*\\{")) {
            if (objeto.contains(clave)) {
                return extraer(objeto, "\"id\"\\s*:\\s*(\\d+)");
            }
        }
        fail("No se encontró " + clave + " en: " + listaJson);
        return null;
    }

    private String json(String ruta, String token) throws Exception {
        return mvc.perform(get(ruta).header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

    // ---------- login ----------

    @Test
    void loginCorrectoDevuelveToken() throws Exception {
        String token = login("admin@gestor.com", "Admin123*");
        assertTrue(token.length() > 20);
    }

    @Test
    void loginConContrasenaIncorrectaDevuelve401() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("admin@gestor.com", "incorrecta")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void sinTokenLaApiDevuelve401() throws Exception {
        mvc.perform(get("/api/estudiantes")).andExpect(status().isUnauthorized());
    }

    // ---------- permisos por rol ----------

    @Test
    void elEstudianteNoPuedeListarEstudiantes() throws Exception {
        String token = login("juan.perez@gestor.com", "Estudiante123*");

        mvc.perform(get("/api/estudiantes").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void elDocenteNoPuedeCrearEstudiantes() throws Exception {
        String token = login("docente@gestor.com", "Docente123*");

        mvc.perform(post("/api/estudiantes")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"documento\":\"777\",\"nombre\":\"X\",\"curso\":\"10A\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void elAdministradorNoPuedeRegistrarNotas() throws Exception {
        String token = login("admin@gestor.com", "Admin123*");

        mvc.perform(put("/api/notas/registro")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void elAdministradorCreaEstudianteYUnDocumentoRepetidoDa409() throws Exception {
        String token = login("admin@gestor.com", "Admin123*");
        String estudiante = "{\"documento\":\"555001\",\"nombre\":\"Laura Ruiz\",\"curso\":\"11B\"}";

        mvc.perform(post("/api/estudiantes")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(estudiante))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/estudiantes")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(estudiante))
                .andExpect(status().isConflict());
    }

    // ---------- notas ----------

    @Test
    void elDocenteRegistraNotasYSeValidaElRango() throws Exception {
        String token = login("docente@gestor.com", "Docente123*");
        String estudianteId = idDe(json("/api/estudiantes?q=123456", token), "123456");
        String asignaturaId = idDe(json("/api/asignaturas", token), "MAT101");
        String cabecera = "{\"estudianteId\":" + estudianteId + ",\"asignaturaId\":" + asignaturaId + ",\"periodo\":\"2027-1\",";

        // Nota fuera de rango: 400
        mvc.perform(put("/api/notas/registro")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cabecera + "\"evaluaciones\":[{\"evaluacion\":\"Parcial\",\"peso\":100,\"valor\":5.5}]}"))
                .andExpect(status().isBadRequest());

        // Pesos que suman más de 100: 400
        mvc.perform(put("/api/notas/registro")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cabecera + "\"evaluaciones\":[{\"evaluacion\":\"A\",\"peso\":70,\"valor\":4.0},"
                                + "{\"evaluacion\":\"B\",\"peso\":50,\"valor\":4.0}]}"))
                .andExpect(status().isBadRequest());

        // Registro correcto: 200 y los pesos suman 100
        String respuesta = mvc.perform(put("/api/notas/registro")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cabecera + "\"evaluaciones\":[{\"evaluacion\":\"Parcial\",\"peso\":50,\"valor\":4.0},"
                                + "{\"evaluacion\":\"Final\",\"peso\":50,\"valor\":5.0}]}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertTrue(respuesta.contains("\"pesoTotal\":100"), "Respuesta inesperada: " + respuesta);
    }

    @Test
    void elEstudianteSoloVeSusPropiasNotas() throws Exception {
        String login = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("juan.perez@gestor.com", "Estudiante123*")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = extraer(login, "\"token\"\\s*:\\s*\"([^\"]+)\"");
        long propio = Long.parseLong(extraer(login, "\"estudianteId\"\\s*:\\s*(\\d+)"));

        // Sin indicar estudiante: devuelve las suyas
        String resumen = mvc.perform(get("/api/notas/resumen?periodo=2026-2").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertTrue(resumen.contains("MAT101"), "Respuesta inesperada: " + resumen);

        // Pidiendo las de otro estudiante: 403
        mvc.perform(get("/api/notas/resumen?periodo=2026-2&estudianteId=" + (propio + 1))
                        .header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }
}
