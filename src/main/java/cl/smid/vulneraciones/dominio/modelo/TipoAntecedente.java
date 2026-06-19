package cl.smid.vulneraciones.dominio.modelo;

import cl.smid.vulneraciones.dominio.excepcion.ReglaNegocioException;

/**
 * Tipo (clasificación) de un antecedente de contexto registrado en la FIR.
 *
 * <p>Se persiste como {@code VARCHAR + CHECK} (nunca como {@code ENUM} nativo de MySQL).</p>
 */
public enum TipoAntecedente {

    DERIVACION,
    PERICIA,
    ANTECEDENTE_FAMILIAR,
    ESCOLAR,
    SALUD,
    OTRO;

    /**
     * Resuelve el tipo a partir de su nombre, exigiendo un valor válido.
     *
     * @param texto nombre del tipo
     * @return el tipo correspondiente
     * @throws ReglaNegocioException si el texto es nulo, en blanco o desconocido (VUL-422)
     */
    public static TipoAntecedente exigir(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new ReglaNegocioException("El tipo de antecedente es obligatorio.");
        }
        String normalizado = texto.trim().toUpperCase();
        for (TipoAntecedente tipo : values()) {
            if (tipo.name().equals(normalizado)) {
                return tipo;
            }
        }
        throw new ReglaNegocioException("Tipo de antecedente no reconocido: '" + texto
                + "'. Valores válidos: DERIVACION, PERICIA, ANTECEDENTE_FAMILIAR, ESCOLAR, SALUD, OTRO.");
    }

    /**
     * Resuelve el tipo de forma tolerante (para reconstrucción desde persistencia).
     *
     * @param texto nombre del tipo (admite nulos)
     * @return el tipo correspondiente o {@code null} si es nulo/en blanco/desconocido
     */
    public static TipoAntecedente desde(String texto) {
        if (texto == null) {
            return null;
        }
        String normalizado = texto.trim().toUpperCase();
        for (TipoAntecedente tipo : values()) {
            if (tipo.name().equals(normalizado)) {
                return tipo;
            }
        }
        return null;
    }
}
