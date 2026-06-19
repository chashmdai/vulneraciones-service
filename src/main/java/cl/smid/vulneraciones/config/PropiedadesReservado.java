package cl.smid.vulneraciones.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración de la protección en reposo de datos reservados ({@code smid.reservado.*}, DT-1).
 *
 * @param cifrado perfil de cifrado: {@code aes-gcm} (producción) o {@code none} (passthrough, solo
 *                desarrollo/pruebas con datos sintéticos)
 * @param clave   clave AES-256 en Base64 (32 bytes) para el perfil {@code aes-gcm}; entregada por
 *                variable de entorno
 */
@ConfigurationProperties(prefix = "smid.reservado")
public record PropiedadesReservado(
        String cifrado,
        String clave) {
}
