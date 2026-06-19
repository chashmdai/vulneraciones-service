package cl.smid.vulneraciones.infraestructura.seguridad;

import cl.smid.vulneraciones.dominio.modelo.ContextoUsuario;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * Portador del contexto de sesión durante el ciclo de una petición HTTP.
 *
 * <p>Se respalda en <strong>atributos de la petición</strong> (no en un bean de ámbito de petición):
 * el filtro de autenticación los deposita escribiendo directamente sobre el {@link HttpServletRequest}
 * (que tiene a mano), evitando la dependencia del enlace de {@code RequestContextHolder} en la fase de
 * filtros. Las lecturas ocurren durante la ejecución del controlador (y de los clientes REST de
 * enriquecimiento), cuando el {@code DispatcherServlet} ya ligó la petición al hilo.</p>
 *
 * <p>El listener de RabbitMQ no opera dentro de una petición; por eso la materialización nunca usa
 * este portador ni los clientes de enriquecimiento.</p>
 */
@Component
public class ProveedorContexto {

    private static final String ATTR_CONTEXTO = "smid.vulneraciones.contexto";
    private static final String ATTR_TOKEN = "smid.vulneraciones.token";

    /** @return el contexto de sesión de la petición en curso, o {@code null} si no autenticada. */
    public ContextoUsuario contexto() {
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        if (ra == null) {
            return null;
        }
        Object valor = ra.getAttribute(ATTR_CONTEXTO, RequestAttributes.SCOPE_REQUEST);
        return valor instanceof ContextoUsuario contexto ? contexto : null;
    }

    /** @return el token en bruto (sin el prefijo {@code Bearer}) de la petición en curso, o {@code null}. */
    public String tokenBruto() {
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        if (ra == null) {
            return null;
        }
        Object valor = ra.getAttribute(ATTR_TOKEN, RequestAttributes.SCOPE_REQUEST);
        return valor instanceof String token ? token : null;
    }

    /** Deposita el contexto y el token en los atributos de la petición (uso exclusivo del filtro). */
    public void establecer(HttpServletRequest request, ContextoUsuario contexto, String tokenBruto) {
        request.setAttribute(ATTR_CONTEXTO, contexto);
        request.setAttribute(ATTR_TOKEN, tokenBruto);
    }
}
