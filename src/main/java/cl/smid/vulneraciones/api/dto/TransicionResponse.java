package cl.smid.vulneraciones.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * Respuesta de un asiento del historial de la máquina de estados.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TransicionResponse(
        String altKey,
        String estadoOrigen,
        String estadoDestino,
        String accion,
        String observacion,
        String actorAlt,
        Instant ocurridoEn) {
}
