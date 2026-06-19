package cl.smid.vulneraciones.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Ítem de contenido reservado en claro (relato o descripción) asociado a una hija de la ficha.
 */
@Schema(description = "Ítem de contenido reservado en claro.")
public record ItemReservadoResponse(
        @Schema(description = "Identificador opaco del elemento que contiene el texto reservado.",
                example = "31abf51b-badf-4a3d-b2a7-1a37089e1370")
        String altKey,
        @Schema(description = "Texto reservado sintético. En producción no usar datos reales en ejemplos.",
                example = "Texto reservado sintético para pruebas.")
        String texto) {
}
