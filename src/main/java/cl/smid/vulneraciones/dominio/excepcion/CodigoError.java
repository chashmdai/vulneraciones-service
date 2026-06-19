package cl.smid.vulneraciones.dominio.excepcion;

/**
 * Catálogo de códigos de error del servicio de Vulneraciones (6.5).
 *
 * <p>Cada código fija el estado HTTP con que se traduce en el sobre de error unificado. El dominio
 * referencia estos códigos sin conocer las constantes HTTP de Spring (capa pura).</p>
 */
public enum CodigoError {

    /** Validación o cuerpo de la solicitud ilegible. */
    VUL_001("VUL-001", 400),

    /** Recurso inexistente o fuera de alcance territorial (no se revela la existencia). */
    VUL_404("VUL-404", 404),

    /** Transición inválida o ficha no mutable para la operación solicitada. */
    VUL_409("VUL-409", 409),

    /** Regla de negocio incumplida (p. ej. derecho/NNA requerido ausente). */
    VUL_422("VUL-422", 422),

    /** Error interno no controlado. */
    VUL_500("VUL-500", 500),

    /** No autenticado (token ausente o inválido). */
    AUTZ_003("AUTZ-003", 401),

    /** Autenticado sin el rol requerido (Coordinación o acceso a contenido reservado). */
    AUTZ_004("AUTZ-004", 403);

    private final String codigo;
    private final int httpStatus;

    CodigoError(String codigo, int httpStatus) {
        this.codigo = codigo;
        this.httpStatus = httpStatus;
    }

    /** @return el código textual (p. ej. {@code "VUL-404"}). */
    public String codigo() {
        return codigo;
    }

    /** @return el estado HTTP asociado. */
    public int httpStatus() {
        return httpStatus;
    }
}
