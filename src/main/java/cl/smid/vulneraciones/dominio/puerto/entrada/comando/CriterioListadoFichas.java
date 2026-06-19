package cl.smid.vulneraciones.dominio.puerto.entrada.comando;

import cl.smid.vulneraciones.dominio.modelo.Alcance;
import cl.smid.vulneraciones.dominio.modelo.EstadoFicha;

/**
 * Criterio de listado paginado de fichas, incluyendo el filtro territorial derivado del contexto.
 *
 * @param alcance         alcance territorial del usuario
 * @param idSedeUsuario   sede del usuario (para alcance SEDE)
 * @param idUnidadUsuario unidad del usuario (para alcance UNIDAD)
 * @param idCasoAlt       filtro opcional por caso ({@code null} = sin filtro)
 * @param estado          filtro opcional por estado ({@code null} = sin filtro)
 * @param pagina          índice de página (base 0)
 * @param tamano          tamaño de página
 */
public record CriterioListadoFichas(
        Alcance alcance,
        String idSedeUsuario,
        String idUnidadUsuario,
        String idCasoAlt,
        EstadoFicha estado,
        int pagina,
        int tamano) {
}
