package cl.smid.vulneraciones.dominio.puerto.salida;

import cl.smid.vulneraciones.dominio.modelo.Antecedente;
import cl.smid.vulneraciones.dominio.modelo.Ficha;
import cl.smid.vulneraciones.dominio.modelo.ResultadoPagina;
import cl.smid.vulneraciones.dominio.modelo.Transicion;
import cl.smid.vulneraciones.dominio.modelo.Vulneracion;
import cl.smid.vulneraciones.dominio.puerto.entrada.comando.CriterioListadoFichas;

import java.util.Optional;

/**
 * Puerto de salida: persistencia del agregado FIR y sus hijas.
 *
 * <p>Las operaciones corren dentro de la transacción demarcada en la frontera (controlador o
 * listener). La unicidad sobre {@code id_caso_alt} la traduce el adaptador a
 * {@code FichaDuplicadaException}.</p>
 */
public interface RepositorioFichas {

    /**
     * Inserta una ficha nueva junto con su asiento de apertura.
     *
     * @param nueva ficha esqueleto recién materializada (sin llave interna)
     * @return la ficha con su llave interna asignada
     * @throws cl.smid.vulneraciones.dominio.excepcion.FichaDuplicadaException si ya existe una ficha
     *                                                                         para el mismo caso
     */
    Ficha materializar(Ficha nueva);

    /**
     * @param idCasoAlt {@code alt_key} del caso
     * @return {@code true} si ya existe una ficha para ese caso (pre-chequeo de idempotencia)
     */
    boolean existePorCaso(String idCasoAlt);

    /**
     * Carga el agregado completo (ficha + vulneraciones + antecedentes + historial) por su
     * identificador público.
     *
     * @param altKey identificador opaco de la ficha
     * @return el agregado si existe; {@link Optional#empty()} si no
     */
    Optional<Ficha> buscarPorAltKey(String altKey);

    /**
     * Lista fichas (resumen) aplicando el filtro territorial y los filtros opcionales del criterio.
     *
     * @param criterio criterio de búsqueda y paginación
     * @return página de fichas (sin cargar las colecciones hijas)
     */
    ResultadoPagina<Ficha> listar(CriterioListadoFichas criterio);

    /**
     * Persiste una vulneración asociada a la ficha y refresca la marca de actualización de la ficha.
     *
     * @param ficha       ficha destino (ya validada y mutada en memoria)
     * @param vulneracion vulneración a persistir
     * @return la vulneración con su llave interna asignada
     */
    Vulneracion agregarVulneracion(Ficha ficha, Vulneracion vulneracion);

    /**
     * Persiste un antecedente asociado a la ficha y refresca la marca de actualización de la ficha.
     *
     * @param ficha       ficha destino (ya validada y mutada en memoria)
     * @param antecedente antecedente a persistir
     * @return el antecedente con su llave interna asignada
     */
    Antecedente agregarAntecedente(Ficha ficha, Antecedente antecedente);

    /**
     * Persiste el cambio de estado de la ficha y el asiento de transición correspondiente.
     *
     * @param ficha     ficha cuyo estado ya fue mutado en memoria
     * @param transicion asiento de transición a persistir
     * @return el asiento persistido
     */
    Transicion aplicarTransicion(Ficha ficha, Transicion transicion);
}
