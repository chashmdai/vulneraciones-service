package cl.smid.vulneraciones.dominio;

import cl.smid.vulneraciones.dominio.modelo.AccionFicha;
import cl.smid.vulneraciones.dominio.modelo.EstadoFicha;
import cl.smid.vulneraciones.dominio.servicio.MaquinaEstadosFicha;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MaquinaEstadosFicha: transiciones permitidas del ciclo de vida de la FIR")
class MaquinaEstadosFichaTest {

    private final MaquinaEstadosFicha maquina = new MaquinaEstadosFicha();

    @Test
    @DisplayName("EN_ELABORACION + CERRAR -> CERRADA")
    void cerrarDesdeElaboracion() {
        assertThat(maquina.siguiente(EstadoFicha.EN_ELABORACION, AccionFicha.CERRAR))
                .contains(EstadoFicha.CERRADA);
        assertThat(maquina.permite(EstadoFicha.EN_ELABORACION, AccionFicha.CERRAR)).isTrue();
    }

    @Test
    @DisplayName("CERRADA + REABRIR -> EN_ELABORACION")
    void reabrirDesdeCerrada() {
        assertThat(maquina.siguiente(EstadoFicha.CERRADA, AccionFicha.REABRIR))
                .contains(EstadoFicha.EN_ELABORACION);
        assertThat(maquina.permite(EstadoFicha.CERRADA, AccionFicha.REABRIR)).isTrue();
    }

    @Test
    @DisplayName("Transiciones no contempladas devuelven vacío")
    void transicionesInvalidas() {
        assertThat(maquina.siguiente(EstadoFicha.EN_ELABORACION, AccionFicha.REABRIR)).isEmpty();
        assertThat(maquina.siguiente(EstadoFicha.CERRADA, AccionFicha.CERRAR)).isEmpty();
        assertThat(maquina.siguiente(EstadoFicha.EN_ELABORACION, AccionFicha.MATERIALIZACION)).isEmpty();
        assertThat(maquina.permite(EstadoFicha.CERRADA, AccionFicha.CERRAR)).isFalse();
    }
}
