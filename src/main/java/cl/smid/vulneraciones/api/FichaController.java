package cl.smid.vulneraciones.api;

import cl.smid.vulneraciones.api.dto.ContenidoReservadoResponse;
import cl.smid.vulneraciones.api.dto.FichaDetalleResponse;
import cl.smid.vulneraciones.api.dto.FichaResumenResponse;
import cl.smid.vulneraciones.api.dto.PaginaResponse;
import cl.smid.vulneraciones.api.dto.RegistrarAntecedenteRequest;
import cl.smid.vulneraciones.api.dto.RegistrarVulneracionRequest;
import cl.smid.vulneraciones.api.dto.TransicionRequest;
import cl.smid.vulneraciones.api.dto.VulneracionResponse;
import cl.smid.vulneraciones.api.dto.AntecedenteResponse;
import cl.smid.vulneraciones.api.mapper.EnsambladorRespuestas;
import cl.smid.vulneraciones.dominio.modelo.ContextoUsuario;
import cl.smid.vulneraciones.dominio.modelo.EstadoFicha;
import cl.smid.vulneraciones.dominio.modelo.vista.ContenidoReservado;
import cl.smid.vulneraciones.dominio.modelo.vista.VistaAntecedente;
import cl.smid.vulneraciones.dominio.modelo.vista.VistaFicha;
import cl.smid.vulneraciones.dominio.modelo.vista.VistaVulneracion;
import cl.smid.vulneraciones.dominio.puerto.entrada.ConsultarContenidoReservado;
import cl.smid.vulneraciones.dominio.puerto.entrada.ConsultarFichas;
import cl.smid.vulneraciones.dominio.puerto.entrada.RegistrarAntecedente;
import cl.smid.vulneraciones.dominio.puerto.entrada.RegistrarVulneracion;
import cl.smid.vulneraciones.dominio.puerto.entrada.TransicionarFicha;
import cl.smid.vulneraciones.dominio.puerto.entrada.comando.ComandoRegistrarAntecedente;
import cl.smid.vulneraciones.dominio.puerto.entrada.comando.ComandoRegistrarVulneracion;
import cl.smid.vulneraciones.dominio.puerto.entrada.comando.ComandoTransicion;
import cl.smid.vulneraciones.dominio.puerto.entrada.comando.CriterioListadoFichas;
import cl.smid.vulneraciones.infraestructura.seguridad.ProveedorContexto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controladores REST del servicio de Vulneraciones (FIR).
 *
 * <p>Las rutas cuelgan de {@code /vulneraciones/fichas} (tras el {@code StripPrefix=1} del gateway).
 * El controlador es la <strong>frontera transaccional</strong> de las operaciones síncronas
 * ({@code @Transactional}); el contexto de sesión proviene del {@link ProveedorContexto} (poblado por
 * el filtro JWT). La FIR no se crea por la API: nace de forma asíncrona desde el evento
 * {@code caso.abierto}.</p>
 */
@RestController
@RequestMapping("/vulneraciones/fichas")
public class FichaController {

    private final ConsultarFichas consultarFichas;
    private final RegistrarVulneracion registrarVulneracion;
    private final RegistrarAntecedente registrarAntecedente;
    private final TransicionarFicha transicionarFicha;
    private final ConsultarContenidoReservado consultarContenidoReservado;
    private final ProveedorContexto proveedorContexto;

    public FichaController(ConsultarFichas consultarFichas, RegistrarVulneracion registrarVulneracion,
                           RegistrarAntecedente registrarAntecedente, TransicionarFicha transicionarFicha,
                           ConsultarContenidoReservado consultarContenidoReservado,
                           ProveedorContexto proveedorContexto) {
        this.consultarFichas = consultarFichas;
        this.registrarVulneracion = registrarVulneracion;
        this.registrarAntecedente = registrarAntecedente;
        this.transicionarFicha = transicionarFicha;
        this.consultarContenidoReservado = consultarContenidoReservado;
        this.proveedorContexto = proveedorContexto;
    }

