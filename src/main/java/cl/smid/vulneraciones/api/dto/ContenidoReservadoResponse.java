package cl.smid.vulneraciones.api.dto;

import java.util.List;

/**
 * Respuesta del endpoint dedicado de contenido reservado (solo rol de acceso reservado).
 */
public record ContenidoReservadoResponse(
        String fichaAltKey,
        List<ItemReservadoResponse> relatos,
        List<ItemReservadoResponse> descripciones) {
}
