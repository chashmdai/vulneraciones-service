package cl.smid.vulneraciones.infraestructura.eventos;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

/**
 * Convertidor JSON de mensajería, compartido por el publicador ({@code RabbitTemplate}) y el
 * consumidor ({@code @RabbitListener}). Siempre disponible (no requiere broker) para que ambos lados
 * serialicen/deserialicen en JSON de forma coherente.
 *
 * <p>El mapeador de tipos usa precedencia <strong>inferida</strong>: el consumidor deserializa al
 * tipo del parámetro del método del listener, ignorando el encabezado {@code __TypeId__} que pudiera
 * fijar el productor (desacople entre servicios). Reutiliza el {@link ObjectMapper} de la aplicación
 * (con soporte de {@code java.time}) para parsear correctamente los instantes ISO-8601.</p>
 */
@Configuration
public class ConfiguracionMensajeria {

    @Bean
    public MessageConverter convertidorJsonEventos(ObjectMapper objectMapper) {
        Jackson2JsonMessageConverter convertidor = new Jackson2JsonMessageConverter(
                Objects.requireNonNull(objectMapper, "El ObjectMapper de eventos es obligatorio"));
        DefaultJackson2JavaTypeMapper mapeador = new DefaultJackson2JavaTypeMapper();
        mapeador.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
        mapeador.setTrustedPackages("*");
        convertidor.setJavaTypeMapper(mapeador);
        return convertidor;
    }
}
