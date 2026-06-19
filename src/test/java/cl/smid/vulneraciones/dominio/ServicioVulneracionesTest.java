package cl.smid.vulneraciones.dominio;

import cl.smid.vulneraciones.dominio.excepcion.AccesoDenegadoException;
import cl.smid.vulneraciones.dominio.excepcion.EventoMalformadoException;
import cl.smid.vulneraciones.dominio.excepcion.FichaNoMutableException;
import cl.smid.vulneraciones.dominio.excepcion.RecursoNoEncontradoException;
import cl.smid.vulneraciones.dominio.modelo.Alcance;
import cl.smid.vulneraciones.dominio.modelo.Complejidad;
import cl.smid.vulneraciones.dominio.modelo.ContextoUsuario;
import cl.smid.vulneraciones.dominio.modelo.EstadoFicha;
import cl.smid.vulneraciones.dominio.modelo.EventoCasoAbierto;
import cl.smid.vulneraciones.dominio.modelo.Ficha;
import cl.smid.vulneraciones.dominio.modelo.vista.ContenidoReservado;
import cl.smid.vulneraciones.dominio.modelo.vista.VistaFicha;
import cl.smid.vulneraciones.dominio.modelo.vista.VistaVulneracion;
import cl.smid.vulneraciones.dominio.puerto.entrada.comando.ComandoRegistrarVulneracion;
import cl.smid.vulneraciones.dominio.puerto.entrada.comando.ComandoTransicion;
import cl.smid.vulneraciones.dominio.puerto.entrada.comando.CriterioListadoFichas;
import cl.smid.vulneraciones.dominio.servicio.EvaluadorAlcance;
import cl.smid.vulneraciones.dominio.servicio.GeneradorNumeroFir;
import cl.smid.vulneraciones.dominio.servicio.MaquinaEstadosFicha;
import cl.smid.vulneraciones.dominio.servicio.ServicioVulneraciones;
import cl.smid.vulneraciones.infraestructura.reservado.ProtectorPassthrough;
import cl.smid.vulneraciones.soporte.ClientesNulos;
import cl.smid.vulneraciones.soporte.CorrelativoFirEnMemoria;
import cl.smid.vulneraciones.soporte.DirectorioSedesFijo;
import cl.smid.vulneraciones.soporte.GeneradorSecuencial;
import cl.smid.vulneraciones.soporte.PublicadorEventosCapturador;
import cl.smid.vulneraciones.soporte.RelojFijo;
import cl.smid.vulneraciones.soporte.RepositorioFichasEnMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ServicioVulneraciones: orquestación de la FIR (materialización, mutabilidad, alcance, reservado)")
class ServicioVulneracionesTest {

    private static final Instant T0 = Instant.parse("2027-05-01T12:00:00Z");
    private static final String SEDE = "sede-rm";
    private static final String UNIDAD = "uni-rm";

    private RepositorioFichasEnMemoria repositorio;
    private PublicadorEventosCapturador publicador;
    private ServicioVulneraciones servicio;

    @BeforeEach
    void preparar() {
        repositorio = new RepositorioFichasEnMemoria();
        publicador = new PublicadorEventosCapturador();
        servicio = new ServicioVulneraciones(
                repositorio,
                new CorrelativoFirEnMemoria(),
                new DirectorioSedesFijo("RM"),
                publicador,
                new ProtectorPassthrough(),
                ClientesNulos.casos(),
                ClientesNulos.personas(),
                ClientesNulos.catalogo(),
                new RelojFijo(T0),
                new GeneradorSecuencial(),
                new MaquinaEstadosFicha(),
                new GeneradorNumeroFir(LocalDate.of(2027, 1, 1), ZoneOffset.UTC),
                new EvaluadorAlcance(),
                Set.of("COORDINADOR", "ADMIN_NACIONAL"),
                Set.of("COORDINADOR", "ADMIN_NACIONAL"));
    }

    // ------------------------------------------------------------------ utilidades

    private EventoCasoAbierto evento(String idCaso, boolean requiere, String idSede) {
        return new EventoCasoAbierto(idCaso, T0, "EXP-1", idSede, UNIDAD, Complejidad.ALTA, requiere, null);
    }

    private ContextoUsuario ctx(Alcance alcance, String idSede, String idUnidad, String... roles) {
        return new ContextoUsuario("user-1", Set.of(roles), idSede, idUnidad, alcance, "Usuario");
    }

    private Ficha unicaFicha() {
        return servicio.listar(new CriterioListadoFichas(Alcance.NACIONAL, null, null, null, null, 0, 50))
                .contenido().get(0);
    }

    private long totalFichas() {
        return servicio.listar(new CriterioListadoFichas(Alcance.NACIONAL, null, null, null, null, 0, 50))
                .total();
    }

    // ------------------------------------------------------------------ materialización

    @Test
    @DisplayName("Materializa una FIR con número, estado inicial y evento ficha.abierta")
    void materializaUnaFicha() {
        servicio.materializar(evento("caso-1", true, SEDE));

        Ficha f = unicaFicha();
        assertThat(f.getNumeroFicha()).isEqualTo("FIR-RM-1/2027");
        assertThat(f.getIdCasoAlt()).isEqualTo("caso-1");
        assertThat(f.getEstado()).isEqualTo(EstadoFicha.EN_ELABORACION);
        assertThat(publicador.contarPorTipo("ficha.abierta")).isEqualTo(1);
    }

    @Test
    @DisplayName("La materialización es idempotente: reentrega del mismo caso ⇒ no-op")
    void materializacionIdempotente() {
        servicio.materializar(evento("caso-1", true, SEDE));
        servicio.materializar(evento("caso-1", true, SEDE));

        assertThat(totalFichas()).isEqualTo(1);
        assertThat(publicador.contarPorTipo("ficha.abierta")).isEqualTo(1);
    }

