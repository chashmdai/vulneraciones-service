package cl.smid.vulneraciones.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Respuesta del endpoint dedicado de contenido reservado (solo rol de acceso reservado).
 */
@Schema(description = "Contenido reservado en claro de una FIR, disponible solo para roles habilitados.")
public record ContenidoReservadoResponse(
        @Schema(description = "Identificador opaco de la FIR.", example = "5f1e36b4-4a38-4c7c-bfb7-cc3ed16cc9b1")
        String fichaAltKey,
        @Schema(description = "Relatos reservados de vulneraciones.")
        List<ItemReservadoResponse> relatos,
        @Schema(description = "Descripciones reservadas de antecedentes.")
        List<ItemReservadoResponse> descripciones) {
}
