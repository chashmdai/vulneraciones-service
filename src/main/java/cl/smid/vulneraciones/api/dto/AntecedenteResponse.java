package cl.smid.vulneraciones.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Respuesta de un antecedente. La descripción solo aparece para usuarios con rol de acceso reservado;
 * en caso contrario va nula (y se omite).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AntecedenteResponse(
        String altKey,
        String tipo,
        String descripcion,
        LocalDate fecha,
        String fuente,
        Instant registradoEn,
        String registradoPor) {
}
