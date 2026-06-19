package cl.smid.vulneraciones.soporte;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

/**
 * Generador de JWT HS256 para pruebas de integración. Emite tokens equivalentes a los del
 * auth-service del clúster: mismo {@code kid}, emisor, audiencia y secreto que valida el servicio.
 */
public final class GeneradorTokensPrueba {

    /** Secreto de pruebas (>= 32 bytes). Debe coincidir con el inyectado en el contexto de la IT. */
    public static final String SECRETO = "secreto-de-integracion-pruebas-smid-vulneraciones-2026-0123456789";
    public static final String KID = "smid-2026-06";
    public static final String ISSUER = "smid-auth";
    public static final String AUDIENCE = "smid-servicios";

    private static final SecretKey CLAVE = Keys.hmacShaKeyFor(SECRETO.getBytes(StandardCharsets.UTF_8));

    private GeneradorTokensPrueba() {
    }

    /**
     * Construye un token firmado.
     *
     * @param sub      identificador del usuario
     * @param roles    roles del usuario
     * @param idSede   alt_key de la sede
     * @param idUnidad alt_key de la unidad
     * @param alcance  alcance territorial (UNIDAD/SEDE/NACIONAL)
     * @return el JWT compacto
     */
    public static String token(String sub, List<String> roles, String idSede, String idUnidad, String alcance) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .header().keyId(KID).and()
                .subject(sub)
                .issuer(ISSUER)
                .audience().add(AUDIENCE).and()
                .claim("roles", roles)
                .claim("idSede", idSede)
                .claim("idUnidad", idUnidad)
                .claim("alcance", alcance)
                .claim("nombre", "Usuario " + sub)
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plusSeconds(3600)))
                .signWith(CLAVE, Jwts.SIG.HS256)
                .compact();
    }
}
