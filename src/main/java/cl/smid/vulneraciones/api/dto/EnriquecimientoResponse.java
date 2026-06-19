package cl.smid.vulneraciones.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Bloque de enriquecimiento on-demand contra Casos (best-effort). Cuando la costura está desactivada o
 * el cruce falla, {@code disponible=false} y el resto va nulo (omitido).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Enriquecimiento on-demand contra Casos, best-effort.")
public record EnriquecimientoResponse(
        @Schema(description = "Indica si el enriquecimiento remoto estuvo disponible.", example = "false")
        boolean disponible,
        @Schema(description = "Estado sintético del caso asociado si el enriquecimiento está disponible.",
                example = "ABIERTO")
        String estadoCaso,
        @Schema(description = "Número de expediente vigente si el enriquecimiento está disponible.",
                example = "EXP-RM-1/2027")
        String numeroExpedienteVigente) {
}
