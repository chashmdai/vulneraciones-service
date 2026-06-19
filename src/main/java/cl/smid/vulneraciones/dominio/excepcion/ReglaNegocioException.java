package cl.smid.vulneraciones.dominio.excepcion;

/**
 * Regla de negocio incumplida (VUL-422 / 422), p. ej. derecho o NNA requerido ausente.
 */
public class ReglaNegocioException extends ErrorDominioException {

    public ReglaNegocioException(String mensaje) {
        super(CodigoError.VUL_422, mensaje);
    }
}
