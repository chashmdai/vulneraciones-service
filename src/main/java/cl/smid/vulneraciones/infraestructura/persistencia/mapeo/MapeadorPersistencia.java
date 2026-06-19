package cl.smid.vulneraciones.infraestructura.persistencia.mapeo;

import cl.smid.vulneraciones.dominio.modelo.AccionFicha;
import cl.smid.vulneraciones.dominio.modelo.Antecedente;
import cl.smid.vulneraciones.dominio.modelo.Complejidad;
import cl.smid.vulneraciones.dominio.modelo.EstadoFicha;
import cl.smid.vulneraciones.dominio.modelo.Ficha;
import cl.smid.vulneraciones.dominio.modelo.Gravedad;
import cl.smid.vulneraciones.dominio.modelo.SerieCorrelativo;
import cl.smid.vulneraciones.dominio.modelo.TipoAntecedente;
import cl.smid.vulneraciones.dominio.modelo.Transicion;
import cl.smid.vulneraciones.dominio.modelo.Vulneracion;
import cl.smid.vulneraciones.infraestructura.persistencia.entidad.AntecedenteEntity;
import cl.smid.vulneraciones.infraestructura.persistencia.entidad.FichaEntity;
import cl.smid.vulneraciones.infraestructura.persistencia.entidad.TransicionEntity;
import cl.smid.vulneraciones.infraestructura.persistencia.entidad.VulneracionEntity;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Traductor entre el dominio POJO puro y las entidades JPA.
 *
 * <p>Concentra dos conversiones delicadas: los enumerados de dominio ↔ {@code VARCHAR} (cadena) y las
 * marcas temporales {@code Instant} (UTC) ↔ {@link LocalDateTime} (hora mural). Se almacena la hora
 * mural UTC verbatim en {@code DATETIME(6)}; al reconstruir, se reinterpreta en UTC. Clase de
 * utilidad sin estado.</p>
 */
public final class MapeadorPersistencia {

    private MapeadorPersistencia() {
    }

    // ------------------------------------------------------------------ tiempos

    /** Convierte un instante UTC a hora mural para persistir en {@code DATETIME(6)}. */
    public static LocalDateTime aLocal(Instant instante) {
        return instante == null ? null : LocalDateTime.ofInstant(instante, ZoneOffset.UTC);
    }

    /** Reinterpreta en UTC la hora mural almacenada y devuelve el instante. */
    public static Instant aInstante(LocalDateTime local) {
        return local == null ? null : local.toInstant(ZoneOffset.UTC);
    }

    // ------------------------------------------------------------------ ficha

    /**
     * Construye la entidad de inserción de una ficha recién materializada (sin {@code id}).
     */
    public static FichaEntity aEntidad(Ficha ficha) {
        FichaEntity e = new FichaEntity();
        e.setAltKey(ficha.getAltKey());
        e.setNumeroFicha(ficha.getNumeroFicha());
        e.setCodigoSede(ficha.getCodigoSede());
        e.setSerie(ficha.getSerie().name());
        e.setCorrelativo(ficha.getCorrelativo());
        e.setAnio(ficha.getAnio());
        e.setIdCasoAlt(ficha.getIdCasoAlt());
        e.setNumeroExpediente(ficha.getNumeroExpediente());
        e.setIdSedeAlt(ficha.getIdSedeAlt());
        e.setIdUnidadAlt(ficha.getIdUnidadAlt());
        e.setComplejidad(ficha.getComplejidad() == null ? null : ficha.getComplejidad().name());
        e.setEsBeta(ficha.isEsBeta());
        e.setEstado(ficha.getEstado().name());
        e.setAbiertaEn(aLocal(ficha.getAbiertaEn()));
        e.setCerradaEn(aLocal(ficha.getCerradaEn()));
        e.setCreadaEn(aLocal(ficha.getCreadaEn()));
        e.setActualizadaEn(aLocal(ficha.getActualizadaEn()));
        e.setCreadaPor(ficha.getCreadaPor());
        e.setVigente(ficha.isVigente());
        return e;
    }