    /** Detalle de una ficha (territorial; contenido reservado redactado según rol). */
    @GetMapping("/{altKey}")
    @Transactional(readOnly = true)
    public ResponseEntity<FichaDetalleResponse> detalle(@PathVariable String altKey) {
        ContextoUsuario ctx = proveedorContexto.contexto();
        VistaFicha vista = consultarFichas.obtenerDetalle(altKey, ctx);
        return ResponseEntity.ok(EnsambladorRespuestas.aDetalle(vista));
    }

    /** Listado paginado de fichas dentro del alcance del solicitante. */
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<PaginaResponse<FichaResumenResponse>> listar(
            @RequestParam(name = "idCaso", required = false) String idCaso,
            @RequestParam(name = "estado", required = false) String estado,
            @RequestParam(name = "pagina", defaultValue = "0") int pagina,
            @RequestParam(name = "tamano", defaultValue = "20") int tamano) {
        ContextoUsuario ctx = proveedorContexto.contexto();
        EstadoFicha estadoFiltro = EstadoFicha.desde(estado);
        CriterioListadoFichas criterio = new CriterioListadoFichas(
                ctx.alcance(), ctx.idSede(), ctx.idUnidad(), idCaso, estadoFiltro, pagina, tamano);
        return ResponseEntity.ok(EnsambladorRespuestas.aPagina(consultarFichas.listar(criterio)));
    }

    /** Contenido reservado en claro (solo rol de acceso reservado; 403 en caso contrario). */
    @GetMapping("/{altKey}/reservado")
    @Transactional(readOnly = true)
    public ResponseEntity<ContenidoReservadoResponse> reservado(@PathVariable String altKey) {
        ContextoUsuario ctx = proveedorContexto.contexto();
        ContenidoReservado contenido = consultarContenidoReservado.obtener(altKey, ctx);
        return ResponseEntity.ok(EnsambladorRespuestas.aReservado(contenido));
    }

    /** Registro de una vulneración (territorial + ventana de mutabilidad). */
    @PostMapping("/{altKey}/vulneraciones")
    @Transactional
    public ResponseEntity<VulneracionResponse> registrarVulneracion(
            @PathVariable String altKey,
            @Valid @RequestBody RegistrarVulneracionRequest request) {
        ContextoUsuario ctx = proveedorContexto.contexto();
        ComandoRegistrarVulneracion comando = new ComandoRegistrarVulneracion(
                altKey, request.idDerechoAlt(), request.idCausaAlt(), request.idNnaAlt(),
                request.gravedad(), request.relato(), request.fechaHecho());
        VistaVulneracion vista = registrarVulneracion.registrar(comando, ctx);
        return ResponseEntity.status(HttpStatus.CREATED).body(EnsambladorRespuestas.aVulneracion(vista));
    }

    /** Registro de un antecedente (territorial + ventana de mutabilidad). */
    @PostMapping("/{altKey}/antecedentes")
    @Transactional
    public ResponseEntity<AntecedenteResponse> registrarAntecedente(
            @PathVariable String altKey,
            @Valid @RequestBody RegistrarAntecedenteRequest request) {
        ContextoUsuario ctx = proveedorContexto.contexto();
        ComandoRegistrarAntecedente comando = new ComandoRegistrarAntecedente(
                altKey, request.tipo(), request.descripcion(), request.fecha(), request.fuente());
        VistaAntecedente vista = registrarAntecedente.registrar(comando, ctx);
        return ResponseEntity.status(HttpStatus.CREATED).body(EnsambladorRespuestas.aAntecedente(vista));
    }

    /** Transición administrativa de la ficha (CERRAR/REABRIR; exige rol de Coordinación). */
    @PostMapping("/{altKey}/transiciones")
    @Transactional
    public ResponseEntity<FichaDetalleResponse> transicionar(
            @PathVariable String altKey,
            @Valid @RequestBody TransicionRequest request) {
        ContextoUsuario ctx = proveedorContexto.contexto();
        ComandoTransicion comando = new ComandoTransicion(altKey, request.accion(), request.observacion());
        VistaFicha vista = transicionarFicha.transicionar(comando, ctx);
        return ResponseEntity.ok(EnsambladorRespuestas.aDetalle(vista));
    }
}
