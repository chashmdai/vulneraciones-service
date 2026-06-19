package cl.smid.vulneraciones.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

/**
 * Cuerpo de la petición de registro de un antecedente.
 *
 * @param tipo        tipo del antecedente (DERIVACION/PERICIA/ANTECEDENTE_FAMILIAR/ESCOLAR/SALUD/OTRO)
 * @param descripcion descripción reservada en claro (opcional; se cifra en reposo)
 * @param fecha       fecha del antecedente (opcional)
 * @param fuente      fuente del antecedente (opcional)
 */
@Schema(description = "Solicitud para registrar un antecedente reservado en una FIR.")
public record RegistrarAntecedenteRequest(
        @Schema(description = "Tipo del antecedente.", example = "ESCOLAR",
                requiredMode = Schema.RequiredMode.REQUIRED,
                allowableValues = {"DERIVACION", "PERICIA", "ANTECEDENTE_FAMILIAR", "ESCOLAR", "SALUD", "OTRO"})
        @NotBlank(message = "El tipo de antecedente es obligatorio.")
        String tipo,
        @Schema(description = "Descripción reservada sintética. No incluir datos reales en ejemplos.",
                example = "Descripción reservada sintética para pruebas.")
        String descripcion,
        @Schema(description = "Fecha del antecedente.", example = "2027-03-15")
        LocalDate fecha,
        @Schema(description = "Fuente sintética o institucional del antecedente.", example = "Fuente institucional sintética")
        String fuente) {
}
