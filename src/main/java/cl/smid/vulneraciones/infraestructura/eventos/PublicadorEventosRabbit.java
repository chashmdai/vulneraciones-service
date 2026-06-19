package cl.smid.vulneraciones.infraestructura.eventos;

import cl.smid.vulneraciones.config.PropiedadesEventos;
import cl.smid.vulneraciones.dominio.puerto.salida.EventoDominio;
import cl.smid.vulneraciones.dominio.puerto.salida.PublicadorEventos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Publicador de eventos sobre RabbitMQ (activo con {@code smid.eventos.transporte=rabbitmq}).
 *
 * <p>Emite al topic exchange del clúster usando el tipo del evento como routing key. El cuerpo es
 * metadata-only (G7): jamás relatos, nombres ni RUT. Tolerante a fallos: ante cualquier error de
 * transporte registra una advertencia y continúa, sin deshacer el negocio ya confirmado.</p>
 */
@Component
@ConditionalOnProperty(name = "smid.eventos.transporte", havingValue = "rabbitmq")
public class PublicadorEventosRabbit implements PublicadorEventos {

    private static final Logger log = LoggerFactory.getLogger(PublicadorEventosRabbit.class);

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;

    public PublicadorEventosRabbit(RabbitTemplate rabbitTemplate, PropiedadesEventos propiedades) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = propiedades.exchange();
    }

    @Override
    public void publicar(EventoDominio evento) {
        try {
            Map<String, Object> cuerpo = new LinkedHashMap<>();
            cuerpo.put("tipo", evento.tipo());
            cuerpo.put("altKey", evento.altKey());
            cuerpo.put("ocurridoEn", evento.ocurridoEn());
            cuerpo.put("metadatos", evento.metadatos());
            rabbitTemplate.convertAndSend(exchange, evento.tipo(), cuerpo);
        } catch (RuntimeException ex) {
            log.warn("Fallo al publicar el evento {} en RabbitMQ (se continúa sin interrumpir el negocio).",
                    evento.tipo(), ex);
        }
    }
}
