package cl.smid.vulneraciones.dominio.excepcion;

/**
 * Intento de modificar una ficha fuera de su ventana de mutabilidad (VUL-409 / 409).
 *
 * <p>Solo en estado {@code EN_ELABORACION} la FIR admite el alta de vulneraciones y antecedentes.</p>
 */
public class FichaNoMutableException extends ErrorDominioException {

    public FichaNoMutableException(String mensaje) {
        super(CodigoError.VUL_409, mensaje);
    }
}
