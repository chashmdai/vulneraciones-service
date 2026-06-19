package cl.smid.vulneraciones.dominio;

import cl.smid.vulneraciones.dominio.modelo.NumeroFir;
import cl.smid.vulneraciones.dominio.modelo.SerieCorrelativo;
import cl.smid.vulneraciones.dominio.servicio.GeneradorNumeroFir;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GeneradorNumeroFir: resolución de serie, año y formato del número")
class GeneradorNumeroFirTest {

    private final GeneradorNumeroFir generador =
            new GeneradorNumeroFir(LocalDate.of(2027, 1, 1), ZoneOffset.UTC);

    private static final Instant ANTES = Instant.parse("2026-06-15T10:00:00Z");
    private static final Instant DESPUES = Instant.parse("2027-03-01T10:00:00Z");

    @Test
    @DisplayName("Sin bandera del evento, la fecha de corte decide la serie")
    void seriePorFecha() {
        assertThat(generador.resolverSerie(null, ANTES)).isEqualTo(SerieCorrelativo.BETA);
        assertThat(generador.resolverSerie(null, DESPUES)).isEqualTo(SerieCorrelativo.OFICIAL);
    }

    @Test
    @DisplayName("La bandera del evento tiene prioridad sobre la fecha")
    void banderaTienePrioridad() {
        assertThat(generador.resolverSerie(Boolean.TRUE, DESPUES)).isEqualTo(SerieCorrelativo.BETA);
        assertThat(generador.resolverSerie(Boolean.FALSE, ANTES)).isEqualTo(SerieCorrelativo.OFICIAL);
    }

    @Test
    @DisplayName("El año se calcula en UTC")
    void anioEnUtc() {
        assertThat(generador.anioDe(ANTES)).isEqualTo(2026);
        assertThat(generador.anioDe(DESPUES)).isEqualTo(2027);
    }

    @Test
    @DisplayName("El número se formatea según la serie")
    void formatoNumero() {
        NumeroFir oficial = generador.construir("RM", SerieCorrelativo.OFICIAL, 1, 2027);
        NumeroFir beta = generador.construir("RM", SerieCorrelativo.BETA, 1, 2027);
        assertThat(oficial.formatear()).isEqualTo("FIR-RM-1/2027");
        assertThat(beta.formatear()).isEqualTo("FIR-RM-B1/2027");
    }
}
