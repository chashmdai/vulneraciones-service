package cl.smid.vulneraciones.api.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

/**
 * Sobre de error unificado del clúster SMID.
 *
 * <p>El campo de la ruta se llama {@code ruta} (no {@code path}), por convención del ecosistema. Los
 * detalles solo se pueblan en errores de validación; en el resto la lista va vacía y se omite de la
 * serialización.</p>
 *
 * @param status    estado HTTP numérico
 * @param error     etiqueta del estado HTTP (p. ej. {@code "Not Found"})
 * @param codigo    código de dominio (p. ej. {@code "VUL-404"})
 * @param mensaje   mensaje legible
 * @param detalles  detalles de validación (puede ir vacío/omitido)
 * @param ruta      ruta de la petición que originó el error
 * @param timestamp instante del error (UTC, ISO-8601)
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Schema(description = "Sobre de error unificado del ecosistema SMID.")
public record ErrorResponse(
        @Schema(description = "Código HTTP numérico.", example = "404")
        int status,
        @Schema(description = "Frase HTTP.", example = "Not Found")
        String error,
        @Schema(description = "Código estable del error.", example = "VUL-404",
                allowableValues = {"AUTZ-003", "AUTZ-004", "VUL-001", "VUL-404", "VUL-409",
                        "VUL-422", "VUL-500"})
        String codigo,
        @Schema(description = "Mensaje legible para el consumidor.",
                example = "No existe una ficha accesible con el identificador indicado.")
        String mensaje,
        @Schema(description = "Detalles de validación, si aplica.",
                example = "[\"gravedad: La gravedad es obligatoria.\"]")
        List<String> detalles,
        @Schema(description = "Ruta solicitada.", example = "/vulneraciones/fichas/5f1e36b4-4a38-4c7c-bfb7-cc3ed16cc9b1")
        String ruta,
        @Schema(description = "Instante UTC del error.", example = "2027-05-01T12:00:00Z")
        Instant timestamp) {

    /**
     * Factoría del sobre con el instante actual.
     *
     * @param status   estado HTTP
     * @param error    etiqueta HTTP
     * @param codigo   código de dominio
     * @param mensaje  mensaje legible
     * @param detalles detalles de validación (puede ser nulo)
     * @param ruta     ruta de la petición
     * @return el sobre de error
     */
    public static ErrorResponse de(int status, String error, String codigo, String mensaje,
                                   List<String> detalles, String ruta) {
        return new ErrorResponse(status, error, codigo, mensaje,
                detalles == null ? List.of() : List.copyOf(detalles), ruta, Instant.now());
    }
}