    /**
     * Reconstruye el agregado a partir de la entidad de la ficha y sus colecciones ya mapeadas.
     */
    public static Ficha aDominio(FichaEntity e, List<Vulneracion> vulneraciones,
                                 List<Antecedente> antecedentes, List<Transicion> historial) {
        return new Ficha(
                e.getId(),
                e.getAltKey(),
                e.getNumeroFicha(),
                e.getCodigoSede(),
                SerieCorrelativo.desde(e.getSerie()),
                e.getCorrelativo(),
                e.getAnio(),
                e.getIdCasoAlt(),
                e.getNumeroExpediente(),
                e.getIdSedeAlt(),
                e.getIdUnidadAlt(),
                Complejidad.desde(e.getComplejidad()),
                e.isEsBeta(),
                EstadoFicha.desde(e.getEstado()),
                aInstante(e.getAbiertaEn()),
                aInstante(e.getCerradaEn()),
                aInstante(e.getCreadaEn()),
                aInstante(e.getActualizadaEn()),
                e.getCreadaPor(),
                e.isVigente(),
                vulneraciones,
                antecedentes,
                historial);
    }

    // ------------------------------------------------------------------ vulneración

    /** Construye la entidad de inserción de una vulneración para una ficha dada. */
    public static VulneracionEntity aEntidad(Vulneracion v, Long idFicha) {
        VulneracionEntity e = new VulneracionEntity();
        e.setAltKey(v.getAltKey());
        e.setIdFicha(idFicha);
        e.setIdDerechoAlt(v.getIdDerechoAlt());
        e.setIdCausaAlt(v.getIdCausaAlt());
        e.setIdNnaAlt(v.getIdNnaAlt());
        e.setNnaNombreLegible(v.getNnaNombreLegible());
        e.setGravedad(v.getGravedad().name());
        e.setRelatoCifrado(v.getRelatoCifrado());
        e.setFechaHecho(v.getFechaHecho());
        e.setRegistradoEn(aLocal(v.getRegistradoEn()));
        e.setActualizadoEn(aLocal(v.getActualizadoEn()));
        e.setRegistradoPor(v.getRegistradoPor());
        return e;
    }

    /** Reconstruye una vulneración de dominio desde su entidad. */
    public static Vulneracion aDominio(VulneracionEntity e) {
        return new Vulneracion(
                e.getId(),
                e.getAltKey(),
                e.getIdDerechoAlt(),
                e.getIdCausaAlt(),
                e.getIdNnaAlt(),
                e.getNnaNombreLegible(),
                Gravedad.desde(e.getGravedad()),
                e.getRelatoCifrado(),
                e.getFechaHecho(),
                aInstante(e.getRegistradoEn()),
                aInstante(e.getActualizadoEn()),
                e.getRegistradoPor());
    }

    // ------------------------------------------------------------------ antecedente

    /** Construye la entidad de inserción de un antecedente para una ficha dada. */
    public static AntecedenteEntity aEntidad(Antecedente a, Long idFicha) {
        AntecedenteEntity e = new AntecedenteEntity();
        e.setAltKey(a.getAltKey());
        e.setIdFicha(idFicha);
        e.setTipo(a.getTipo().name());
        e.setDescripcionCifrada(a.getDescripcionCifrada());
        e.setFecha(a.getFecha());
        e.setFuente(a.getFuente());
        e.setRegistradoEn(aLocal(a.getRegistradoEn()));
        e.setRegistradoPor(a.getRegistradoPor());
        return e;
    }

    /** Reconstruye un antecedente de dominio desde su entidad. */
    public static Antecedente aDominio(AntecedenteEntity e) {
        return new Antecedente(
                e.getId(),
                e.getAltKey(),
                TipoAntecedente.desde(e.getTipo()),
                e.getDescripcionCifrada(),
                e.getFecha(),
                e.getFuente(),
                aInstante(e.getRegistradoEn()),
                e.getRegistradoPor());
    }

    // ------------------------------------------------------------------ transición

    /** Construye la entidad de inserción de un asiento de transición para una ficha dada. */
    public static TransicionEntity aEntidad(Transicion t, Long idFicha) {
        TransicionEntity e = new TransicionEntity();
        e.setAltKey(t.altKey());
        e.setIdFicha(idFicha);
        e.setEstadoOrigen(t.estadoOrigen() == null ? null : t.estadoOrigen().name());
        e.setEstadoDestino(t.estadoDestino().name());
        e.setAccion(t.accion().name());
        e.setObservacion(t.observacion());
        e.setActorAlt(t.actorAlt());
        e.setOcurridoEn(aLocal(t.ocurridoEn()));
        return e;
    }

    /** Reconstruye un asiento de transición de dominio desde su entidad. */
    public static Transicion aDominio(TransicionEntity e) {
        return new Transicion(
                e.getAltKey(),
                EstadoFicha.desde(e.getEstadoOrigen()),
                EstadoFicha.desde(e.getEstadoDestino()),
                AccionFicha.valueOf(e.getAccion()),
                e.getObservacion(),
                e.getActorAlt(),
                aInstante(e.getOcurridoEn()));
    }
}
