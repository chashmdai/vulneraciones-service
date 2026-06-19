package cl.smid.vulneraciones.dominio.puerto.entrada;

import cl.smid.vulneraciones.dominio.modelo.ContextoUsuario;
import cl.smid.vulneraciones.dominio.modelo.Ficha;
import cl.smid.vulneraciones.dominio.modelo.ResultadoPagina;
import cl.smid.vulneraciones.dominio.modelo.vista.VistaFicha;
import cl.smid.vulneraciones.dominio.puerto.entrada.comando.CriterioListadoFichas;

/**
 * Caso de uso: consulta de fichas (detalle y listado), con filtro territorial y redacción de
 * contenido reservado según el rol del solicitante.
 */
public interface ConsultarFichas {

    /**
     * Detalle de una ficha. El contenido reservado se redacta si el solicitante no tiene rol de
     * acceso reservado.
     *
     * @param altKey identificador opaco de la ficha
     * @param ctx    contexto de sesión
     * @return la vista de detalle
     * @throws cl.smid.vulneraciones.dominio.excepcion.RecursoNoEncontradoException si no existe o está
     *         fuera de alcance (VUL-404)
     */
    VistaFicha obtenerDetalle(String altKey, ContextoUsuario ctx);

    /**
     * Listado paginado de fichas dentro del alcance del solicitante.
     *
     * @param criterio criterio territorial, filtros y paginación
     * @return página de fichas (resumen)
     */
    ResultadoPagina<Ficha> listar(CriterioListadoFichas criterio);
}
