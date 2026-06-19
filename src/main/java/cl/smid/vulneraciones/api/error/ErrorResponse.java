package cl.smid.vulneraciones.api.error;

import com.fasterxml.jackson.annotation.JsonInclude;

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
public record ErrorResponse(
        int status,
        String error,
        String codigo,
        String mensaje,
        List<String> detalles,
        String ruta,
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
