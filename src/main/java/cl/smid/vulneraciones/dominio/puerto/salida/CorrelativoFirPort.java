package cl.smid.vulneraciones.dominio.puerto.salida;

import cl.smid.vulneraciones.dominio.modelo.SerieCorrelativo;

/**
 * Puerto de salida: reserva atómica del correlativo de FIR.
 *
 * <p>La reserva debe ser segura ante concurrencia y única por {@code (sede, año, serie)}; la serie
 * BETA queda aislada de la OFICIAL. La implementación corre dentro de la transacción del listener,
 * de modo que un rollback no deja huecos.</p>
 */
public interface CorrelativoFirPort {

    /**
     * Reserva el siguiente correlativo para la combinación dada.
     *
     * @param idSedeAlt {@code alt_key} de la sede
     * @param anio      año de la serie
     * @param serie     serie (OFICIAL/BETA)
     * @return el correlativo reservado (≥ 1), consecutivo y único dentro de la combinación
     */
    long reservar(String idSedeAlt, int anio, SerieCorrelativo serie);
}
