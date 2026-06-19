package cl.smid.vulneraciones.dominio;

import cl.smid.vulneraciones.dominio.modelo.Alcance;
import cl.smid.vulneraciones.dominio.modelo.ContextoUsuario;
import cl.smid.vulneraciones.dominio.servicio.EvaluadorAlcance;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EvaluadorAlcance: control territorial registro a registro")
class EvaluadorAlcanceTest {

    private final EvaluadorAlcance evaluador = new EvaluadorAlcance();

    private ContextoUsuario ctx(Alcance alcance, String idSede, String idUnidad) {
        return new ContextoUsuario("u-1", Set.of("PROFESIONAL"), idSede, idUnidad, alcance, "Usuario");
    }

    @Test
    @DisplayName("Alcance NACIONAL accede a cualquier registro")
    void nacionalAccedeTodo() {
        assertThat(evaluador.puedeAcceder(ctx(Alcance.NACIONAL, "sede-A", "uni-A"), "sede-Z", "uni-Z")).isTrue();
    }

    @Test
    @DisplayName("Alcance SEDE solo accede a su misma sede")
    void sedeFiltraPorSede() {
        ContextoUsuario u = ctx(Alcance.SEDE, "sede-A", "uni-A");
        assertThat(evaluador.puedeAcceder(u, "sede-A", "uni-X")).isTrue();
        assertThat(evaluador.puedeAcceder(u, "sede-B", "uni-A")).isFalse();
    }

    @Test
    @DisplayName("Alcance UNIDAD solo accede a su misma unidad")
    void unidadFiltraPorUnidad() {
        ContextoUsuario u = ctx(Alcance.UNIDAD, "sede-A", "uni-A");
        assertThat(evaluador.puedeAcceder(u, "sede-A", "uni-A")).isTrue();
        assertThat(evaluador.puedeAcceder(u, "sede-A", "uni-B")).isFalse();
    }
}
