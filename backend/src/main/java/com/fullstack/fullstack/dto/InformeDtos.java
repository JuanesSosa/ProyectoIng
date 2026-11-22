package com.fullstack.fullstack.dto;

/** DTOs del envío de informes por correo. */
public final class InformeDtos {

    private InformeDtos() {
    }

    public record EnvioInformeResponse(int enviados, int sinCorreo, int fallidos) {
    }
}