package cl.smid.vulneraciones.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Bloque de enriquecimiento on-demand contra Casos (best-effort). Cuando la costura está desactivada o
 * el cruce falla, {@code disponible=false} y el resto va nulo (omitido).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record EnriquecimientoResponse(
        boolean disponible,
        String estadoCaso,
        String numeroExpedienteVigente) {
}
