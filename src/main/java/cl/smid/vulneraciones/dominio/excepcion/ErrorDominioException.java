package cl.smid.vulneraciones.dominio.excepcion;

import java.util.List;

/**
 * Raíz de la jerarquía de excepciones de negocio del dominio de Vulneraciones.
 *
 * <p>Cada excepción transporta un {@link CodigoError} que el adaptador HTTP traduce a su estado y
 * código en el sobre de error unificado. El campo {@code detalles} solo se usa para errores de
 * validación (no expone internals en otros casos).</p>
 */
public abstract class ErrorDominioException extends RuntimeException {

    private final CodigoError codigo;
    private final transient List<String> detalles;

    protected ErrorDominioException(CodigoError codigo, String mensaje) {
        this(codigo, mensaje, List.of());
    }

    protected ErrorDominioException(CodigoError codigo, String mensaje, List<String> detalles) {
        super(mensaje);
        this.codigo = codigo;
        this.detalles = detalles == null ? List.of() : List.copyOf(detalles);
    }

    /** @return el código de error asociado. */
    public CodigoError getCodigo() {
        return codigo;
    }

    /** @return detalles adicionales (solo poblados en validación); lista vacía en otros casos. */
    public List<String> getDetalles() {
        return detalles;
    }
}
