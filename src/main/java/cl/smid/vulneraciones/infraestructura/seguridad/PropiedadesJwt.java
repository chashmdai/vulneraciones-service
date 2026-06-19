package cl.smid.vulneraciones.infraestructura.seguridad;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propiedades de validación del JWT ({@code smid.jwt.*}).
 *
 * <p>Configuración con par <strong>activo/previo</strong> de claves para soportar rotación por
 * {@code kid}: no se usa un mapa YAML con clave {@code kid} dinámica (los marcadores {@code ${VAR}} no
 * resuelven sobre claves dinámicas de YAML). El secreto se entrega siempre por variable de entorno
 * (DT-2). El previo es opcional (ventana de rotación).</p>
 *
 * @param issuer        emisor esperado ({@code iss}); debe ser {@code smid-auth}
 * @param audience      audiencia requerida ({@code aud} debe contenerla); {@code smid-servicios}
 * @param kidActivo     identificador de la clave activa de firma
 * @param secretoActivo secreto HS256 de la clave activa (UTF-8, ≥ 32 bytes)
 * @param kidPrevio     identificador de la clave previa (opcional, para rotación)
 * @param secretoPrevio secreto HS256 de la clave previa (opcional)
 */
@ConfigurationProperties(prefix = "smid.jwt")
public record PropiedadesJwt(
        String issuer,
        String audience,
        String kidActivo,
        String secretoActivo,
        String kidPrevio,
        String secretoPrevio) {
}
