package cl.smid.vulneraciones.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Sobre de respuesta paginada genérico.
 *
 * @param contenido elementos de la página
 * @param pagina    índice de página (base 0)
 * @param tamano    tamaño de página
 * @param total     total de elementos que satisfacen el criterio
 * @param <T>       tipo de elemento
 */
@Schema(description = "Página de resultados de la API.")
public record PaginaResponse<T>(
        @Schema(description = "Contenido de la página.")
        List<T> contenido,
        @Schema(description = "Número de página, base cero.", example = "0")
        int pagina,
        @Schema(description = "Tamaño de página solicitado.", example = "20")
        int tamano,
        @Schema(description = "Total de elementos encontrados.", example = "42")
        long total) {
}
