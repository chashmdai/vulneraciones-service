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
import cl.smid.vulneraciones.api.error.ErrorResponse;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Vulneraciones y FIR", description = "Ficha Interna Reservada, vulneraciones y antecedentes reservados.")
@SecurityRequirement(name = "bearerAuth")
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
    @Operation(summary = "Obtiene el detalle de una FIR",
            description = """
                    Retorna la Ficha Interna Reservada por identificador opaco dentro del alcance territorial.
                    La FIR no se crea por API: nace por el evento caso.abierto. El contenido reservado puede
                    venir redactado según el rol del usuario. Si no existe o está fuera de alcance territorial,
                    responde VUL-404.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ficha encontrada",
                    content = @Content(schema = @Schema(implementation = FichaDetalleResponse.class))),
            @ApiResponse(responseCode = "401", description = "AUTZ-003 - No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "AUTZ-004 - Sin rol suficiente",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "VUL-404 - No existe o fuera de alcance territorial",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "VUL-500 - Error interno",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<FichaDetalleResponse> detalle(@Parameter(description = "Identificador opaco de la FIR.",
                                                            example = "5f1e36b4-4a38-4c7c-bfb7-cc3ed16cc9b1")
                                                        @PathVariable String altKey) {
        ContextoUsuario ctx = proveedorContexto.contexto();
        VistaFicha vista = consultarFichas.obtenerDetalle(altKey, ctx);
        return ResponseEntity.ok(EnsambladorRespuestas.aDetalle(vista));
    }

    /** Listado paginado de fichas dentro del alcance del solicitante. */
    @GetMapping
    @Transactional(readOnly = true)
    @Operation(summary = "Lista FIR paginadas",
            description = """
                    Lista fichas dentro del alcance territorial del usuario autenticado.
                    Permite filtrar por caso de origen y estado. Estados: EN_ELABORACION y CERRADA.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de fichas",
                    content = @Content(schema = @Schema(implementation = PaginaResponse.class))),
            @ApiResponse(responseCode = "400", description = "VUL-001 - Solicitud inválida",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "AUTZ-003 - No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "VUL-500 - Error interno",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PaginaResponse<FichaResumenResponse>> listar(
            @Parameter(description = "Identificador opaco del caso asociado.",
                    example = "7d6b3c3c-9d22-4b10-8d3d-1f3900c13f20")
            @RequestParam(name = "idCaso", required = false) String idCaso,
            @Parameter(description = "Estado de la FIR.", example = "EN_ELABORACION",
                    schema = @Schema(allowableValues = {"EN_ELABORACION", "CERRADA"}))
            @RequestParam(name = "estado", required = false) String estado,
            @Parameter(description = "Número de página, base cero.", example = "0")
            @RequestParam(name = "pagina", defaultValue = "0") int pagina,
            @Parameter(description = "Tamaño de página.", example = "20")
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
    @Operation(summary = "Obtiene contenido reservado de una FIR",
            description = """
                    Retorna relatos y descripciones reservadas en claro solo para roles habilitados.
                    En el detalle general el contenido puede venir redactado; este endpoint requiere rol de
                    acceso reservado. No usar ejemplos con nombres, RUT ni relatos reales.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contenido reservado",
                    content = @Content(schema = @Schema(implementation = ContenidoReservadoResponse.class))),
            @ApiResponse(responseCode = "401", description = "AUTZ-003 - No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "AUTZ-004 - Sin rol de acceso reservado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "VUL-404 - No existe o fuera de alcance territorial",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "VUL-500 - Error interno",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ContenidoReservadoResponse> reservado(@Parameter(description = "Identificador opaco de la FIR.",
                                                                    example = "5f1e36b4-4a38-4c7c-bfb7-cc3ed16cc9b1")
                                                                @PathVariable String altKey) {
        ContextoUsuario ctx = proveedorContexto.contexto();
        ContenidoReservado contenido = consultarContenidoReservado.obtener(altKey, ctx);
        return ResponseEntity.ok(EnsambladorRespuestas.aReservado(contenido));
    }

    /** Registro de una vulneración (territorial + ventana de mutabilidad). */
    @PostMapping("/{altKey}/vulneraciones")
    @Transactional
    @Operation(summary = "Registra una vulneración en la FIR",
            description = """
                    Registra una vulneración durante la ventana mutable de la FIR. Solo se admiten fichas en
                    EN_ELABORACION; fuera de esa ventana responde VUL-409. Los textos son reservados y se cifran
                    en reposo según configuración.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Vulneración registrada",
                    content = @Content(schema = @Schema(implementation = VulneracionResponse.class))),
            @ApiResponse(responseCode = "400", description = "VUL-001 - Solicitud inválida",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "AUTZ-003 - No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "AUTZ-004 - Sin rol suficiente",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "VUL-404 - No existe o fuera de alcance territorial",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "VUL-409 - Ficha no mutable o conflicto",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "VUL-422 - Regla de negocio incumplida",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "VUL-500 - Error interno",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<VulneracionResponse> registrarVulneracion(
            @Parameter(description = "Identificador opaco de la FIR.",
                    example = "5f1e36b4-4a38-4c7c-bfb7-cc3ed16cc9b1")
            @PathVariable String altKey,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
                    content = @Content(schema = @Schema(implementation = RegistrarVulneracionRequest.class),
                            examples = @ExampleObject("""
                                    {
                                      "idDerechoAlt": "2e7f7e7d-2f0d-41a5-bc9c-98d0a4bd32b5",
                                      "idCausaAlt": "6a0df156-9898-4e8e-9cf4-214f1b138a4a",
                                      "idNnaAlt": "d8d4c0c9-ec8e-49f2-b1c3-0d0f1f6b66a1",
                                      "gravedad": "GRAVE",
                                      "relato": "Texto reservado sintético para pruebas.",
                                      "fechaHecho": "2027-04-20"
                                    }
                                    """)))
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
    @Operation(summary = "Registra un antecedente reservado",
            description = """
                    Registra un antecedente en la FIR durante la ventana mutable. La descripción es reservada y
                    puede mostrarse redactada en respuestas generales según el rol.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Antecedente registrado",
                    content = @Content(schema = @Schema(implementation = AntecedenteResponse.class))),
            @ApiResponse(responseCode = "400", description = "VUL-001 - Solicitud inválida",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "AUTZ-003 - No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "AUTZ-004 - Sin rol suficiente",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "VUL-404 - No existe o fuera de alcance territorial",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "VUL-409 - Ficha no mutable o conflicto",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "VUL-422 - Regla de negocio incumplida",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "VUL-500 - Error interno",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AntecedenteResponse> registrarAntecedente(
            @Parameter(description = "Identificador opaco de la FIR.",
                    example = "5f1e36b4-4a38-4c7c-bfb7-cc3ed16cc9b1")
            @PathVariable String altKey,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
                    content = @Content(schema = @Schema(implementation = RegistrarAntecedenteRequest.class),
                            examples = @ExampleObject("""
                                    {
                                      "tipo": "ESCOLAR",
                                      "descripcion": "Descripción reservada sintética para pruebas.",
                                      "fecha": "2027-03-15",
                                      "fuente": "Fuente institucional sintética"
                                    }
                                    """)))
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
    @Operation(summary = "Transiciona una FIR",
            description = """
                    Aplica transiciones administrativas CERRAR o REABRIR. Ambas requieren rol de Coordinación.
                    MATERIALIZACION es una pseudo-acción de sistema y no es invocable por API.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ficha transicionada",
                    content = @Content(schema = @Schema(implementation = FichaDetalleResponse.class))),
            @ApiResponse(responseCode = "400", description = "VUL-001 - Solicitud inválida",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "AUTZ-003 - No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "AUTZ-004 - Sin rol de Coordinación",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "VUL-404 - No existe o fuera de alcance territorial",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "VUL-409 - Transición inválida",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "VUL-422 - Regla de negocio incumplida",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "VUL-500 - Error interno",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<FichaDetalleResponse> transicionar(
            @Parameter(description = "Identificador opaco de la FIR.",
                    example = "5f1e36b4-4a38-4c7c-bfb7-cc3ed16cc9b1")
            @PathVariable String altKey,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
                    content = @Content(schema = @Schema(implementation = TransicionRequest.class),
                            examples = @ExampleObject("""
                                    {
                                      "accion": "CERRAR",
                                      "observacion": "Cierre administrativo sintético."
                                    }
                                    """)))
            @Valid @RequestBody TransicionRequest request) {
        ContextoUsuario ctx = proveedorContexto.contexto();
        ComandoTransicion comando = new ComandoTransicion(altKey, request.accion(), request.observacion());
        VistaFicha vista = transicionarFicha.transicionar(comando, ctx);
        return ResponseEntity.ok(EnsambladorRespuestas.aDetalle(vista));
    }
}
