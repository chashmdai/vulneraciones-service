package cl.smid.vulneraciones.dominio.modelo;

/**
 * Serie del correlativo del número de FIR.
 *
 * <p>La serie {@link #BETA} (marcha blanca) está aislada de la {@link #OFICIAL} en filas distintas
 * del contador, de modo que la serie oficial arranca inmaculada en su correlativo unificado. El
 * prefijo {@code B} distingue visualmente la serie beta en el número formateado
 * (p. ej. {@code FIR-RM-B1/2027}).</p>
 */
public enum SerieCorrelativo {

    /** Serie oficial; sin prefijo en el número (p. ej. {@code FIR-RM-1/2027}). */
    OFICIAL(""),

    /** Serie de marcha blanca; prefijo {@code B} (p. ej. {@code FIR-RM-B1/2027}). */
    BETA("B");

    private final String prefijo;

    SerieCorrelativo(String prefijo) {
        this.prefijo = prefijo;
    }

    /** @return prefijo que precede al correlativo en el número formateado ("" para oficial, "B" para beta). */
    public String prefijo() {
        return prefijo;
    }

    /** @return {@code true} si la serie es BETA. */
    public boolean esBeta() {
        return this == BETA;
    }

    /**
     * Selecciona la serie a partir de la bandera {@code esBeta}.
     *
     * @param esBeta {@code true} para serie BETA, {@code false} para OFICIAL
     * @return la serie correspondiente
     */
    public static SerieCorrelativo desdeBandera(boolean esBeta) {
        return esBeta ? BETA : OFICIAL;
    }

    /**
     * Resuelve una serie por nombre, sin distinguir mayúsculas/minúsculas.
     *
     * @param texto nombre de la serie (admite nulos)
     * @return la serie correspondiente o {@code null} si el texto es nulo/en blanco/desconocido
     */
    public static SerieCorrelativo desde(String texto) {
        if (texto == null) {
            return null;
        }
        String normalizado = texto.trim().toUpperCase();
        for (SerieCorrelativo serie : values()) {
            if (serie.name().equals(normalizado)) {
                return serie;
            }
        }
        return null;
    }
}
