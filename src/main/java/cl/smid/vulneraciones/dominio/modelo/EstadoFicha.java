package cl.smid.vulneraciones.dominio.modelo;

/**
 * Estados del ciclo de vida de la Ficha Interna Reservada (FIR).
 *
 * <p>El agregado nace en {@link #EN_ELABORACION} al materializarse desde el evento
 * {@code caso.abierto} y transita a {@link #CERRADA} (terminal salvo reapertura administrativa).
 * Solo en {@code EN_ELABORACION} la ficha admite el alta de vulneraciones y antecedentes
 * (ventana de mutabilidad).</p>
 */
public enum EstadoFicha {

    /** Estado inicial: la ficha está abierta y admite registro de vulneraciones/antecedentes. */
    EN_ELABORACION,

    /** Estado terminal: la ficha está cerrada; no admite nuevos registros hasta una reapertura. */
    CERRADA;

    /**
     * Resuelve un estado a partir de su nombre, sin distinguir mayúsculas/minúsculas ni espacios.
     *
     * @param texto nombre del estado (admite nulos)
     * @return el estado correspondiente o {@code null} si el texto es nulo/en blanco/desconocido
     */
    public static EstadoFicha desde(String texto) {
        if (texto == null) {
            return null;
        }
        String normalizado = texto.trim().toUpperCase();
        for (EstadoFicha estado : values()) {
            if (estado.name().equals(normalizado)) {
                return estado;
            }
        }
        return null;
    }

    /** @return {@code true} si el estado es terminal (cerrado). */
    public boolean esTerminal() {
        return this == CERRADA;
    }
}
