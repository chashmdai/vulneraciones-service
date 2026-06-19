package cl.smid.vulneraciones.dominio.modelo;

import java.util.List;

/**
 * Resultado paginado genérico del dominio.
 *
 * @param contenido elementos de la página
 * @param pagina    índice de página (base 0)
 * @param tamano    tamaño de página solicitado
 * @param total     total de elementos que satisfacen el criterio
 * @param <T>       tipo de elemento
 */
public record ResultadoPagina<T>(List<T> contenido, int pagina, int tamano, long total) {

    public ResultadoPagina {
        contenido = contenido == null ? List.of() : List.copyOf(contenido);
    }
}
