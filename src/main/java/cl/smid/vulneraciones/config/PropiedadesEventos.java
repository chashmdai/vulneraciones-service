package cl.smid.vulneraciones.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración de mensajería de dominio ({@code smid.eventos.*}).
 *
 * <p>El transporte de publicación y el consumo son conmutables, lo que permite ejecutar en
 * aislamiento (solo log, sin consumir de RabbitMQ).</p>
 *
 * @param transporte         transporte de publicación: {@code log} o {@code rabbitmq}
 * @param consumo            consumo de {@code caso.abierto}: {@code none} o {@code rabbitmq}
 * @param exchange           nombre del topic exchange de eventos del clúster
 * @param colaCasoAbierto    nombre de la cola de consumo de {@code caso.abierto}
 * @param dlx                nombre del exchange de mensajes muertos (DLX)
 * @param dlqCasoAbierto     nombre de la cola de mensajes muertos (DLQ)
 * @param routingCasoAbierto routing key del evento consumido
 */
@ConfigurationProperties(prefix = "smid.eventos")
public record PropiedadesEventos(
        String transporte,
        String consumo,
        String exchange,
        String colaCasoAbierto,
        String dlx,
        String dlqCasoAbierto,
        String routingCasoAbierto) {
}
