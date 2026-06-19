package cl.smid.vulneraciones.dominio.puerto.entrada.comando;

import java.time.LocalDate;

/**
 * Comando de alta de un antecedente dentro de una ficha.
 *
 * @param fichaAltKey {@code alt_key} de la ficha destino
 * @param tipo        tipo textual del antecedente (DERIVACION/PERICIA/...)
 * @param descripcion descripción reservada en claro (se cifrará en reposo; opcional)
 * @param fecha       fecha del antecedente (opcional)
 * @param fuente      fuente del antecedente (opcional)
 */
public record ComandoRegistrarAntecedente(
        String fichaAltKey,
        String tipo,
        String descripcion,
        LocalDate fecha,
        String fuente) {
}
