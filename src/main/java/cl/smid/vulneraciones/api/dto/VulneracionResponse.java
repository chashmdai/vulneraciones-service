package cl.smid.vulneraciones.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Respuesta de una vulneración. El relato solo aparece para usuarios con rol de acceso reservado; en
 * caso contrario va nulo (y se omite). Las etiquetas de catálogo son enriquecimiento opcional.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Vulneración registrada en una FIR.")
public record VulneracionResponse(
        @Schema(description = "Identificador opaco de la vulneración.", example = "31abf51b-badf-4a3d-b2a7-1a37089e1370")
        String altKey,
        @Schema(description = "Identificador opaco del derecho vulnerado.",
                example = "2e7f7e7d-2f0d-41a5-bc9c-98d0a4bd32b5")
        String idDerechoAlt,
        @Schema(description = "Identificador opaco de la causa asociada, si aplica.",
                example = "6a0df156-9898-4e8e-9cf4-214f1b138a4a")
        String idCausaAlt,
        @Schema(description = "Identificador opaco del NNA afectado.",
                example = "d8d4c0c9-ec8e-49f2-b1c3-0d0f1f6b66a1")
        String idNnaAlt,
        @Schema(description = "Etiqueta legible sintética del NNA cuando el enriquecimiento está disponible.",
                example = "NNA registrado")
        String nnaNombreLegible,
        @Schema(description = "Etiqueta del derecho desde catálogo, si está disponible.",
                example = "Derecho sintético")
        String etiquetaDerecho,
        @Schema(description = "Etiqueta de la causa desde catálogo, si está disponible.",
                example = "Causa sintética")
        String etiquetaCausa,
        @Schema(description = "Gravedad de la vulneración.", example = "GRAVE",
                allowableValues = {"LEVE", "MEDIA", "GRAVE", "GRAVISIMA"})
        String gravedad,
        @Schema(description = "Relato reservado. Puede omitirse o venir redactado según rol.",
                example = "Texto reservado sintético para pruebas.")
        String relato,
        @Schema(description = "Fecha del hecho.", example = "2027-04-20")
        LocalDate fechaHecho,
        @Schema(description = "Instante de registro en UTC.", example = "2027-05-01T12:00:00Z")
        Instant registradoEn,
        @Schema(description = "Identificador opaco del actor que registró la vulneración.",
                example = "c27f4500-f412-4fd1-86a8-6caa5933583b")
        String registradoPor) {
}
