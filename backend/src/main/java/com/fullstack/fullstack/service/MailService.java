package com.fullstack.fullstack.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

/** Envío de correos por la API HTTPS de Brevo (Render gratis bloquea SMTP). */
@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);
    private static final String URL = "https://api.brevo.com/v3/smtp/email";

    private final RestClient restClient = RestClient.create();
    private final String apiKey;
    private final String fromEmail;
    private final String fromName;

    public MailService(@Value("${app.mail.brevo-api-key:}") String apiKey,
                       @Value("${app.mail.from-email:}") String fromEmail,
                       @Value("${app.mail.from-name:Gestor de Notas}") String fromName) {
        this.apiKey = apiKey;
        this.fromEmail = fromEmail;
        this.fromName = fromName;
    }

    public boolean estaConfigurado() {
        return !apiKey.isBlank() && !fromEmail.isBlank();
    }

    /** Devuelve true si Brevo aceptó el mensaje. Nunca registra la API key. */
    public boolean enviar(String paraCorreo, String paraNombre, String asunto, String html) {
        try {
            restClient.post()
                    .uri(URL)
                    .header("api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "sender", Map.of("name", fromName, "email", fromEmail),
                            "to", List.of(Map.of("email", paraCorreo, "name", paraNombre)),
                            "subject", asunto,
                            "htmlContent", html))
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (RestClientException e) {
            log.warn("No se pudo enviar el correo a {}: {}", paraCorreo, e.getMessage());
            return false;
        }
    }
}