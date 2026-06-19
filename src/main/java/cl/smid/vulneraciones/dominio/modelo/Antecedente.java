package cl.smid.vulneraciones.dominio.modelo;

import cl.smid.vulneraciones.dominio.excepcion.ReglaNegocioException;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Entrada de antecedente/relato de contexto del caso dentro de una FIR.
 *
 * <p>Entidad de dominio POJO. El campo {@code descripcionCifrada} es la representación en reposo del
 * dato reservado; el cifrado/descifrado se hace por el puerto {@code ProtectorDatosReservados}. La
 * descripción jamás viaja en eventos ni se escribe en logs (G7 / DT-1).</p>
 */
public class Antecedente {

    private Long idInterno;
    private final String altKey;
    private final TipoAntecedente tipo;
    private final String descripcionCifrada;
    private final LocalDate fecha;
    private final String fuente;
    private final Instant registradoEn;
    private final String registradoPor;

    /**
     * Constructor de reconstrucción (usado por el mapeo de persistencia).
     */
    public Antecedente(Long idInterno, String altKey, TipoAntecedente tipo, String descripcionCifrada,
                       LocalDate fecha, String fuente, Instant registradoEn, String registradoPor) {
        this.idInterno = idInterno;
        this.altKey = altKey;
        this.tipo = tipo;
        this.descripcionCifrada = descripcionCifrada;
        this.fecha = fecha;
        this.fuente = fuente;
        this.registradoEn = registradoEn;
        this.registradoPor = registradoPor;
    }

    /**
     * Factoría de alta. Exige un tipo válido y fija la marca temporal. La {@code descripcionCifrada}
     * ya debe venir cifrada por el puerto correspondiente.
     *
     * @throws ReglaNegocioException si el tipo es nulo (VUL-422)
     */
    public static Antecedente crear(String altKey, TipoAntecedente tipo, String descripcionCifrada,
                                    LocalDate fecha, String fuente, String registradoPor, Instant ahora) {
        if (tipo == null) {
            throw new ReglaNegocioException("El antecedente requiere un tipo válido.");
        }
        return new Antecedente(null, altKey, tipo, descripcionCifrada, fecha,
                (fuente == null || fuente.isBlank()) ? null : fuente.trim(),
                ahora, registradoPor);
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

    public TipoAntecedente getTipo() {
        return tipo;
    }

    /** @return la representación en reposo (cifrada) de la descripción; puede ser nula. */
    public String getDescripcionCifrada() {
        return descripcionCifrada;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public String getFuente() {
        return fuente;
    }

    public Instant getRegistradoEn() {
        return registradoEn;
    }

    public String getRegistradoPor() {
        return registradoPor;
    }
}
