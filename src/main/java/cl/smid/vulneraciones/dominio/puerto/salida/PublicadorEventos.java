package cl.smid.vulneraciones.dominio.puerto.salida;

/**
 * Puerto de salida: publicación de eventos de dominio.
 *
 * <p>La publicación es <strong>tolerante a fallos</strong>: las implementaciones nunca propagan una
 * excepción de transporte (no deshacen el negocio). El transporte es conmutable (log / RabbitMQ).</p>
 */
public interface PublicadorEventos {

    /**
     * Publica un evento de dominio. No debe lanzar excepciones por fallos de transporte.
     *
     * @param evento sobre de evento (metadata-only)
     */
    void publicar(EventoDominio evento);
}
