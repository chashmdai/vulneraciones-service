package cl.smid.vulneraciones.dominio.modelo.vista;

/**
 * Bloque de enriquecimiento on-demand de la ficha, resuelto best-effort contra casos-service (6.4).
 *
 * <p>Cuando la costura está desactivada o el cruce falla, {@link #disponible()} es {@code false} y el
 * resto de campos son nulos (degradación a "no disponible").</p>
 *
 * @param disponible               si el cruce con Casos pudo resolverse
 * @param estadoCaso               estado actual del caso (o {@code null})
 * @param numeroExpedienteVigente  número de expediente vigente del caso (o {@code null})
 */
public record EnriquecimientoFicha(boolean disponible, String estadoCaso, String numeroExpedienteVigente) {

    /** @return un bloque que indica enriquecimiento no disponible. */
    public static EnriquecimientoFicha noDisponible() {
        return new EnriquecimientoFicha(false, null, null);
    }
}
