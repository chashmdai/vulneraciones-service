package cl.smid.vulneraciones.infraestructura.soporte;

import cl.smid.vulneraciones.dominio.puerto.salida.Reloj;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Adaptador del puerto {@link Reloj} sobre el reloj del sistema. Devuelve siempre instantes en UTC.
 */
@Component
public class RelojSistema implements Reloj {

    @Override
    public Instant ahora() {
        return Instant.now();
    }
}
