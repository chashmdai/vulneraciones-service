package cl.smid.vulneraciones.api.dto;

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
public record RegistrarAntecedenteRequest(
        @NotBlank(message = "El tipo de antecedente es obligatorio.")
        String tipo,
        String descripcion,
        LocalDate fecha,
        String fuente) {
}
