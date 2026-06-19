package cl.smid.vulneraciones.dominio.modelo;

import cl.smid.vulneraciones.dominio.excepcion.FichaNoMutableException;
import cl.smid.vulneraciones.dominio.excepcion.TransicionInvalidaException;
import cl.smid.vulneraciones.dominio.servicio.MaquinaEstadosFicha;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Agregado raíz: Ficha Interna Reservada (FIR).
 *
 * <p>Es 1:1 con un Caso (6.4). Se materializa de forma asíncrona desde el evento {@code caso.abierto}
 * (solo si {@code requiereFichaReservada == true}) y nace como esqueleto en estado
 * {@link EstadoFicha#EN_ELABORACION}, con su asiento de apertura. Mantiene sus hijas (vulneraciones y
 * antecedentes) y su historial de transiciones, y concentra las invariantes: ventana de mutabilidad
 * y validez de transiciones.</p>
 *
 * <p>Entidad POJO pura: sin dependencias de framework. La llave numérica interna nunca cruza la
 * frontera del servicio; públicamente la ficha se identifica por {@code altKey}.</p>
 */
public class Ficha {

    /** Actor de sistema para asientos generados sin usuario (p. ej. la materialización). */
    public static final String ACTOR_SISTEMA = "00000000-0000-0000-0000-000000000000";

    private Long idInterno;
    private final String altKey;
    private final String numeroFicha;
    private final String codigoSede;
    private final SerieCorrelativo serie;
    private final long correlativo;
    private final int anio;
    private final String idCasoAlt;
    private final String numeroExpediente;
    private final String idSedeAlt;
    private final String idUnidadAlt;
    private final Complejidad complejidad;
    private final boolean esBeta;
    private EstadoFicha estado;
    private final Instant abiertaEn;
    private Instant cerradaEn;
    private final Instant creadaEn;
    private Instant actualizadaEn;
    private final String creadaPor;
    private boolean vigente;
    private final List<Vulneracion> vulneraciones;
    private final List<Antecedente> antecedentes;
    private final List<Transicion> historial;

    /**
     * Constructor de reconstrucción (usado por el mapeo de persistencia). Recibe las colecciones ya
     * cargadas. No valida reglas: confía en el estado persistido.
     */
    public Ficha(Long idInterno, String altKey, String numeroFicha, String codigoSede,
                 SerieCorrelativo serie, long correlativo, int anio, String idCasoAlt,
                 String numeroExpediente, String idSedeAlt, String idUnidadAlt, Complejidad complejidad,
                 boolean esBeta, EstadoFicha estado, Instant abiertaEn, Instant cerradaEn, Instant creadaEn,
                 Instant actualizadaEn, String creadaPor, boolean vigente, List<Vulneracion> vulneraciones,
                 List<Antecedente> antecedentes, List<Transicion> historial) {
        this.idInterno = idInterno;
        this.altKey = altKey;
        this.numeroFicha = numeroFicha;
        this.codigoSede = codigoSede;
        this.serie = serie;
        this.correlativo = correlativo;
        this.anio = anio;
        this.idCasoAlt = idCasoAlt;
        this.numeroExpediente = numeroExpediente;
        this.idSedeAlt = idSedeAlt;
        this.idUnidadAlt = idUnidadAlt;
        this.complejidad = complejidad;
        this.esBeta = esBeta;
        this.estado = estado;
        this.abiertaEn = abiertaEn;
        this.cerradaEn = cerradaEn;
        this.creadaEn = creadaEn;
        this.actualizadaEn = actualizadaEn;
        this.creadaPor = creadaPor;
        this.vigente = vigente;
        this.vulneraciones = vulneraciones == null ? new ArrayList<>() : new ArrayList<>(vulneraciones);
        this.antecedentes = antecedentes == null ? new ArrayList<>() : new ArrayList<>(antecedentes);
        this.historial = historial == null ? new ArrayList<>() : new ArrayList<>(historial);
    }

    /**
     * Materializa una FIR nueva a partir de las referencias del evento y del correlativo ya reservado.
     * Genera el asiento de apertura (origen {@code null}, acción {@code MATERIALIZACION}, actor de
     * sistema) y deja la ficha en {@link EstadoFicha#EN_ELABORACION}.
     *
     * @param altKey            identificador opaco de la ficha
     * @param numero            número de FIR (aporta código de sede, serie, correlativo y año)
     * @param idCasoAlt         {@code alt_key} del caso de origen
     * @param numeroExpediente  número de expediente del caso (snapshot)
     * @param idSedeAlt         {@code alt_key} de la sede del caso
     * @param idUnidadAlt       {@code alt_key} de la unidad del caso (puede ser nulo)
     * @param complejidad       complejidad del caso (puede ser nula)
     * @param ahora             instante de materialización (UTC)
     * @param altKeyTransicion  identificador opaco del asiento de apertura
     * @return la ficha esqueleto recién materializada (aún sin llave interna)
     */
    public static Ficha materializar(String altKey, NumeroFir numero, String idCasoAlt,
                                     String numeroExpediente, String idSedeAlt, String idUnidadAlt,
                                     Complejidad complejidad, Instant ahora, String altKeyTransicion) {
        List<Transicion> historialInicial = new ArrayList<>();
        historialInicial.add(new Transicion(
                altKeyTransicion,
                null,
                EstadoFicha.EN_ELABORACION,
                AccionFicha.MATERIALIZACION,
                "Materialización de la FIR desde el evento caso.abierto.",
                ACTOR_SISTEMA,
                ahora));
        return new Ficha(
                null,
                altKey,
                numero.formatear(),
                numero.codigoSede(),
                numero.serie(),
                numero.correlativo(),
                numero.anio(),
                idCasoAlt,
                numeroExpediente,
                idSedeAlt,
                idUnidadAlt,
                complejidad,
                numero.serie().esBeta(),
                EstadoFicha.EN_ELABORACION,
                ahora,
                null,
                ahora,
                ahora,
                null,
                true,
                new ArrayList<>(),
                new ArrayList<>(),
                historialInicial);
    }

