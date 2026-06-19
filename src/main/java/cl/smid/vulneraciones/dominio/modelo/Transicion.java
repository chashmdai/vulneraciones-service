package cl.smid.vulneraciones.dominio.modelo;

import java.time.Instant;

/**
 * Asiento del historial de la máquina de estados de la FIR.
 *
 * <p>El asiento de apertura usa {@code estadoOrigen = null}, {@code accion = MATERIALIZACION} y el
 * actor de sistema. Es un objeto de valor inmutable, append-only en el agregado.</p>
 *
 * @param altKey        identificador opaco del asiento
 * @param estadoOrigen  estado previo (nulo en el asiento de apertura)
 * @param estadoDestino estado resultante
 * @param accion        acción que provocó la transición
 * @param observacion   observación opcional del usuario
 * @param actorAlt      {@code alt_key} del actor (de sistema en la apertura)
 * @param ocurridoEn    instante de la transición (UTC)
 */
public record Transicion(
        String altKey,
        EstadoFicha estadoOrigen,
        EstadoFicha estadoDestino,
        AccionFicha accion,
        String observacion,
        String actorAlt,
        Instant ocurridoEn) {
}
