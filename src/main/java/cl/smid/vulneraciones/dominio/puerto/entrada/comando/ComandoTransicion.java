package cl.smid.vulneraciones.dominio.puerto.entrada.comando;

/**
 * Comando de transición administrativa de una ficha.
 *
 * @param fichaAltKey {@code alt_key} de la ficha
 * @param accion      acción textual (CERRAR/REABRIR)
 * @param observacion observación opcional
 */
public record ComandoTransicion(String fichaAltKey, String accion, String observacion) {
}
