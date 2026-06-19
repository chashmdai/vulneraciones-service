package cl.smid.vulneraciones.dominio.puerto.entrada.comando;

import java.time.LocalDate;

/**
 * Comando de alta de una vulneración dentro de una ficha.
 *
 * @param fichaAltKey  {@code alt_key} de la ficha destino
 * @param idDerechoAlt {@code alt_key} del derecho vulnerado (obligatorio)
 * @param idCausaAlt   {@code alt_key} de la causa (opcional)
 * @param idNnaAlt     {@code alt_key} del NNA afectado (obligatorio)
 * @param gravedad     gravedad textual (LEVE/MEDIA/GRAVE/GRAVISIMA)
 * @param relato       relato reservado en claro (se cifrará en reposo; opcional)
 * @param fechaHecho   fecha del hecho (opcional)
 */
public record ComandoRegistrarVulneracion(
        String fichaAltKey,
        String idDerechoAlt,
        String idCausaAlt,
        String idNnaAlt,
        String gravedad,
        String relato,
        LocalDate fechaHecho) {
}
