package cl.smid.vulneraciones.dominio.modelo;

import java.time.Instant;

/**
 * Proyección de dominio del evento {@code caso.abierto} emitido por casos-service (6.4), ya
 * deserializado y normalizado.
 *
 * <p>Es la costura de materialización de la FIR: la ficha se crea solo si
 * {@link #requiereFichaReservada()} es {@code true}. El campo {@link #esBeta()} es opcional
 * ({@code Boolean}): si viene informado, tiene prioridad para decidir la serie; si es {@code null},
 * la serie se decide por la política de fecha sobre {@link #ocurridoEn()}.</p>
 *
 * @param idCasoAlt              {@code alt_key} del caso de origen
 * @param ocurridoEn             instante de apertura del caso (UTC)
 * @param numeroExpediente       número de expediente del caso (snapshot)
 * @param idSedeAlt              {@code alt_key} de la sede del caso
 * @param idUnidadAlt            {@code alt_key} de la unidad del caso (puede ser nulo)
 * @param complejidad            complejidad del caso (puede ser nula/desconocida)
 * @param requiereFichaReservada bandera de gating; la FIR se materializa solo si es {@code true}
 * @param esBeta                 serie informada por el caso (opcional; prioridad sobre la fecha)
 */
public record EventoCasoAbierto(
        String idCasoAlt,
        Instant ocurridoEn,
        String numeroExpediente,
        String idSedeAlt,
        String idUnidadAlt,
        Complejidad complejidad,
        boolean requiereFichaReservada,
        Boolean esBeta) {
}
