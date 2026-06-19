package cl.smid.vulneraciones.dominio.puerto.salida;

import java.util.Optional;

/**
 * Puerto de salida (costura on-demand, opcional): validación/etiquetado del caso contra
 * casos-service (6.4) por {@code alt_key}. Best-effort: degrada a vacío ante cualquier fallo o si
 * está desactivado.
 */
public interface ClienteCasos {

    /**
     * Resumen mínimo del caso, usado para verificación best-effort de referencias.
     *
     * @param estado           estado actual del caso
     * @param numeroExpediente número de expediente del caso
     */
    record ResumenCaso(String estado, String numeroExpediente) {
    }

    /**
     * @param idCasoAlt {@code alt_key} del caso
     * @return el resumen del caso si pudo resolverse; {@link Optional#empty()} en caso contrario
     */
    Optional<ResumenCaso> obtener(String idCasoAlt);
}
