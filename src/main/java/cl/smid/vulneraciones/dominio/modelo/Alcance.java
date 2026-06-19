package cl.smid.vulneraciones.dominio.modelo;

/**
 * Radio territorial del usuario, transportado por el claim {@code alcance} del token.
 *
 * <p>Determina el filtro registro a registro: {@link #NACIONAL} ve todo; {@link #SEDE} filtra por
 * sede; {@link #UNIDAD} filtra por unidad. Fuera de alcance ⇒ 404 (no se revela la existencia).</p>
 */
public enum Alcance {

    /** El usuario solo accede a registros de su unidad. */
    UNIDAD,

    /** El usuario accede a todos los registros de su sede. */
    SEDE,

    /** El usuario accede a todo el país (sin filtro territorial). */
    NACIONAL;

    /**
     * Resuelve el alcance de forma tolerante. Un valor ausente o desconocido se interpreta como el
     * radio más restrictivo ({@link #UNIDAD}) por seguridad (denegación por defecto).
     *
     * @param texto nombre del alcance (admite nulos)
     * @return el alcance correspondiente, o {@link #UNIDAD} si es nulo/en blanco/desconocido
     */
    public static Alcance desde(String texto) {
        if (texto == null) {
            return UNIDAD;
        }
        String normalizado = texto.trim().toUpperCase();
        for (Alcance alcance : values()) {
            if (alcance.name().equals(normalizado)) {
                return alcance;
            }
        }
        return UNIDAD;
    }
}
