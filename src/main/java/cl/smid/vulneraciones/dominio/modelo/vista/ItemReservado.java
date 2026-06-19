package cl.smid.vulneraciones.dominio.modelo.vista;

/**
 * Ítem de contenido reservado: el texto en claro asociado a una hija de la ficha.
 *
 * @param altKey identificador opaco de la vulneración/antecedente
 * @param texto  contenido reservado en claro (relato o descripción)
 */
public record ItemReservado(String altKey, String texto) {
}
