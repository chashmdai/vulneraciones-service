package cl.smid.vulneraciones.dominio.modelo;

/**
 * Complejidad del caso, heredada del evento {@code caso.abierto} (snapshot).
 *
 * <p>Es nullable en la FIR: el evento podría no informarla o informar un valor desconocido, en cuyo
 * caso se almacena {@code null}. Requerimientos marca {@code requiereFichaReservada} cuando la
 * complejidad es {@link #MEDIANA} o {@link #ALTA}; esa bandera es la costura que materializa la FIR.</p>
 */
public enum Complejidad {

    BAJA,
    MEDIANA,
    ALTA;

    /**
     * Resuelve la complejidad de forma tolerante. Un valor desconocido no es un error: la FIR
     * conserva la complejidad como dato opcional.
     *
     * @param texto nombre de la complejidad (admite nulos)
     * @return la complejidad correspondiente o {@code null} si es nulo/en blanco/desconocido
     */
    public static Complejidad desde(String texto) {
        if (texto == null) {
            return null;
        }
        String normalizado = texto.trim().toUpperCase();
        for (Complejidad complejidad : values()) {
            if (complejidad.name().equals(normalizado)) {
                return complejidad;
            }
        }
        return null;
    }
}
