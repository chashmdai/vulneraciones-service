package cl.smid.vulneraciones.infraestructura.soporte;

import cl.smid.vulneraciones.config.PropiedadesSedes;
import cl.smid.vulneraciones.dominio.puerto.salida.DirectorioSedes;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Adaptador del puerto {@link DirectorioSedes}: resuelve el código corto de una sede a partir del
 * mapa de configuración {@code smid.sedes.codigos.<alt_key>}, con un código de respaldo
 * ({@code smid.sedes.codigo-defecto}) cuando la sede no está mapeada.
 *
 * <p>El token solo transporta el {@code alt_key} de la sede; el código corto que compone el número de
 * FIR (p. ej. {@code RM}) se administra por configuración, no se deriva del token.</p>
 */
@Component
public class DirectorioSedesConfig implements DirectorioSedes {

    private final Map<String, String> codigos;
    private final String codigoDefecto;

    public DirectorioSedesConfig(PropiedadesSedes propiedades) {
        this.codigos = propiedades.codigos() == null ? Map.of() : Map.copyOf(propiedades.codigos());
        this.codigoDefecto = (propiedades.codigoDefecto() == null || propiedades.codigoDefecto().isBlank())
                ? "SD" : propiedades.codigoDefecto().trim();
    }

    @Override
    public String codigoDe(String idSedeAlt) {
        if (idSedeAlt == null) {
            return codigoDefecto;
        }
        return codigos.getOrDefault(idSedeAlt, codigoDefecto);
    }
}
