package cl.smid.vulneraciones.dominio.puerto.entrada;

import cl.smid.vulneraciones.dominio.modelo.EventoCasoAbierto;

/**
 * Caso de uso: materialización asíncrona de la FIR desde el evento {@code caso.abierto}.
 *
 * <p>Aplica el gating por {@code requiereFichaReservada} y la idempotencia por caso. Lo invoca el
 * listener dentro de su transacción.</p>
 */
public interface MaterializarFicha {

    /**
     * Materializa la FIR si corresponde (gating + idempotencia).
     *
     * @param evento evento de apertura del caso (ya validado estructuralmente)
     * @throws cl.smid.vulneraciones.dominio.excepcion.FichaDuplicadaException en carrera entre
     *         instancias (el contenedor reintenta y el pre-chequeo la resuelve como no-op)
     * @throws cl.smid.vulneraciones.dominio.excepcion.EventoMalformadoException si faltan datos
     *         mínimos para materializar
     */
    void materializar(EventoCasoAbierto evento);
}
