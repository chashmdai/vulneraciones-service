package cl.smid.vulneraciones.dominio.servicio;

import cl.smid.vulneraciones.dominio.excepcion.AccesoDenegadoException;
import cl.smid.vulneraciones.dominio.excepcion.EventoMalformadoException;
import cl.smid.vulneraciones.dominio.excepcion.RecursoNoEncontradoException;
import cl.smid.vulneraciones.dominio.modelo.Antecedente;
import cl.smid.vulneraciones.dominio.modelo.ContextoUsuario;
import cl.smid.vulneraciones.dominio.modelo.EventoCasoAbierto;
import cl.smid.vulneraciones.dominio.modelo.Ficha;
import cl.smid.vulneraciones.dominio.modelo.Gravedad;
import cl.smid.vulneraciones.dominio.modelo.NumeroFir;
import cl.smid.vulneraciones.dominio.modelo.ResultadoPagina;
import cl.smid.vulneraciones.dominio.modelo.SerieCorrelativo;
import cl.smid.vulneraciones.dominio.modelo.TipoAntecedente;
import cl.smid.vulneraciones.dominio.modelo.Transicion;
import cl.smid.vulneraciones.dominio.modelo.Vulneracion;
import cl.smid.vulneraciones.dominio.modelo.vista.ContenidoReservado;
import cl.smid.vulneraciones.dominio.modelo.vista.EnriquecimientoFicha;
import cl.smid.vulneraciones.dominio.modelo.vista.ItemReservado;
import cl.smid.vulneraciones.dominio.modelo.vista.VistaAntecedente;
import cl.smid.vulneraciones.dominio.modelo.vista.VistaFicha;
import cl.smid.vulneraciones.dominio.modelo.vista.VistaVulneracion;
import cl.smid.vulneraciones.dominio.puerto.entrada.ConsultarContenidoReservado;
import cl.smid.vulneraciones.dominio.puerto.entrada.ConsultarFichas;
import cl.smid.vulneraciones.dominio.puerto.entrada.MaterializarFicha;
import cl.smid.vulneraciones.dominio.puerto.entrada.RegistrarAntecedente;
import cl.smid.vulneraciones.dominio.puerto.entrada.RegistrarVulneracion;
import cl.smid.vulneraciones.dominio.puerto.entrada.TransicionarFicha;
import cl.smid.vulneraciones.dominio.puerto.entrada.comando.ComandoRegistrarAntecedente;
import cl.smid.vulneraciones.dominio.puerto.entrada.comando.ComandoRegistrarVulneracion;
import cl.smid.vulneraciones.dominio.puerto.entrada.comando.ComandoTransicion;
import cl.smid.vulneraciones.dominio.puerto.entrada.comando.CriterioListadoFichas;
import cl.smid.vulneraciones.dominio.modelo.AccionFicha;
import cl.smid.vulneraciones.dominio.puerto.salida.ClienteCasos;
import cl.smid.vulneraciones.dominio.puerto.salida.ClienteCatalogo;
import cl.smid.vulneraciones.dominio.puerto.salida.ClientePersonas;
import cl.smid.vulneraciones.dominio.puerto.salida.CorrelativoFirPort;
import cl.smid.vulneraciones.dominio.puerto.salida.DirectorioSedes;
import cl.smid.vulneraciones.dominio.puerto.salida.EventoDominio;
import cl.smid.vulneraciones.dominio.puerto.salida.GeneradorIdentificadores;
import cl.smid.vulneraciones.dominio.puerto.salida.ProtectorDatosReservados;
import cl.smid.vulneraciones.dominio.puerto.salida.PublicadorEventos;
import cl.smid.vulneraciones.dominio.puerto.salida.Reloj;
import cl.smid.vulneraciones.dominio.puerto.salida.RepositorioFichas;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Servicio de aplicación del dominio de Vulneraciones (6.5). Orquesta los seis casos de uso
 * implementando sus puertos de entrada y apoyándose en los servicios puros (máquina de estados,
 * generador de número, evaluador de alcance) y en los puertos de salida.
 *
 * <p>POJO puro: no conoce Spring ni JPA. Su instancia y cableado se realizan en
 * {@code config.CableadoDominio}. La demarcación transaccional vive en la frontera (controlador y
 * listener), nunca aquí.</p>
 *
 * <p>Reglas transversales aplicadas: acceso territorial registro a registro (denegación ⇒ 404),
 * control de rol para acciones administrativas y para contenido reservado (⇒ 403), cifrado en reposo
 * de datos reservados a través del puerto correspondiente, eventos metadata-only (G7) y publicación
 * tolerante a fallos.</p>
 */
