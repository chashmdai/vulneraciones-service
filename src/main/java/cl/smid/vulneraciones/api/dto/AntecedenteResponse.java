package cl.smid.vulneraciones.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Respuesta de un antecedente. La descripción solo aparece para usuarios con rol de acceso reservado;
 * en caso contrario va nula (y se omite).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Antecedente reservado asociado a una FIR.")
public record AntecedenteResponse(
        @Schema(description = "Identificador opaco del antecedente.", example = "31abf51b-badf-4a3d-b2a7-1a37089e1370")
        String altKey,
        @Schema(description = "Tipo del antecedente.", example = "ESCOLAR",
                allowableValues = {"DERIVACION", "PERICIA", "ANTECEDENTE_FAMILIAR", "ESCOLAR", "SALUD", "OTRO"})
        String tipo,
        @Schema(description = "Descripción reservada. Puede omitirse o venir redactada según rol.",
                example = "Descripción reservada sintética para pruebas.")
        String descripcion,
        @Schema(description = "Fecha del antecedente.", example = "2027-03-15")
        LocalDate fecha,
        @Schema(description = "Fuente sintética o institucional del antecedente.", example = "Fuente institucional sintética")
        String fuente,
        @Schema(description = "Instante de registro en UTC.", example = "2027-05-01T12:00:00Z")
        Instant registradoEn,
        @Schema(description = "Identificador opaco del actor que registró el antecedente.",
                example = "c27f4500-f412-4fd1-86a8-6caa5933583b")
        String registradoPor) {
}
