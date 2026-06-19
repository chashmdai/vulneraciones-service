package cl.smid.vulneraciones.infraestructura.eventos;

import cl.smid.vulneraciones.dominio.excepcion.EventoMalformadoException;
import cl.smid.vulneraciones.dominio.excepcion.FichaDuplicadaException;
import cl.smid.vulneraciones.dominio.modelo.Complejidad;
import cl.smid.vulneraciones.dominio.modelo.EventoCasoAbierto;
import cl.smid.vulneraciones.dominio.puerto.entrada.MaterializarFicha;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consumidor del evento {@code caso.abierto}: frontera transaccional de la materialización de la FIR.
 *
 * <p>Activo solo con {@code smid.eventos.consumo=rabbitmq} (en aislamiento/local no consume). El
 * método es la única frontera {@code @Transactional} del flujo de materialización: la reserva del
 * correlativo, el alta de la ficha y el asiento de apertura ocurren de forma atómica.</p>
 *
 * <p>Política de errores:</p>
 * <ul>
 *   <li><strong>Evento malformado</strong> (estructura mínima ausente) ⇒ se relanza como
 *   {@link AmqpRejectAndDontRequeueException} para derivarlo a la DLQ sin reencolar.</li>
 *   <li><strong>Ficha duplicada</strong> (carrera entre instancias) ⇒ se deja propagar: el contenedor
 *   reintenta y el pre-chequeo de idempotencia resuelve la reentrega como no-op.</li>
 *   <li><strong>Gating en falso</strong> ({@code requiereFichaReservada != true}) ⇒ no-op con ack
 *   limpio (resuelto en el dominio).</li>
 * </ul>
 */
@Component
@ConditionalOnProperty(name = "smid.eventos.consumo", havingValue = "rabbitmq")
public class ListenerCasoAbierto {

    private final MaterializarFicha materializarFicha;

    public ListenerCasoAbierto(MaterializarFicha materializarFicha) {
        this.materializarFicha = materializarFicha;
    }

    @RabbitListener(queues = "${smid.eventos.cola-caso-abierto}")
    @Transactional
    public void onCasoAbierto(PayloadEventoEntrante payload) {
        EventoCasoAbierto evento = mapear(payload);
        try {
            materializarFicha.materializar(evento);
        } catch (EventoMalformadoException ex) {
            // No reintentable: a la DLQ.
            throw new AmqpRejectAndDontRequeueException(ex.getMessage(), ex);
        } catch (FichaDuplicadaException ex) {
            // Carrera entre instancias: se deja propagar para reintento (el pre-chequeo lo resolverá).
            throw ex;
        }
    }

    /**
     * Mapea el payload a la proyección de dominio, validando la estructura mínima imprescindible.
     *
     * @throws AmqpRejectAndDontRequeueException si el mensaje no tiene la estructura mínima (DLQ)
     */
    private EventoCasoAbierto mapear(PayloadEventoEntrante payload) {
        if (payload == null || esVacio(payload.tipo()) || esVacio(payload.altKey()) || payload.metadatos() == null) {
            throw new AmqpRejectAndDontRequeueException(
                    "Evento caso.abierto estructuralmente inválido (faltan tipo/altKey/metadatos).");
        }
        PayloadEventoEntrante.Metadatos m = payload.metadatos();
        return new EventoCasoAbierto(
                payload.altKey(),
                payload.ocurridoEn(),
                m.numeroExpediente(),
                m.idSede(),
                m.idUnidad(),
                Complejidad.desde(m.complejidad()),
                Boolean.TRUE.equals(m.requiereFichaReservada()),
                m.esBeta());
    }

    private boolean esVacio(String valor) {
        return valor == null || valor.isBlank();
    }
}
