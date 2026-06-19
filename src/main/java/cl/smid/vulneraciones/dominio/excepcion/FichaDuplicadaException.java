package cl.smid.vulneraciones.dominio.excepcion;

/**
 * La FIR ya existe para el caso indicado (violación de unicidad sobre {@code id_caso_alt}).
 *
 * <p>El adaptador de persistencia traduce la violación de unicidad de la base de datos a esta
 * excepción de dominio. En el consumo de eventos representa una carrera entre instancias: al
 * propagarse, el contenedor reintenta y el pre-chequeo de idempotencia resuelve la reentrega como
 * no-op. No corresponde a un código HTTP de la API (la FIR no se crea por la API).</p>
 */
public class FichaDuplicadaException extends ErrorDominioException {

    public FichaDuplicadaException(String mensaje) {
        super(CodigoError.VUL_409, mensaje);
    }
}
