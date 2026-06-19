package cl.smid.vulneraciones.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración de las costuras de enriquecimiento on-demand ({@code smid.enriquecimiento.*}).
 *
 * <p>Cada conector se activa de forma independiente. Desactivados por defecto: el servicio opera en
 * aislamiento sin necesidad de los servicios hermanos. Cuando un conector está activo, su URL base
 * apunta al servicio correspondiente (a través del gateway o directo).</p>
 *
 * @param casos    conector hacia casos-service (6.4)
 * @param personas conector hacia personas-service (6.2)
 * @param catalogo conector hacia catalogo-service (6.7)
 */
@ConfigurationProperties(prefix = "smid.enriquecimiento")
public record PropiedadesEnriquecimiento(
        Conector casos,
        Conector personas,
        Conector catalogo) {

    /**
     * Conector individual.
     *
     * @param activo si la costura está habilitada
     * @param url    URL base del servicio destino
     */
    public record Conector(boolean activo, String url) {
    }
}
