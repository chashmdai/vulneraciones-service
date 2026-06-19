package cl.smid.vulneraciones.dominio.excepcion;

/**
 * Transición no contemplada por la máquina de estados para el estado actual (VUL-409 / 409).
 */
public class TransicionInvalidaException extends ErrorDominioException {

    public TransicionInvalidaException(String mensaje) {
        super(CodigoError.VUL_409, mensaje);
    }
}
