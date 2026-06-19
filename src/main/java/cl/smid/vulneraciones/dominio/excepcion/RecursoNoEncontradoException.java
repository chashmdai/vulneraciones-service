package cl.smid.vulneraciones.dominio.excepcion;

/**
 * Recurso inexistente o fuera de alcance territorial (VUL-404 / 404).
 *
 * <p>La denegación territorial se expresa como 404 (no 403) para no revelar la existencia de
 * registros fuera del alcance del solicitante.</p>
 */
public class RecursoNoEncontradoException extends ErrorDominioException {

    public RecursoNoEncontradoException(String mensaje) {
        super(CodigoError.VUL_404, mensaje);
    }
}
