package cl.smid.vulneraciones.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Cuerpo de la petición de transición administrativa de una ficha.
 *
 * @param accion      acción a aplicar (CERRAR/REABRIR)
 * @param observacion observación opcional
 */
public record TransicionRequest(
        @NotBlank(message = "La acción de transición es obligatoria.")
        String accion,
        String observacion) {
}
