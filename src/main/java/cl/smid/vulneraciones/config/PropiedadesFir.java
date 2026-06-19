package cl.smid.vulneraciones.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.LocalDate;

/**
 * Política de numeración de la FIR ({@code smid.fir.*}).
 *
 * @param inicioOficial fecha de corte a partir de la cual la serie es OFICIAL (apertura anterior ⇒
 *                      serie BETA). Si el evento informa {@code esBeta}, esa bandera tiene prioridad.
 */
@ConfigurationProperties(prefix = "smid.fir")
public record PropiedadesFir(LocalDate inicioOficial) {
}
