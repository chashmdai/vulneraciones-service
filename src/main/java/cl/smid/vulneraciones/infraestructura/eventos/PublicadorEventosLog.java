package cl.smid.vulneraciones.infraestructura.eventos;

import cl.smid.vulneraciones.dominio.puerto.salida.EventoDominio;
import cl.smid.vulneraciones.dominio.puerto.salida.PublicadorEventos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Publicador de eventos por log (transporte por defecto, apto para aislamiento).
 *
 * <p>Registra el sobre del evento (metadata-only, G7) sin emitir a un broker. Tolerante a fallos: no
 * propaga excepciones. Activo con {@code smid.eventos.transporte=log} o si no se configura transporte.</p>
 */
@Component
@ConditionalOnProperty(name = "smid.eventos.transporte", havingValue = "log", matchIfMissing = true)
public class PublicadorEventosLog implements PublicadorEventos {

    private static final Logger log = LoggerFactory.getLogger(PublicadorEventosLog.class);

    @Override
    public void publicar(EventoDominio evento) {
        try {
            log.info("EVENTO tipo={} altKey={} ocurridoEn={} metadatos={}",
                    evento.tipo(), evento.altKey(), evento.ocurridoEn(), evento.metadatos());
        } catch (RuntimeException ex) {
            log.warn("No se pudo registrar el evento {} (se continúa sin interrumpir el negocio).",
                    evento.tipo(), ex);
        }
    }
}
