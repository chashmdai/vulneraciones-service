package cl.smid.vulneraciones.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Respuesta de una vulneración. El relato solo aparece para usuarios con rol de acceso reservado; en
 * caso contrario va nulo (y se omite). Las etiquetas de catálogo son enriquecimiento opcional.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record VulneracionResponse(
        String altKey,
        String idDerechoAlt,
        String idCausaAlt,
        String idNnaAlt,
        String nnaNombreLegible,
        String etiquetaDerecho,
        String etiquetaCausa,
        String gravedad,
        String relato,
        LocalDate fechaHecho,
        Instant registradoEn,
        String registradoPor) {
}
