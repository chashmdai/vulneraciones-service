package cl.smid.vulneraciones.dominio.modelo;

import cl.smid.vulneraciones.dominio.excepcion.ReglaNegocioException;

/**
 * Gravedad de una vulneración de derecho registrada en la FIR.
 *
 * <p>Se persiste como {@code VARCHAR + CHECK} (nunca como {@code ENUM} nativo de MySQL).</p>
 */
public enum Gravedad {

    LEVE,
    MEDIA,
    GRAVE,
    GRAVISIMA;

    /**
     * Resuelve la gravedad a partir de su nombre, exigiendo un valor válido.
     *
     * @param texto nombre de la gravedad
     * @return la gravedad correspondiente
     * @throws ReglaNegocioException si el texto es nulo, en blanco o desconocido (VUL-422)
     */
    public static Gravedad exigir(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new ReglaNegocioException("La gravedad de la vulneración es obligatoria.");
        }
        String normalizado = texto.trim().toUpperCase();
        for (Gravedad gravedad : values()) {
            if (gravedad.name().equals(normalizado)) {
                return gravedad;
            }
        }
        throw new ReglaNegocioException(
                "Gravedad no reconocida: '" + texto + "'. Valores válidos: LEVE, MEDIA, GRAVE, GRAVISIMA.");
    }

    /**
     * Resuelve la gravedad de forma tolerante (para reconstrucción desde persistencia).
     *
     * @param texto nombre de la gravedad (admite nulos)
     * @return la gravedad correspondiente o {@code null} si es nulo/en blanco/desconocido
     */
    public static Gravedad desde(String texto) {
        if (texto == null) {
            return null;
        }
        String normalizado = texto.trim().toUpperCase();
        for (Gravedad gravedad : values()) {
            if (gravedad.name().equals(normalizado)) {
                return gravedad;
            }
        }
        return null;
    }
}
