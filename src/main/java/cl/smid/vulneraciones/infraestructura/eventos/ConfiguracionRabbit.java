package cl.smid.vulneraciones.infraestructura.eventos;

import cl.smid.vulneraciones.config.PropiedadesEventos;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

/**
 * Topología de consumo de eventos en RabbitMQ (activa solo con {@code smid.eventos.consumo=rabbitmq}).
 *
 * <p>Declara el {@code TopicExchange} de eventos del clúster, la cola de consumo de
 * {@code caso.abierto} con su exchange de mensajes muertos (DLX) y la cola muerta (DLQ), más la
 * fábrica del contenedor de escucha. El contenedor usa un único consumidor (concurrencia 1, para
 * serializar la materialización), reintenta con backoff exponencial (3 intentos) y
 * <strong>no reencola</strong> los rechazos definitivos ({@code defaultRequeueRejected=false}): un
 * evento irrecuperable termina en la DLQ.</p>
 */
@Configuration
@ConditionalOnProperty(name = "smid.eventos.consumo", havingValue = "rabbitmq")
public class ConfiguracionRabbit {

    private final PropiedadesEventos propiedades;

    public ConfiguracionRabbit(PropiedadesEventos propiedades) {
        this.propiedades = propiedades;
    }

    @Bean
    public TopicExchange exchangeEventos() {
        return new TopicExchange(propiedades.exchange(), true, false);
    }

    @Bean
    public FanoutExchange exchangeMuertos() {
        return new FanoutExchange(propiedades.dlx(), true, false);
    }

    @Bean
    public Queue colaCasoAbierto() {
        return QueueBuilder.durable(propiedades.colaCasoAbierto())
                .withArgument("x-dead-letter-exchange", propiedades.dlx())
                .build();
    }

    @Bean
    public Queue colaCasoAbiertoMuerta() {
        return QueueBuilder.durable(propiedades.dlqCasoAbierto()).build();
    }

    @Bean
    public Binding bindingCasoAbierto() {
        return BindingBuilder.bind(colaCasoAbierto())
                .to(exchangeEventos())
                .with(propiedades.routingCasoAbierto());
    }

    @Bean
    public Binding bindingCasoAbiertoMuerta() {
        return BindingBuilder.bind(colaCasoAbiertoMuerta()).to(exchangeMuertos());
    }

    /**
     * Interceptor de reintentos stateless: 3 intentos con backoff exponencial (1s, x2, tope 10s).
     * Agotados los intentos, el mensaje se rechaza y, por {@code defaultRequeueRejected=false}, se
     * deriva a la DLQ.
     */
    @Bean
    public RetryOperationsInterceptor interceptorReintentosEventos() {
        return RetryInterceptorBuilder.stateless()
                .maxAttempts(3)
                .backOffOptions(1000, 2.0, 10000)
                .build();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter convertidorJsonEventos,
            RetryOperationsInterceptor interceptorReintentosEventos) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(convertidorJsonEventos);
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(1);
        factory.setDefaultRequeueRejected(false);
        factory.setAdviceChain(interceptorReintentosEventos);
        return factory;
    }
}
