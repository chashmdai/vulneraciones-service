package cl.smid.vulneraciones.infraestructura.seguridad;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Locator;
import io.jsonwebtoken.ProtectedHeader;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Validador de JWT del clúster SMID (defensa en profundidad en el borde de cada servicio).
 *
 * <p>Verifica la firma HS256 seleccionando la clave por el {@code kid} del encabezado (par
 * <strong>activo/previo</strong> para soportar rotación), exige el emisor ({@code iss = smid-auth}),
 * comprueba que la audiencia ({@code aud}) contenga {@code smid-servicios} y deja que el parser
 * valide la expiración ({@code exp}). No confía en el gateway: revalida íntegramente el token.</p>
 *
 * <p>Las claves se derivan de los secretos por UTF-8 ({@link Keys#hmacShaKeyFor(byte[])} exige ≥ 256
 * bits, es decir ≥ 32 bytes). Cualquier fallo de validación se traduce, en el filtro, a AUTZ-003.</p>
 */
@Component
public class ValidadorJwt {

    private final Map<String, SecretKey> claves = new LinkedHashMap<>();
    private final String kidActivo;
    private final String issuer;
    private final String audience;

    public ValidadorJwt(PropiedadesJwt propiedades) {
        this.issuer = propiedades.issuer();
        this.audience = propiedades.audience();
        this.kidActivo = propiedades.kidActivo();
        if (propiedades.kidActivo() == null || propiedades.secretoActivo() == null) {
            throw new IllegalStateException(
                    "Configuración JWT incompleta: se requieren smid.jwt.kid-activo y smid.jwt.secreto-activo.");
        }
        claves.put(propiedades.kidActivo(), clave(propiedades.secretoActivo()));
        if (propiedades.kidPrevio() != null && !propiedades.kidPrevio().isBlank()
                && propiedades.secretoPrevio() != null && !propiedades.secretoPrevio().isBlank()) {
            claves.put(propiedades.kidPrevio(), clave(propiedades.secretoPrevio()));
        }
    }

    private SecretKey clave(String secreto) {
        return Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Valida el token y devuelve sus claims.
     *
     * @param token JWT compacto (sin el prefijo {@code Bearer})
     * @return los claims verificados
     * @throws RuntimeException (de jjwt o {@link IllegalArgumentException}) si el token es inválido,
     *         su firma no verifica, está expirado, el emisor no coincide o la audiencia no incluye la
     *         requerida
     */
    public Claims validar(String token) {
        JwtParser parser = Jwts.parser()
                .keyLocator(new LocalizadorPorKid())
                .requireIssuer(issuer)
                .build();
        Claims claims = parser.parseSignedClaims(token).getPayload();
        Set<String> aud = claims.getAudience();
        if (aud == null || !aud.contains(audience)) {
            throw new IllegalArgumentException(
                    "La audiencia del token no incluye '" + audience + "'.");
        }
        return claims;
    }

    /**
     * Localizador de la clave de verificación a partir del {@code kid} del encabezado protegido. Si el
     * token no informa {@code kid}, se recurre a la clave activa (robustez ante emisores sin {@code kid}).
     */
    private final class LocalizadorPorKid implements Locator<Key> {
        @Override
        public Key locate(Header header) {
            if (header instanceof ProtectedHeader protegido) {
                String kid = protegido.getKeyId();
                if (kid != null && claves.containsKey(kid)) {
                    return claves.get(kid);
                }
            }
            return claves.get(kidActivo);
        }
    }
}
