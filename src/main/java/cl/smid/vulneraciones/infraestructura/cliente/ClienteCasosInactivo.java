package cl.smid.vulneraciones.infraestructura.cliente;

import cl.smid.vulneraciones.dominio.puerto.salida.ClienteCasos;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Implementación inactiva del cliente de Casos (costura desactivada por defecto). Permite ejecutar el
 * servicio en aislamiento: siempre degrada a vacío.
 */
@Component
@ConditionalOnProperty(prefix = "smid.enriquecimiento.casos", name = "activo",
        havingValue = "false", matchIfMissing = true)
public class ClienteCasosInactivo implements ClienteCasos {

    @Override
    public Optional<ResumenCaso> obtener(String idCasoAlt) {
        return Optional.empty();
    }
}
