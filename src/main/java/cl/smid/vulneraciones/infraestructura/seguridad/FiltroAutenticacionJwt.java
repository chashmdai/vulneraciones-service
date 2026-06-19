package cl.smid.vulneraciones.infraestructura.seguridad;

import cl.smid.vulneraciones.dominio.modelo.Alcance;
import cl.smid.vulneraciones.dominio.modelo.ContextoUsuario;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Filtro de autenticación por JWT (defensa en profundidad en el borde del servicio).
 *
 * <p>Extrae el token {@code Bearer}, lo valida con {@link ValidadorJwt} y, si es válido, construye el
 * {@link ContextoUsuario} a partir de los claims ({@code sub}, {@code roles}, {@code idSede},
 * {@code idUnidad}, {@code alcance}, {@code nombre}), lo publica en el {@link ProveedorContexto} y fija
 * la autenticación en el {@code SecurityContext}. Ante token ausente o inválido <strong>no lanza</strong>:
 * deja la petición sin autenticar para que el punto de entrada responda AUTZ-003 (401) de forma
 * uniforme.</p>
 */
@Component
public class FiltroAutenticacionJwt extends OncePerRequestFilter {

    private static final String CABECERA = "Authorization";
    private static final String PREFIJO = "Bearer ";

    private final ValidadorJwt validador;
    private final ProveedorContexto proveedorContexto;

    public FiltroAutenticacionJwt(ValidadorJwt validador, ProveedorContexto proveedorContexto) {
        this.validador = validador;
        this.proveedorContexto = proveedorContexto;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        String cabecera = request.getHeader(CABECERA);
        if (cabecera != null && cabecera.startsWith(PREFIJO)) {
            String token = cabecera.substring(PREFIJO.length()).trim();
            try {
                Claims claims = validador.validar(token);
                ContextoUsuario contexto = construirContexto(claims);
                proveedorContexto.establecer(request, contexto, token);

                var authorities = contexto.roles().stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList();
                var autenticacion = new UsernamePasswordAuthenticationToken(contexto.sub(), null, authorities);
                autenticacion.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(autenticacion);
            } catch (RuntimeException ex) {
                // Token inválido/expirado/firma incorrecta: se deja sin autenticar (⇒ 401 en el entry point).
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }

    private ContextoUsuario construirContexto(Claims claims) {
        String sub = claims.getSubject();
        Set<String> roles = extraerRoles(claims.get("roles"));
        String idSede = comoTexto(claims.get("idSede"));
        String idUnidad = comoTexto(claims.get("idUnidad"));
        Alcance alcance = Alcance.desde(comoTexto(claims.get("alcance")));
        String nombre = comoTexto(claims.get("nombre"));
        return new ContextoUsuario(sub, roles, idSede, idUnidad, alcance, nombre);
    }

    private Set<String> extraerRoles(Object valor) {
        Set<String> roles = new LinkedHashSet<>();
        if (valor instanceof List<?> lista) {
            for (Object elemento : lista) {
                if (elemento != null) {
                    roles.add(elemento.toString());
                }
            }
        } else if (valor instanceof String texto && !texto.isBlank()) {
            for (String parte : texto.split(",")) {
                if (!parte.isBlank()) {
                    roles.add(parte.trim());
                }
            }
        }
        return roles;
    }

    private String comoTexto(Object valor) {
        return valor == null ? null : valor.toString();
    }
}
