package cl.smid.vulneraciones.infraestructura.soporte;

import cl.smid.vulneraciones.dominio.puerto.salida.GeneradorIdentificadores;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Adaptador del puerto {@link GeneradorIdentificadores}: genera identificadores opacos UUID v4 en su
 * representación canónica de 36 caracteres.
 */
@Component
public class GeneradorUuid implements GeneradorIdentificadores {

    @Override
    public String nuevoUuid() {
        return UUID.randomUUID().toString();
    }
}
