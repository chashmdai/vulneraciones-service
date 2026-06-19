package cl.smid.vulneraciones.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * Respuesta de detalle de una FIR, con el contenido reservado ya resuelto según el rol del
 * solicitante. {@code reservadoOculto=true} indica que el relato/descripcion fueron redactados.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record FichaDetalleResponse(
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
        Instant actualizadaEn,
        boolean reservadoOculto,
        EnriquecimientoResponse enriquecimiento,
        List<VulneracionResponse> vulneraciones,
        List<AntecedenteResponse> antecedentes,
        List<TransicionResponse> historial) {
}
