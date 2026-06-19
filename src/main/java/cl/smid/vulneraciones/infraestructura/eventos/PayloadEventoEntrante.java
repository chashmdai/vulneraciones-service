package cl.smid.vulneraciones.infraestructura.eventos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

/**
 * DTO de deserialización del evento {@code caso.abierto} emitido por casos-service (6.4).
 *
 * <p>Tolerante a campos desconocidos: el contrato del productor puede crecer sin romper el consumo.
 * Se mapea luego a la proyección de dominio {@code EventoCasoAbierto}.</p>
 *
 * @param tipo       tipo del evento (debe ser {@code caso.abierto})
 * @param altKey     {@code alt_key} del caso (identificador del recurso)
 * @param ocurridoEn instante de apertura del caso (UTC, ISO-8601)
 * @param metadatos  metadatos no sensibles del caso
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PayloadEventoEntrante(
        String tipo,
        String altKey,
        Instant ocurridoEn,
        Metadatos metadatos) {

    /**
     * Metadatos del evento {@code caso.abierto}.
     *
     * @param numeroExpediente       número de expediente del caso
     * @param estado                 estado del caso
     * @param idSede                 {@code alt_key} de la sede
     * @param idUnidad               {@code alt_key} de la unidad
     * @param complejidad            complejidad del caso
     * @param requiereFichaReservada bandera de gating de la FIR
     * @param esBeta                 serie informada por el caso (opcional)
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Metadatos(
            String numeroExpediente,
            String estado,
            String idSede,
            String idUnidad,
            String complejidad,
            Boolean requiereFichaReservada,
            Boolean esBeta) {
    }
}
