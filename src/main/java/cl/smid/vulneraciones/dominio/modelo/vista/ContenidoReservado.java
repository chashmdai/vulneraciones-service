package cl.smid.vulneraciones.dominio.modelo.vista;

import java.util.List;

/**
 * Contenido reservado de una FIR (relatos y descripciones en claro), devuelto solo por el endpoint
 * dedicado y solo a usuarios con rol de acceso reservado.
 *
 * @param fichaAltKey  identificador opaco de la ficha
 * @param relatos      relatos de las vulneraciones (en claro)
 * @param descripciones descripciones de los antecedentes (en claro)
 */
public record ContenidoReservado(
        String fichaAltKey,
        List<ItemReservado> relatos,
        List<ItemReservado> descripciones) {

    public ContenidoReservado {
        relatos = relatos == null ? List.of() : List.copyOf(relatos);
        descripciones = descripciones == null ? List.of() : List.copyOf(descripciones);
    }
}