    @Test
    @DisplayName("Gating en falso (requiereFichaReservada=false) no crea ficha ni emite eventos")
    void gatingFalsoNoCreaFicha() {
        servicio.materializar(evento("caso-1", false, SEDE));

        assertThat(totalFichas()).isZero();
        assertThat(publicador.eventos()).isEmpty();
    }

    @Test
    @DisplayName("Evento sin datos mínimos (sin sede) ⇒ EventoMalformadoException (a DLQ)")
    void materializacionMalformada() {
        EventoCasoAbierto malo = evento("caso-x", true, null);
        assertThatThrownBy(() -> servicio.materializar(malo))
                .isInstanceOf(EventoMalformadoException.class);
    }

    // ------------------------------------------------------------------ alcance territorial

    @Test
    @DisplayName("Acceso fuera de alcance territorial ⇒ 404 (no revela existencia)")
    void territorialDevuelve404() {
        servicio.materializar(evento("caso-1", true, SEDE));
        String altKey = unicaFicha().getAltKey();

        ContextoUsuario otraSede = ctx(Alcance.SEDE, "sede-otra", "uni-otra", "ADMIN_NACIONAL");
        assertThatThrownBy(() -> servicio.obtenerDetalle(altKey, otraSede))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    // ------------------------------------------------------------------ rol y mutabilidad

    @Test
    @DisplayName("CERRAR sin rol de Coordinación ⇒ 403 (AccesoDenegado)")
    void cerrarSinCoordinacion403() {
        servicio.materializar(evento("caso-1", true, SEDE));
        String altKey = unicaFicha().getAltKey();

        ContextoUsuario profesional = ctx(Alcance.NACIONAL, SEDE, UNIDAD, "PROFESIONAL");
        ComandoTransicion cerrar = new ComandoTransicion(altKey, "CERRAR", "cierre");
        assertThatThrownBy(() -> servicio.transicionar(cerrar, profesional))
                .isInstanceOf(AccesoDenegadoException.class);
    }

    @Test
    @DisplayName("Tras CERRAR, registrar vulneración ⇒ 409 (ficha no mutable) y se emitió ficha.cerrada")
    void cierreBloqueaMutabilidad() {
        servicio.materializar(evento("caso-1", true, SEDE));
        String altKey = unicaFicha().getAltKey();
        ContextoUsuario coord = ctx(Alcance.NACIONAL, SEDE, UNIDAD, "ADMIN_NACIONAL");

        servicio.transicionar(new ComandoTransicion(altKey, "CERRAR", "cierre"), coord);

        ComandoRegistrarVulneracion cmd = new ComandoRegistrarVulneracion(
                altKey, "der-1", null, "nna-1", "GRAVE", "relato", null);
        assertThatThrownBy(() -> servicio.registrar(cmd, coord))
                .isInstanceOf(FichaNoMutableException.class);
        assertThat(publicador.contarPorTipo("ficha.cerrada")).isEqualTo(1);
    }

    // ------------------------------------------------------------------ contenido reservado

    @Test
    @DisplayName("El relato se redacta para quien no tiene rol reservado y se muestra para quien sí")
    void redaccionReservadaSegunRol() {
        servicio.materializar(evento("caso-1", true, SEDE));
        String altKey = unicaFicha().getAltKey();
        ContextoUsuario coord = ctx(Alcance.NACIONAL, SEDE, UNIDAD, "ADMIN_NACIONAL");

        ComandoRegistrarVulneracion cmd = new ComandoRegistrarVulneracion(
                altKey, "der-1", null, "nna-1", "GRAVE", "relato secreto", LocalDate.of(2027, 1, 2));
        VistaVulneracion registrada = servicio.registrar(cmd, coord);
        assertThat(registrada.relato()).isEqualTo("relato secreto");

        ContextoUsuario profesional = ctx(Alcance.NACIONAL, SEDE, UNIDAD, "PROFESIONAL");
        VistaFicha sinRol = servicio.obtenerDetalle(altKey, profesional);
        assertThat(sinRol.reservadoOculto()).isTrue();
        assertThat(sinRol.vulneraciones().get(0).relato()).isNull();

        VistaFicha conRol = servicio.obtenerDetalle(altKey, coord);
        assertThat(conRol.reservadoOculto()).isFalse();
        assertThat(conRol.vulneraciones().get(0).relato()).isEqualTo("relato secreto");
        assertThat(publicador.contarPorTipo("vulneracion.registrada")).isEqualTo(1);
    }

    @Test
    @DisplayName("Endpoint reservado: 403 sin rol; con rol entrega el contenido descifrado")
    void contenidoReservadoPorRol() {
        servicio.materializar(evento("caso-1", true, SEDE));
        String altKey = unicaFicha().getAltKey();
        ContextoUsuario coord = ctx(Alcance.NACIONAL, SEDE, UNIDAD, "ADMIN_NACIONAL");
        servicio.registrar(new ComandoRegistrarVulneracion(
                altKey, "der-1", null, "nna-1", "GRAVE", "relato secreto", null), coord);

        ContextoUsuario profesional = ctx(Alcance.NACIONAL, SEDE, UNIDAD, "PROFESIONAL");
        assertThatThrownBy(() -> servicio.obtener(altKey, profesional))
                .isInstanceOf(AccesoDenegadoException.class);

        ContenidoReservado contenido = servicio.obtener(altKey, coord);
        assertThat(contenido.relatos()).hasSize(1);
        assertThat(contenido.relatos().get(0).texto()).isEqualTo("relato secreto");
    }
}
