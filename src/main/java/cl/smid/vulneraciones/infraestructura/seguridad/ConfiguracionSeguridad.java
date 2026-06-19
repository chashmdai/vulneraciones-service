package cl.smid.vulneraciones.infraestructura.seguridad;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración de seguridad HTTP del servicio.
 *
 * <p>API sin estado (sin sesión, sin CSRF, sin formularios): toda petición exige autenticación por
 * JWT. El {@link FiltroAutenticacionJwt} se inserta antes del filtro de usuario/contraseña y el
 * {@link PuntoEntradaNoAutorizado} unifica la respuesta 401 (AUTZ-003). La autorización por rol
 * (Coordinación, acceso reservado) y el control territorial se resuelven en el dominio, no aquí, para
 * mantener un único punto de verdad y poder traducir la denegación territorial a 404.</p>
 */
@Configuration
@EnableWebSecurity
public class ConfiguracionSeguridad {

    private final FiltroAutenticacionJwt filtroAutenticacionJwt;
    private final PuntoEntradaNoAutorizado puntoEntradaNoAutorizado;

    public ConfiguracionSeguridad(FiltroAutenticacionJwt filtroAutenticacionJwt,
                                  PuntoEntradaNoAutorizado puntoEntradaNoAutorizado) {
        this.filtroAutenticacionJwt = filtroAutenticacionJwt;
        this.puntoEntradaNoAutorizado = puntoEntradaNoAutorizado;
    }

    @Bean
    public SecurityFilterChain cadenaFiltros(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .anyRequest().authenticated())
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(puntoEntradaNoAutorizado))
                .addFilterBefore(filtroAutenticacionJwt, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
