package cl.smid.vulneraciones.dominio.modelo.vista;

import cl.smid.vulneraciones.dominio.modelo.Complejidad;
import cl.smid.vulneraciones.dominio.modelo.EstadoFicha;
import cl.smid.vulneraciones.dominio.modelo.Transicion;

import java.time.Instant;
import java.util.List;

/**
 * Vista de lectura del detalle de una FIR, con los campos reservados ya resueltos según el rol del
 * solicitante. La bandera {@link #reservadoOculto()} indica si el contenido reservado fue redactado.
 *
 * @param altKey           identificador opaco de la ficha
 * @param numeroFicha      número de FIR
 * @param idCasoAlt        {@code alt_key} del caso
 * @param numeroExpediente número de expediente (snapshot al materializar)
 * @param estado           estado actual
 * @param complejidad      complejidad (puede ser nula)
 * @param idSedeAlt        {@code alt_key} de la sede
 * @param idUnidadAlt      {@code alt_key} de la unidad (puede ser nulo)
 * @param esBeta           si la ficha pertenece a la serie beta
 * @param abiertaEn        instante de apertura (UTC)
 * @param cerradaEn        instante de cierre (UTC, nulo si no está cerrada)
 * @param creadaEn         instante de creación (UTC)
 * @param actualizadaEn    instante de última actualización (UTC)
 * @param reservadoOculto  {@code true} si el contenido reservado fue redactado para el solicitante
 * @param enriquecimiento  bloque de enriquecimiento on-demand contra Casos (best-effort)
 * @param vulneraciones    vulneraciones (con relato resuelto/redactado)
 * @param antecedentes     antecedentes (con descripción resuelta/redactada)
 * @param historial        historial de transiciones
 */
public record VistaFicha(
        String altKey,
        String numeroFicha,
        String idCasoAlt,
        String numeroExpediente,
        EstadoFicha estado,
        Complejidad complejidad,
        String idSedeAlt,
        String idUnidadAlt,
        boolean esBeta,
        Instant abiertaEn,
        Instant cerradaEn,
        Instant creadaEn,
        Instant actualizadaEn,
        boolean reservadoOculto,
        EnriquecimientoFicha enriquecimiento,
        List<VistaVulneracion> vulneraciones,
        List<VistaAntecedente> antecedentes,
        List<Transicion> historial) {
}
