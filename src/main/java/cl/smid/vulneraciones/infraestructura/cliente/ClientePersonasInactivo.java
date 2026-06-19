package cl.smid.vulneraciones.infraestructura.cliente;

import cl.smid.vulneraciones.dominio.puerto.salida.ClientePersonas;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Implementación inactiva del cliente de Personas (costura desactivada por defecto). Siempre degrada a
 * vacío; el snapshot {@code nnaNombreLegible} quedará nulo.
 */
@Component
@ConditionalOnProperty(prefix = "smid.enriquecimiento.personas", name = "activo",
        havingValue = "false", matchIfMissing = true)
public class ClientePersonasInactivo implements ClientePersonas {

    @Override
    public Optional<String> nombreLegible(String idNnaAlt) {
        return Optional.empty();
    }
}