public class ServicioVulneraciones implements MaterializarFicha, ConsultarFichas, RegistrarVulneracion,
        RegistrarAntecedente, TransicionarFicha, ConsultarContenidoReservado {

    private final RepositorioFichas repositorio;
    private final CorrelativoFirPort correlativo;
    private final DirectorioSedes directorioSedes;
    private final PublicadorEventos publicador;
    private final ProtectorDatosReservados protector;
    private final ClienteCasos clienteCasos;
    private final ClientePersonas clientePersonas;
    private final ClienteCatalogo clienteCatalogo;
    private final Reloj reloj;
    private final GeneradorIdentificadores generador;
    private final MaquinaEstadosFicha maquina;
    private final GeneradorNumeroFir generadorNumero;
    private final EvaluadorAlcance evaluador;
    private final Set<String> rolesCoordinacion;
    private final Set<String> rolesAccesoReservado;

    /**
     * Constructor de composición. Recibe todas las dependencias ya resueltas (puertos, servicios
     * puros y las listas de roles de configuración).
     */
    public ServicioVulneraciones(RepositorioFichas repositorio, CorrelativoFirPort correlativo,
                                 DirectorioSedes directorioSedes, PublicadorEventos publicador,
                                 ProtectorDatosReservados protector, ClienteCasos clienteCasos,
                                 ClientePersonas clientePersonas, ClienteCatalogo clienteCatalogo,
                                 Reloj reloj, GeneradorIdentificadores generador, MaquinaEstadosFicha maquina,
                                 GeneradorNumeroFir generadorNumero, EvaluadorAlcance evaluador,
                                 Set<String> rolesCoordinacion, Set<String> rolesAccesoReservado) {
        this.repositorio = repositorio;
        this.correlativo = correlativo;
        this.directorioSedes = directorioSedes;
        this.publicador = publicador;
        this.protector = protector;
        this.clienteCasos = clienteCasos;
        this.clientePersonas = clientePersonas;
        this.clienteCatalogo = clienteCatalogo;
        this.reloj = reloj;
        this.generador = generador;
        this.maquina = maquina;
        this.generadorNumero = generadorNumero;
        this.evaluador = evaluador;
        this.rolesCoordinacion = Set.copyOf(rolesCoordinacion);
        this.rolesAccesoReservado = Set.copyOf(rolesAccesoReservado);
    }

    // ===================================================================================
    // Caso de uso: materialización asíncrona de la FIR desde el evento caso.abierto
    // ===================================================================================

    @Override
    public void materializar(EventoCasoAbierto evento) {
        // 1) Gating: la FIR se crea solo si el caso requiere ficha reservada.
        if (!evento.requiereFichaReservada()) {
            return; // no-op con ack limpio: no se crea ficha.
        }
        // 2) Idempotencia (entrega at-least-once): pre-chequeo por caso.
        if (repositorio.existePorCaso(evento.idCasoAlt())) {
            return; // reentrega secuencial ⇒ no-op.
        }
        // 3) Validación de datos mínimos para materializar (si faltan ⇒ DLQ).
        validarDatosMaterializacion(evento);

        // 4) Resolución de serie, año, código de sede y correlativo atómico.
        Instant ahora = reloj.ahora();
        SerieCorrelativo serie = generadorNumero.resolverSerie(evento.esBeta(), evento.ocurridoEn());
        int anio = generadorNumero.anioDe(evento.ocurridoEn());
        String codigoSede = directorioSedes.codigoDe(evento.idSedeAlt());
        long secuencia = correlativo.reservar(evento.idSedeAlt(), anio, serie);
        NumeroFir numero = generadorNumero.construir(codigoSede, serie, secuencia, anio);

        // 5) Construcción del esqueleto con su asiento de apertura y persistencia atómica.
        Ficha ficha = Ficha.materializar(
                generador.nuevoUuid(),
                numero,
                evento.idCasoAlt(),
                evento.numeroExpediente(),
                evento.idSedeAlt(),
                evento.idUnidadAlt(),
                evento.complejidad(),
                ahora,
                generador.nuevoUuid());
        // Una carrera entre instancias se traduce en FichaDuplicadaException (propaga ⇒ reintento ⇒ no-op).
        Ficha persistida = repositorio.materializar(ficha);

        // 6) Evento de dominio (metadata-only, G7). Publicación tolerante a fallos.
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("numeroFicha", persistida.getNumeroFicha());
        meta.put("idCasoAlt", persistida.getIdCasoAlt());
        meta.put("idSede", persistida.getIdSedeAlt());
        meta.put("idUnidad", persistida.getIdUnidadAlt());
        meta.put("complejidad", persistida.getComplejidad() == null ? null : persistida.getComplejidad().name());
        meta.put("esBeta", persistida.isEsBeta());
        publicador.publicar(EventoDominio.de("ficha.abierta", persistida.getAltKey(), ahora, meta));
    }

    private void validarDatosMaterializacion(EventoCasoAbierto evento) {
        List<String> faltantes = new ArrayList<>();
        if (esVacio(evento.idCasoAlt())) {
            faltantes.add("altKey (idCasoAlt)");
        }
        if (esVacio(evento.idSedeAlt())) {
            faltantes.add("metadatos.idSede");
        }
        if (esVacio(evento.numeroExpediente())) {
            faltantes.add("metadatos.numeroExpediente");
        }
        if (evento.ocurridoEn() == null) {
            faltantes.add("ocurridoEn");
        }
        if (!faltantes.isEmpty()) {
            throw new EventoMalformadoException(
                    "Evento caso.abierto sin datos mínimos para materializar la FIR: " + String.join(", ", faltantes) + ".");
        }
    }

    // ===================================================================================
    // Caso de uso: consulta de fichas (detalle y listado)
    // ===================================================================================

    @Override
    public VistaFicha obtenerDetalle(String altKey, ContextoUsuario ctx) {
        Ficha ficha = cargarConAlcance(altKey, ctx);
        boolean autorizadoReservado = ctx.tieneAlgunRol(rolesAccesoReservado);
        return construirVistaFicha(ficha, autorizadoReservado, true);
    }

    @Override
    public ResultadoPagina<Ficha> listar(CriterioListadoFichas criterio) {
        return repositorio.listar(criterio);
    }

    // ===================================================================================
    // Caso de uso: registro de una vulneración
    // ===================================================================================

    @Override
    public VistaVulneracion registrar(ComandoRegistrarVulneracion comando, ContextoUsuario ctx) {
        Ficha ficha = cargarConAlcance(comando.fichaAltKey(), ctx);
        ficha.asegurarMutable(); // ventana de mutabilidad ⇒ VUL-409 si no procede

        Gravedad gravedad = Gravedad.exigir(comando.gravedad());
        String relatoCifrado = protector.cifrar(comando.relato());
        String nombreLegible = clientePersonas.nombreLegible(comando.idNnaAlt()).orElse(null);
        Instant ahora = reloj.ahora();

        Vulneracion vulneracion = Vulneracion.crear(
                generador.nuevoUuid(),
                comando.idDerechoAlt(),
                comando.idCausaAlt(),
                comando.idNnaAlt(),
                nombreLegible,
                gravedad,
                relatoCifrado,
                comando.fechaHecho(),
                ctx.sub(),
                ahora);

        ficha.registrarVulneracion(vulneracion);
        Vulneracion persistida = repositorio.agregarVulneracion(ficha, vulneracion);

        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("idFichaAlt", ficha.getAltKey());
        meta.put("idDerechoAlt", persistida.getIdDerechoAlt());
        meta.put("gravedad", persistida.getGravedad().name());
        publicador.publicar(EventoDominio.de("vulneracion.registrada", persistida.getAltKey(), ahora, meta));

        return construirVistaVulneracion(persistida, ctx.tieneAlgunRol(rolesAccesoReservado), true);
    }

    // ===================================================================================
    // Caso de uso: registro de un antecedente
    // ===================================================================================

    @Override
    public VistaAntecedente registrar(ComandoRegistrarAntecedente comando, ContextoUsuario ctx) {
        Ficha ficha = cargarConAlcance(comando.fichaAltKey(), ctx);
        ficha.asegurarMutable(); // ventana de mutabilidad ⇒ VUL-409 si no procede

        TipoAntecedente tipo = TipoAntecedente.exigir(comando.tipo());
        String descripcionCifrada = protector.cifrar(comando.descripcion());
        Instant ahora = reloj.ahora();

        Antecedente antecedente = Antecedente.crear(
                generador.nuevoUuid(),
                tipo,
                descripcionCifrada,
                comando.fecha(),
                comando.fuente(),
                ctx.sub(),
                ahora);

        ficha.registrarAntecedente(antecedente);
        Antecedente persistido = repositorio.agregarAntecedente(ficha, antecedente);

        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("idFichaAlt", ficha.getAltKey());
        meta.put("tipo", persistido.getTipo().name());
        publicador.publicar(EventoDominio.de("antecedente.registrado", persistido.getAltKey(), ahora, meta));

        return construirVistaAntecedente(persistido, ctx.tieneAlgunRol(rolesAccesoReservado));
    }

    // ===================================================================================
    // Caso de uso: transición administrativa (CERRAR/REABRIR)
    // ===================================================================================

    @Override
    public VistaFicha transicionar(ComandoTransicion comando, ContextoUsuario ctx) {
        Ficha ficha = cargarConAlcance(comando.fichaAltKey(), ctx);
        AccionFicha accion = AccionFicha.desdeUsuario(comando.accion());

        // Control de rol para acciones administrativas (Coordinación) ⇒ AUTZ-004 / 403.
        if (accion.esAdministrativa() && !ctx.tieneAlgunRol(rolesCoordinacion)) {
            throw new AccesoDenegadoException(
                    "La acción " + accion + " sobre la ficha requiere rol de Coordinación.");
        }

        Instant ahora = reloj.ahora();
        Transicion asiento = ficha.transicionar(accion, maquina, comando.observacion(), ctx.sub(),
                ahora, generador.nuevoUuid());
        repositorio.aplicarTransicion(ficha, asiento);

        if (accion == AccionFicha.CERRAR) {
            Map<String, Object> meta = new LinkedHashMap<>();
            meta.put("idFichaAlt", ficha.getAltKey());
            meta.put("numeroFicha", ficha.getNumeroFicha());
            publicador.publicar(EventoDominio.de("ficha.cerrada", ficha.getAltKey(), ahora, meta));
        }

        boolean autorizadoReservado = ctx.tieneAlgunRol(rolesAccesoReservado);
        return construirVistaFicha(ficha, autorizadoReservado, false);
    }

    // ===================================================================================
    // Caso de uso: contenido reservado dedicado (403 si no habilita)
    // ===================================================================================

    @Override
    public ContenidoReservado obtener(String altKey, ContextoUsuario ctx) {
        Ficha ficha = cargarConAlcance(altKey, ctx);
        if (!ctx.tieneAlgunRol(rolesAccesoReservado)) {
            throw new AccesoDenegadoException(
                    "El acceso al contenido reservado de la ficha requiere un rol habilitante.");
        }
        List<ItemReservado> relatos = new ArrayList<>();
        for (Vulneracion v : ficha.getVulneraciones()) {
            relatos.add(new ItemReservado(v.getAltKey(), protector.descifrar(v.getRelatoCifrado())));
        }
        List<ItemReservado> descripciones = new ArrayList<>();
        for (Antecedente a : ficha.getAntecedentes()) {
            descripciones.add(new ItemReservado(a.getAltKey(), protector.descifrar(a.getDescripcionCifrada())));
        }
        return new ContenidoReservado(ficha.getAltKey(), relatos, descripciones);
    }

    // ===================================================================================
    // Apoyo: carga con control territorial y construcción de vistas
    // ===================================================================================

    /**
     * Carga la ficha por {@code alt_key} y aplica el control territorial. La inexistencia y la
     * denegación territorial se funden en VUL-404 (no se revela la existencia de la ficha).
     */
    private Ficha cargarConAlcance(String altKey, ContextoUsuario ctx) {
        Ficha ficha = repositorio.buscarPorAltKey(altKey)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe una ficha accesible con identificador " + altKey + "."));
        if (!evaluador.puedeAcceder(ctx, ficha.getIdSedeAlt(), ficha.getIdUnidadAlt())) {
            throw new RecursoNoEncontradoException(
                    "No existe una ficha accesible con identificador " + altKey + ".");
        }
        return ficha;
    }

    /**
     * Construye la vista de detalle de la ficha aplicando la política de redacción del contenido
     * reservado y, opcionalmente, el enriquecimiento on-demand contra Casos.
     *
     * @param ficha               agregado cargado
     * @param autorizadoReservado si el solicitante puede ver el contenido reservado en claro
     * @param conEnriquecimiento  si se intenta el cruce best-effort con Casos
     */
    private VistaFicha construirVistaFicha(Ficha ficha, boolean autorizadoReservado, boolean conEnriquecimiento) {
        List<VistaVulneracion> vistasVuln = new ArrayList<>();
        for (Vulneracion v : ficha.getVulneraciones()) {
            vistasVuln.add(construirVistaVulneracion(v, autorizadoReservado, conEnriquecimiento));
        }
        List<VistaAntecedente> vistasAnt = new ArrayList<>();
        for (Antecedente a : ficha.getAntecedentes()) {
            vistasAnt.add(construirVistaAntecedente(a, autorizadoReservado));
        }
        EnriquecimientoFicha enriquecimiento = conEnriquecimiento
                ? enriquecerDesdeCasos(ficha.getIdCasoAlt())
                : EnriquecimientoFicha.noDisponible();
        return new VistaFicha(
                ficha.getAltKey(),
                ficha.getNumeroFicha(),
                ficha.getIdCasoAlt(),
                ficha.getNumeroExpediente(),
                ficha.getEstado(),
                ficha.getComplejidad(),
                ficha.getIdSedeAlt(),
                ficha.getIdUnidadAlt(),
                ficha.isEsBeta(),
                ficha.getAbiertaEn(),
                ficha.getCerradaEn(),
                ficha.getCreadaEn(),
                ficha.getActualizadaEn(),
                !autorizadoReservado,
                enriquecimiento,
                vistasVuln,
                vistasAnt,
                new ArrayList<>(ficha.getHistorial()));
    }

    private VistaVulneracion construirVistaVulneracion(Vulneracion v, boolean autorizadoReservado,
                                                       boolean conEtiquetas) {
        String relato = autorizadoReservado ? protector.descifrar(v.getRelatoCifrado()) : null;
        String etiquetaDerecho = conEtiquetas ? clienteCatalogo.etiquetaDerecho(v.getIdDerechoAlt()).orElse(null) : null;
        String etiquetaCausa = conEtiquetas && v.getIdCausaAlt() != null
                ? clienteCatalogo.etiquetaCausa(v.getIdCausaAlt()).orElse(null) : null;
        return new VistaVulneracion(
                v.getAltKey(),
                v.getIdDerechoAlt(),
                v.getIdCausaAlt(),
                v.getIdNnaAlt(),
                v.getNnaNombreLegible(),
                etiquetaDerecho,
                etiquetaCausa,
                v.getGravedad(),
                relato,
                v.getFechaHecho(),
                v.getRegistradoEn(),
                v.getRegistradoPor());
    }

    private VistaAntecedente construirVistaAntecedente(Antecedente a, boolean autorizadoReservado) {
        String descripcion = autorizadoReservado ? protector.descifrar(a.getDescripcionCifrada()) : null;
        return new VistaAntecedente(
                a.getAltKey(),
                a.getTipo(),
                descripcion,
                a.getFecha(),
                a.getFuente(),
                a.getRegistradoEn(),
                a.getRegistradoPor());
    }

    /** Enriquecimiento best-effort: degrada a "no disponible" ante cualquier ausencia o fallo. */
    private EnriquecimientoFicha enriquecerDesdeCasos(String idCasoAlt) {
        Optional<ClienteCasos.ResumenCaso> resumen = clienteCasos.obtener(idCasoAlt);
        return resumen
                .map(r -> new EnriquecimientoFicha(true, r.estado(), r.numeroExpediente()))
                .orElseGet(EnriquecimientoFicha::noDisponible);
    }

    private static boolean esVacio(String valor) {
        return valor == null || valor.isBlank();
    }
}
