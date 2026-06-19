package cl.smid.vulneraciones.dominio.modelo.vista;

import cl.smid.vulneraciones.dominio.modelo.TipoAntecedente;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Vista de lectura de un antecedente, con la descripción reservada ya resuelta según el rol del
 * solicitante (en claro si está autorizado; {@code null} si fue redactada).
 *
 * @param altKey        identificador opaco
 * @param tipo          tipo del antecedente
 * @param descripcion   descripción en claro si está autorizada; {@code null} si redactada
 * @param fecha         fecha del antecedente (puede ser nula)
 * @param fuente        fuente (puede ser nula)
 * @param registradoEn  instante de registro (UTC)
 * @param registradoPor {@code alt_key} del autor
 */
public record VistaAntecedente(
        String altKey,
        TipoAntecedente tipo,
        String descripcion,
        LocalDate fecha,
        String fuente,
        Instant registradoEn,
        String registradoPor) {
}
