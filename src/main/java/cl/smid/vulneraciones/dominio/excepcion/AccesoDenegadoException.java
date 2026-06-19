package cl.smid.vulneraciones.dominio.excepcion;

/**
 * Autenticado pero sin el rol requerido (AUTZ-004 / 403).
 *
 * <p>Aplica a acciones administrativas (Coordinación) y al acceso a contenido reservado.</p>
 */
public class AccesoDenegadoException extends ErrorDominioException {

    public AccesoDenegadoException(String mensaje) {
        super(CodigoError.AUTZ_004, mensaje);
    }
}
