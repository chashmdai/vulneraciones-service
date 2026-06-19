package cl.smid.vulneraciones.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * Respuesta de resumen de una FIR para el listado paginado (sin colecciones hijas).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record FichaResumenResponse(
        String altKey,
        String numeroFicha,
        String idCasoAlt,
        String numeroExpediente,
        String estado,
        String complejidad,
        String idSedeAlt,
        String idUnidadAlt,
        boolean esBeta,
        Instant abiertaEn,
        Instant cerradaEn,
        Instant creadaEn,
        Instant actualizadaEn) {
}
