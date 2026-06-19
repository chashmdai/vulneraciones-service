package cl.smid.vulneraciones.dominio.servicio;

import cl.smid.vulneraciones.dominio.modelo.AccionFicha;
import cl.smid.vulneraciones.dominio.modelo.EstadoFicha;

import java.util.Map;
import java.util.Optional;

/**
 * Máquina de estados de la FIR expresada como una <strong>tabla pura</strong>
 * {@code Map<(estado, acción) → estado destino>}.
 *
 * <p>Transiciones contempladas:</p>
 * <pre>
 *   EN_ELABORACION ──CERRAR──▶ CERRADA
 *   CERRADA        ──REABRIR─▶ EN_ELABORACION   (reapertura administrativa)
 * </pre>
 *
 * <p>El asiento de apertura ({@code MATERIALIZACION}) es génesis del agregado y no forma parte de la
 * tabla. Una transición no contemplada se traduce en VUL-409.</p>
 */
public class MaquinaEstadosFicha {

    /** Clave compuesta de la tabla de transiciones. */
    private record Clave(EstadoFicha origen, AccionFicha accion) {
    }

    private final Map<Clave, EstadoFicha> transiciones = Map.of(
            new Clave(EstadoFicha.EN_ELABORACION, AccionFicha.CERRAR), EstadoFicha.CERRADA,
            new Clave(EstadoFicha.CERRADA, AccionFicha.REABRIR), EstadoFicha.EN_ELABORACION);

    /**
     * Calcula el estado destino de una transición.
     *
     * @param origen estado actual de la ficha
     * @param accion acción solicitada
     * @return el estado destino si la transición está contemplada; {@link Optional#empty()} si no
     */
    public Optional<EstadoFicha> siguiente(EstadoFicha origen, AccionFicha accion) {
        return Optional.ofNullable(transiciones.get(new Clave(origen, accion)));
    }

    /**
     * @param origen estado actual
     * @param accion acción solicitada
     * @return {@code true} si la transición está contemplada
     */
    public boolean permite(EstadoFicha origen, AccionFicha accion) {
        return transiciones.containsKey(new Clave(origen, accion));
    }
}
