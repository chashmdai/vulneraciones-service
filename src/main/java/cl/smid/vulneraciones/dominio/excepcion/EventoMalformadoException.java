package cl.smid.vulneraciones.dominio.excepcion;

/**
 * Evento entrante estructuralmente inválido o sin los datos mínimos para materializar la FIR.
 *
 * <p>El listener la traduce a {@code AmqpRejectAndDontRequeueException} para derivar el mensaje a
 * la DLQ sin reencolarlo (no es reintentable).</p>
 */
public class EventoMalformadoException extends ErrorDominioException {

    public EventoMalformadoException(String mensaje) {
        super(CodigoError.VUL_001, mensaje);
    }
}
