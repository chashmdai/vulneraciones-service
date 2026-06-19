package cl.smid.vulneraciones.dominio.excepcion;

import java.util.List;

/**
 * Error de validación o de cuerpo ilegible (VUL-001 / 400).
 */
public class ValidacionException extends ErrorDominioException {

    public ValidacionException(String mensaje) {
        super(CodigoError.VUL_001, mensaje);
    }

    public ValidacionException(String mensaje, List<String> detalles) {
        super(CodigoError.VUL_001, mensaje, detalles);
    }
}
