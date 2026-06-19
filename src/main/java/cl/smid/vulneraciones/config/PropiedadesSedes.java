package cl.smid.vulneraciones.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * Directorio de códigos de sede ({@code smid.sedes.*}).
 *
 * <p>El token solo transporta el {@code alt_key} de la sede; el código corto que compone el número de
 * FIR se administra aquí, mapeando {@code alt_key → código} (p. ej. {@code RM}).</p>
 *
 * @param codigos       mapa {@code alt_key de sede → código corto}
 * @param codigoDefecto código de respaldo cuando la sede no está mapeada (por defecto {@code SD})
 */
@ConfigurationProperties(prefix = "smid.sedes")
public record PropiedadesSedes(
        Map<String, String> codigos,
        String codigoDefecto) {
}
