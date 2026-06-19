package cl.smid.vulneraciones.dominio.modelo.vista;

import cl.smid.vulneraciones.dominio.modelo.Gravedad;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Vista de lectura de una vulneración, con el campo reservado ya resuelto según el rol del solicitante
 * (relato en claro si está autorizado; {@code null} si fue redactado). Las etiquetas de catálogo son
 * enriquecimiento opcional (nulas si la costura está desactivada o el cruce falla).
 *
 * @param altKey           identificador opaco
 * @param idDerechoAlt     {@code alt_key} del derecho
 * @param idCausaAlt       {@code alt_key} de la causa (puede ser nulo)
 * @param idNnaAlt         {@code alt_key} del NNA
 * @param nnaNombreLegible snapshot del nombre legible del NNA (puede ser nulo)
 * @param etiquetaDerecho  etiqueta legible del derecho (enriquecimiento opcional; puede ser nula)
 * @param etiquetaCausa    etiqueta legible de la causa (enriquecimiento opcional; puede ser nula)
 * @param gravedad         gravedad
 * @param relato           relato en claro si está autorizado; {@code null} si redactado
 * @param fechaHecho       fecha del hecho (puede ser nula)
 * @param registradoEn     instante de registro (UTC)
 * @param registradoPor    {@code alt_key} del autor
 */
public record VistaVulneracion(
        String altKey,
        String idDerechoAlt,
        String idCausaAlt,
        String idNnaAlt,
        String nnaNombreLegible,
        String etiquetaDerecho,
        String etiquetaCausa,
        Gravedad gravedad,
        String relato,
        LocalDate fechaHecho,
        Instant registradoEn,
        String registradoPor) {
}
