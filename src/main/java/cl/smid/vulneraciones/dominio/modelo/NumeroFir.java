package cl.smid.vulneraciones.dominio.modelo;

import java.util.Objects;

/**
 * Objeto de valor del número de FIR.
 *
 * <p>Formato: {@code FIR-{CODIGO_SEDE}-{[B]CORRELATIVO}/{AÑO}}. El prefijo {@code B} aparece solo en
 * la serie {@link SerieCorrelativo#BETA}. Ejemplos: {@code FIR-RM-1/2027} (oficial),
 * {@code FIR-RM-B1/2027} (beta).</p>
 *
 * @param codigoSede código corto de la sede (p. ej. {@code RM})
 * @param serie      serie del correlativo (OFICIAL/BETA)
 * @param correlativo correlativo unificado por sede/año/serie (positivo)
 * @param anio       año de la serie
 */
public record NumeroFir(String codigoSede, SerieCorrelativo serie, long correlativo, int anio) {

    public NumeroFir {
        Objects.requireNonNull(codigoSede, "codigoSede es obligatorio");
        Objects.requireNonNull(serie, "serie es obligatoria");
        if (correlativo <= 0) {
            throw new IllegalArgumentException("El correlativo debe ser positivo: " + correlativo);
        }
    }

    /**
     * @return el número de FIR formateado como cadena pública.
     */
    public String formatear() {
        return "FIR-" + codigoSede + "-" + serie.prefijo() + correlativo + "/" + anio;
    }

    @Override
    public String toString() {
        return formatear();
    }
}