    /** @return {@code true} si la ficha admite el alta de vulneraciones/antecedentes. */
    public boolean esMutable() {
        return vigente && estado == EstadoFicha.EN_ELABORACION;
    }

    /**
     * Verifica la ventana de mutabilidad.
     *
     * @throws FichaNoMutableException si la ficha no admite modificaciones (VUL-409)
     */
    public void asegurarMutable() {
        if (!esMutable()) {
            throw new FichaNoMutableException(
                    "La ficha " + altKey + " no admite modificaciones en estado " + estado + ".");
        }
    }

    /**
     * Agrega una vulneración respetando la ventana de mutabilidad y refresca la marca de
     * actualización con el instante de registro de la vulneración.
     *
     * @throws FichaNoMutableException si la ficha no es mutable (VUL-409)
     */
    public void registrarVulneracion(Vulneracion vulneracion) {
        asegurarMutable();
        vulneraciones.add(vulneracion);
        this.actualizadaEn = vulneracion.getRegistradoEn();
    }

    /**
     * Agrega un antecedente respetando la ventana de mutabilidad y refresca la marca de actualización.
     *
     * @throws FichaNoMutableException si la ficha no es mutable (VUL-409)
     */
    public void registrarAntecedente(Antecedente antecedente) {
        asegurarMutable();
        antecedentes.add(antecedente);
        this.actualizadaEn = antecedente.getRegistradoEn();
    }

    /**
     * Aplica una transición de estado validada por la máquina de estados, registra el asiento
     * correspondiente y actualiza las marcas temporales. El control de rol de Coordinación para las
     * acciones administrativas es responsabilidad del servicio de aplicación (no del agregado).
     *
     * @param accion           acción solicitada (CERRAR/REABRIR)
     * @param maquina          máquina de estados (tabla pura de transiciones)
     * @param observacion      observación opcional
     * @param actorAlt         {@code alt_key} del usuario que ejecuta la acción
     * @param ahora            instante de la transición (UTC)
     * @param altKeyTransicion identificador opaco del nuevo asiento
     * @return el asiento de transición recién registrado
     * @throws TransicionInvalidaException si la transición no está contemplada para el estado actual
     */
    public Transicion transicionar(AccionFicha accion, MaquinaEstadosFicha maquina, String observacion,
                                   String actorAlt, Instant ahora, String altKeyTransicion) {
        EstadoFicha destino = maquina.siguiente(estado, accion)
                .orElseThrow(() -> new TransicionInvalidaException(
                        "Transición no permitida: " + estado + " --" + accion + "-->."));
        EstadoFicha origen = this.estado;
        Transicion asiento = new Transicion(altKeyTransicion, origen, destino, accion, observacion,
                actorAlt, ahora);
        historial.add(asiento);
        this.estado = destino;
        if (destino == EstadoFicha.CERRADA) {
            this.cerradaEn = ahora;
        }
        if (accion == AccionFicha.REABRIR) {
            this.cerradaEn = null;
        }
        this.actualizadaEn = ahora;
        return asiento;
    }

    /** Asigna la llave interna tras la persistencia (uso exclusivo del adaptador de persistencia). */
    public void asignarIdInterno(Long idInterno) {
        this.idInterno = idInterno;
    }

    public Long getIdInterno() {
        return idInterno;
    }

    public String getAltKey() {
        return altKey;
    }

    public String getNumeroFicha() {
        return numeroFicha;
    }

    public String getCodigoSede() {
        return codigoSede;
    }

    public SerieCorrelativo getSerie() {
        return serie;
    }

    public long getCorrelativo() {
        return correlativo;
    }

    public int getAnio() {
        return anio;
    }

    public String getIdCasoAlt() {
        return idCasoAlt;
    }

    public String getNumeroExpediente() {
        return numeroExpediente;
    }

    public String getIdSedeAlt() {
        return idSedeAlt;
    }

    public String getIdUnidadAlt() {
        return idUnidadAlt;
    }

    public Complejidad getComplejidad() {
        return complejidad;
    }

    public boolean isEsBeta() {
        return esBeta;
    }

    public EstadoFicha getEstado() {
        return estado;
    }

    public Instant getAbiertaEn() {
        return abiertaEn;
    }

    public Instant getCerradaEn() {
        return cerradaEn;
    }

    public Instant getCreadaEn() {
        return creadaEn;
    }

    public Instant getActualizadaEn() {
        return actualizadaEn;
    }

    public String getCreadaPor() {
        return creadaPor;
    }

    public boolean isVigente() {
        return vigente;
    }

    /** @return vista inmutable de las vulneraciones de la ficha. */
    public List<Vulneracion> getVulneraciones() {
        return Collections.unmodifiableList(vulneraciones);
    }

    /** @return vista inmutable de los antecedentes de la ficha. */
    public List<Antecedente> getAntecedentes() {
        return Collections.unmodifiableList(antecedentes);
    }

    /** @return vista inmutable del historial de transiciones de la ficha. */
    public List<Transicion> getHistorial() {
        return Collections.unmodifiableList(historial);
    }
}
