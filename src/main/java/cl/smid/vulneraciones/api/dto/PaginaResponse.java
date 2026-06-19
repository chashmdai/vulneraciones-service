package cl.smid.vulneraciones.api.dto;

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
public record PaginaResponse<T>(List<T> contenido, int pagina, int tamano, long total) {
}
