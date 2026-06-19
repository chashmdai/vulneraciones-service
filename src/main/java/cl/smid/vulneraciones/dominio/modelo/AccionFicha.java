package cl.smid.vulneraciones.dominio.modelo;

import cl.smid.vulneraciones.dominio.excepcion.ValidacionException;

/**
 * Acciones que disparan transiciones en la máquina de estados de la FIR.
 *
 * <p>{@link #MATERIALIZACION} es una pseudo-acción de sistema: registra el asiento de apertura
 * de la ficha (origen {@code null}) y no es invocable por un usuario. {@link #CERRAR} y
 * {@link #REABRIR} son acciones <strong>administrativas</strong>: exigen rol de Coordinación.</p>
 */
public enum AccionFicha {

    /** Pseudo-acción de sistema: asiento de apertura al materializar la ficha. No invocable por usuario. */
    MATERIALIZACION(false, false),

    /** Cierra la ficha ({@code EN_ELABORACION → CERRADA}). Acción administrativa. */
    CERRAR(true, true),

    /** Reabre la ficha ({@code CERRADA → EN_ELABORACION}). Acción administrativa. */
    REABRIR(true, true);

    private final boolean invocablePorUsuario;
    private final boolean administrativa;

    AccionFicha(boolean invocablePorUsuario, boolean administrativa) {
        this.invocablePorUsuario = invocablePorUsuario;
        this.administrativa = administrativa;
    }

    /** @return {@code true} si la acción exige rol de Coordinación. */
    public boolean esAdministrativa() {
        return administrativa;
    }

    /** @return {@code true} si la acción puede ser solicitada por un usuario vía API. */
    public boolean esInvocablePorUsuario() {
        return invocablePorUsuario;
    }

    /**
     * Resuelve una acción solicitada por un usuario. Solo {@link #CERRAR} y {@link #REABRIR} son
     * válidas por esta vía; cualquier otro valor (incluida {@link #MATERIALIZACION}) se rechaza.
     *
     * @param texto nombre de la acción
     * @return la acción correspondiente
     * @throws ValidacionException si el texto es nulo, en blanco, desconocido o no invocable
     */
    public static AccionFicha desdeUsuario(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new ValidacionException("La acción de transición es obligatoria.");
        }
        String normalizado = texto.trim().toUpperCase();
        for (AccionFicha accion : values()) {
            if (accion.name().equals(normalizado) && accion.invocablePorUsuario) {
                return accion;
            }
        }
        throw new ValidacionException("Acción de transición no reconocida: '" + texto + "'. Use CERRAR o REABRIR.");
    }
}
