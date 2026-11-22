package com.fullstack.fullstack.dto;

import java.util.List;

/** DTOs del envío de notas por correo. */
public final class CorreoDtos {

    private CorreoDtos() {
    }

    /** Resultado de un envío: cuántos se enviaron y a quiénes no se pudo (con el motivo). */
    public record EnvioResponse(int enviados, int fallidos, List<String> detalles) {
    }
}
