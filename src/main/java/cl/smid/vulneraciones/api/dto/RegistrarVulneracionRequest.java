package cl.smid.vulneraciones.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

/**
 * Cuerpo de la petición de registro de una vulneración.
 *
 * @param idDerechoAlt {@code alt_key} del derecho vulnerado (obligatorio)
 * @param idCausaAlt   {@code alt_key} de la causa (opcional)
 * @param idNnaAlt     {@code alt_key} del NNA afectado (obligatorio)
 * @param gravedad     gravedad (LEVE/MEDIA/GRAVE/GRAVISIMA)
 * @param relato       relato reservado en claro (opcional; se cifra en reposo)
 * @param fechaHecho   fecha del hecho (opcional)
 */
@Schema(description = "Solicitud para registrar una vulneración en una FIR.")
public record RegistrarVulneracionRequest(
        @Schema(description = "Identificador opaco del derecho vulnerado.",
                example = "2e7f7e7d-2f0d-41a5-bc9c-98d0a4bd32b5",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El derecho vulnerado (idDerechoAlt) es obligatorio.")
        String idDerechoAlt,
        @Schema(description = "Identificador opaco de la causa asociada, si aplica.",
                example = "6a0df156-9898-4e8e-9cf4-214f1b138a4a")
        String idCausaAlt,
        @Schema(description = "Identificador opaco del NNA afectado.",
                example = "d8d4c0c9-ec8e-49f2-b1c3-0d0f1f6b66a1",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El NNA afectado (idNnaAlt) es obligatorio.")
        String idNnaAlt,
        @Schema(description = "Gravedad de la vulneración.", example = "GRAVE",
                requiredMode = Schema.RequiredMode.REQUIRED,
                allowableValues = {"LEVE", "MEDIA", "GRAVE", "GRAVISIMA"})
        @NotBlank(message = "La gravedad es obligatoria.")
        String gravedad,
        @Schema(description = "Relato reservado sintético. No incluir datos reales en ejemplos.",
                example = "Texto reservado sintético para pruebas.")
        String relato,
        @Schema(description = "Fecha del hecho.", example = "2027-04-20")
        LocalDate fechaHecho) {
}
