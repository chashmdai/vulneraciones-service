package cl.smid.vulneraciones.dominio.servicio;

import cl.smid.vulneraciones.dominio.modelo.NumeroFir;
import cl.smid.vulneraciones.dominio.modelo.SerieCorrelativo;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;

/**
 * Servicio de dominio que decide la serie del correlativo y arma el número de FIR.
 *
 * <p>Política de serie: si el evento informa {@code esBeta}, tiene prioridad; si no, se decide por
 * fecha contra {@code inicioOficial} (apertura anterior al corte ⇒ BETA; en o después ⇒ OFICIAL).
 * El año del número se toma del instante de apertura del caso, en la zona de referencia (UTC).</p>
 *
 * <p>POJO puro: la fecha de corte y la zona se inyectan al construirlo (desde configuración, vía
 * {@code CableadoDominio}); el servicio no conoce Spring.</p>
 */
public class GeneradorNumeroFir {

    private final LocalDate inicioOficial;
    private final ZoneId zona;

    /**
     * @param inicioOficial fecha de corte a partir de la cual la serie es OFICIAL
     * @param zona          zona de referencia para derivar fecha/año (debe ser UTC en el clúster)
     */
    public GeneradorNumeroFir(LocalDate inicioOficial, ZoneId zona) {
        this.inicioOficial = Objects.requireNonNull(inicioOficial, "inicioOficial es obligatorio");
        this.zona = Objects.requireNonNull(zona, "zona es obligatoria");
    }

    /**
     * Resuelve la serie del correlativo.
     *
     * @param esBetaEvento bandera informada por el caso (puede ser nula)
     * @param ocurridoEn   instante de apertura del caso (no nulo)
     * @return la serie a usar
     */
    public SerieCorrelativo resolverSerie(Boolean esBetaEvento, Instant ocurridoEn) {
        Objects.requireNonNull(ocurridoEn, "ocurridoEn es obligatorio para resolver la serie");
        if (esBetaEvento != null) {
            return SerieCorrelativo.desdeBandera(esBetaEvento);
        }
        LocalDate fecha = ocurridoEn.atZone(zona).toLocalDate();
        return fecha.isBefore(inicioOficial) ? SerieCorrelativo.BETA : SerieCorrelativo.OFICIAL;
    }

    /**
     * Año a usar en el número de FIR, derivado del instante de apertura del caso.
     *
     * @param ocurridoEn instante de apertura del caso (no nulo)
     * @return el año en la zona de referencia
     */
    public int anioDe(Instant ocurridoEn) {
        Objects.requireNonNull(ocurridoEn, "ocurridoEn es obligatorio para derivar el año");
        return ocurridoEn.atZone(zona).getYear();
    }

    /**
     * Arma el objeto de valor del número de FIR.
     *
     * @param codigoSede  código corto de la sede
     * @param serie       serie del correlativo
     * @param correlativo correlativo reservado (positivo)
     * @param anio        año de la serie
     * @return el número de FIR
     */
    public NumeroFir construir(String codigoSede, SerieCorrelativo serie, long correlativo, int anio) {
        return new NumeroFir(codigoSede, serie, correlativo, anio);
    }
}
