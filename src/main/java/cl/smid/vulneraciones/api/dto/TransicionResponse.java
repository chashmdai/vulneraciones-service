package cl.smid.vulneraciones.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Respuesta de un asiento del historial de la máquina de estados.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Asiento del historial de materialización y transiciones de la FIR.")
public record TransicionResponse(
        @Schema(description = "Identificador opaco del asiento de historial.",
                example = "afcf2d25-190c-4e37-aef8-9dd1543d29b0")
        String altKey,
        @Schema(description = "Estado origen. Nulo en la materialización inicial.", example = "EN_ELABORACION",
                allowableValues = {"EN_ELABORACION", "CERRADA"})
        String estadoOrigen,
        @Schema(description = "Estado destino.", example = "CERRADA",
                allowableValues = {"EN_ELABORACION", "CERRADA"})
        String estadoDestino,
        @Schema(description = "Acción aplicada. MATERIALIZACION es de sistema, no invocable por API.",
                example = "CERRAR",
                allowableValues = {"MATERIALIZACION", "CERRAR", "REABRIR"})
        String accion,
        @Schema(description = "Observación sintética de la transición.", example = "Cierre administrativo sintético.")
        String observacion,
        @Schema(description = "Identificador opaco del actor.", example = "c27f4500-f412-4fd1-86a8-6caa5933583b")
        String actorAlt,
        @Schema(description = "Instante UTC de la transición.", example = "2027-05-01T12:00:00Z")
        Instant ocurridoEn) {
}
