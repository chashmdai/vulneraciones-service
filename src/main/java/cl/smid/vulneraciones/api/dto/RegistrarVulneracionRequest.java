package cl.smid.vulneraciones.api.dto;

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
public record RegistrarVulneracionRequest(
        @NotBlank(message = "El derecho vulnerado (idDerechoAlt) es obligatorio.")
        String idDerechoAlt,
        String idCausaAlt,
        @NotBlank(message = "El NNA afectado (idNnaAlt) es obligatorio.")
        String idNnaAlt,
        @NotBlank(message = "La gravedad es obligatoria.")
        String gravedad,
        String relato,
        LocalDate fechaHecho) {
}
