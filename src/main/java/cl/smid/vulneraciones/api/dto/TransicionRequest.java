package cl.smid.vulneraciones.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Cuerpo de la petición de transición administrativa de una ficha.
 *
 * @param accion      acción a aplicar (CERRAR/REABRIR)
 * @param observacion observación opcional
 */
@Schema(description = "Solicitud de transición administrativa de una FIR. Requiere rol de Coordinación.")
public record TransicionRequest(
        @Schema(description = "Acción administrativa a aplicar.", example = "CERRAR",
                requiredMode = Schema.RequiredMode.REQUIRED,
                allowableValues = {"CERRAR", "REABRIR"})
        @NotBlank(message = "La acción de transición es obligatoria.")
        String accion,
        @Schema(description = "Observación sintética asociada a la transición.",
                example = "Cierre administrativo sintético.")
        String observacion) {
}
