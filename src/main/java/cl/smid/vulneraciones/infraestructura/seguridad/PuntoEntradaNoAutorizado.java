package cl.smid.vulneraciones.infraestructura.seguridad;

import cl.smid.vulneraciones.api.error.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Punto de entrada de autenticación: ante una petición no autenticada escribe el sobre de error
 * unificado con código AUTZ-003 y estado 401, de forma coherente con el resto de la API.
 */
@Component
public class PuntoEntradaNoAutorizado implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public PuntoEntradaNoAutorizado(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        ErrorResponse cuerpo = ErrorResponse.de(
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                "AUTZ-003",
                "Autenticación requerida: token ausente o inválido.",
                null,
                request.getRequestURI());
        objectMapper.writeValue(response.getWriter(), cuerpo);
    }
}
