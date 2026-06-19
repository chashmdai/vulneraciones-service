package cl.smid.vulneraciones.dominio.modelo;

import cl.smid.vulneraciones.dominio.excepcion.ReglaNegocioException;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Registro de una vulneración de derecho concreta dentro de una FIR.
 *
 * <p>Entidad de dominio POJO (sin Spring/JPA/Lombok). El campo {@code relatoCifrado} es la
 * representación en reposo del relato reservado: el dominio nunca conserva el texto en claro de forma
 * persistente; el cifrado/descifrado se hace a través del puerto {@code ProtectorDatosReservados}.
 * El relato jamás viaja en eventos ni se escribe en logs (G7 / DT-1).</p>
 */
public class Vulneracion {

    private Long idInterno;
    private final String altKey;
    private final String idDerechoAlt;
    private final String idCausaAlt;
    private final String idNnaAlt;
    private final String nnaNombreLegible;
    private final Gravedad gravedad;
    private final String relatoCifrado;
    private final LocalDate fechaHecho;
    private final Instant registradoEn;
    private final Instant actualizadoEn;
    private final String registradoPor;

    /**
     * Constructor de reconstrucción (usado por el mapeo de persistencia). No valida reglas de
     * negocio: confía en el estado ya persistido.
     */
    public Vulneracion(Long idInterno, String altKey, String idDerechoAlt, String idCausaAlt,
                       String idNnaAlt, String nnaNombreLegible, Gravedad gravedad, String relatoCifrado,
                       LocalDate fechaHecho, Instant registradoEn, Instant actualizadoEn, String registradoPor) {
        this.idInterno = idInterno;
        this.altKey = altKey;
        this.idDerechoAlt = idDerechoAlt;
        this.idCausaAlt = idCausaAlt;
        this.idNnaAlt = idNnaAlt;
        this.nnaNombreLegible = nnaNombreLegible;
        this.gravedad = gravedad;
        this.relatoCifrado = relatoCifrado;
        this.fechaHecho = fechaHecho;
        this.registradoEn = registradoEn;
        this.actualizadoEn = actualizadoEn;
        this.registradoPor = registradoPor;
    }

    /**
     * Factoría de alta. Valida las reglas de negocio (derecho y NNA obligatorios) y fija las marcas
     * temporales. El {@code relatoCifrado} ya debe venir cifrado por el puerto correspondiente.
     *
     * @throws ReglaNegocioException si falta el derecho o el NNA (VUL-422)
     */
    public static Vulneracion crear(String altKey, String idDerechoAlt, String idCausaAlt,
                                    String idNnaAlt, String nnaNombreLegible, Gravedad gravedad,
                                    String relatoCifrado, LocalDate fechaHecho, String registradoPor,
                                    Instant ahora) {
        if (idDerechoAlt == null || idDerechoAlt.isBlank()) {
            throw new ReglaNegocioException("La vulneración requiere el derecho vulnerado (idDerechoAlt).");
        }
        if (idNnaAlt == null || idNnaAlt.isBlank()) {
            throw new ReglaNegocioException("La vulneración requiere el NNA afectado (idNnaAlt).");
        }
        if (gravedad == null) {
            throw new ReglaNegocioException("La vulneración requiere una gravedad válida.");
        }
        return new Vulneracion(null, altKey, idDerechoAlt.trim(),
                (idCausaAlt == null || idCausaAlt.isBlank()) ? null : idCausaAlt.trim(),
                idNnaAlt.trim(), nnaNombreLegible, gravedad, relatoCifrado, fechaHecho,
                ahora, ahora, registradoPor);
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

    public String getIdDerechoAlt() {
        return idDerechoAlt;
    }

    public String getIdCausaAlt() {
        return idCausaAlt;
    }

    public String getIdNnaAlt() {
        return idNnaAlt;
    }

    public String getNnaNombreLegible() {
        return nnaNombreLegible;
    }

    public Gravedad getGravedad() {
        return gravedad;
    }

    /** @return la representación en reposo (cifrada) del relato; puede ser nula. */
    public String getRelatoCifrado() {
        return relatoCifrado;
    }

    public LocalDate getFechaHecho() {
        return fechaHecho;
    }

    public Instant getRegistradoEn() {
        return registradoEn;
    }

    public Instant getActualizadoEn() {
        return actualizadoEn;
    }

    public String getRegistradoPor() {
        return registradoPor;
    }
}
