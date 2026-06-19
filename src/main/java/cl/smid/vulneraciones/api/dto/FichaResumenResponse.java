package cl.smid.vulneraciones.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Respuesta de resumen de una FIR para el listado paginado (sin colecciones hijas).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Resumen de una FIR para listados paginados.")
public record FichaResumenResponse(
        @Schema(description = "Identificador opaco de la FIR.", example = "5f1e36b4-4a38-4c7c-bfb7-cc3ed16cc9b1")
        String altKey,
        @Schema(description = "Número institucional de la FIR.", example = "FIR-RM-1/2027")
        String numeroFicha,
        @Schema(description = "Identificador opaco del caso asociado.", example = "7d6b3c3c-9d22-4b10-8d3d-1f3900c13f20")
        String idCasoAlt,
        @Schema(description = "Número de expediente del caso asociado.", example = "EXP-RM-1/2027")
        String numeroExpediente,
        @Schema(description = "Estado actual de la FIR.", example = "EN_ELABORACION",
                allowableValues = {"EN_ELABORACION", "CERRADA"})
        String estado,
        @Schema(description = "Complejidad asociada.", example = "ALTA")
        String complejidad,
        @Schema(description = "Identificador opaco de la sede.", example = "11111111-1111-1111-1111-111111111111")
        String idSedeAlt,
        @Schema(description = "Identificador opaco de la unidad.", example = "4f86e9a4-2924-41d7-bf27-6ef13b6f6b9a")
        String idUnidadAlt,
        @Schema(description = "Indica si pertenece a serie beta.", example = "false")
        boolean esBeta,
        @Schema(description = "Instante de apertura en UTC.", example = "2027-05-01T12:00:00Z")
        Instant abiertaEn,
        @Schema(description = "Instante de cierre en UTC, nulo si sigue en elaboración.", example = "2027-06-01T12:00:00Z")
        Instant cerradaEn,
        @Schema(description = "Instante de creación en UTC.", example = "2027-05-01T12:00:00Z")
        Instant creadaEn,
        @Schema(description = "Instante de última actualización en UTC.", example = "2027-05-02T10:30:00Z")
        Instant actualizadaEn) {
}
