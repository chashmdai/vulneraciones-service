package cl.smid.vulneraciones.infraestructura.cliente;

import cl.smid.vulneraciones.dominio.puerto.salida.ClienteCatalogo;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Implementación inactiva del cliente de Catálogo (costura desactivada por defecto). Siempre degrada a
 * vacío; las etiquetas de derecho/causa quedarán nulas.
 */
@Component
@ConditionalOnProperty(prefix = "smid.enriquecimiento.catalogo", name = "activo",
        havingValue = "false", matchIfMissing = true)
public class ClienteCatalogoInactivo implements ClienteCatalogo {

    @Override
    public Optional<String> etiquetaDerecho(String idDerechoAlt) {
        return Optional.empty();
    }

    @Override
    public Optional<String> etiquetaCausa(String idCausaAlt) {
        return Optional.empty();
    }
}
