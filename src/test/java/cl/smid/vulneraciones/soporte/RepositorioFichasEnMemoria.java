package cl.smid.vulneraciones.soporte;

import cl.smid.vulneraciones.dominio.excepcion.FichaDuplicadaException;
import cl.smid.vulneraciones.dominio.modelo.Ficha;
import cl.smid.vulneraciones.dominio.modelo.ResultadoPagina;
import cl.smid.vulneraciones.dominio.modelo.Transicion;
import cl.smid.vulneraciones.dominio.modelo.Vulneracion;
import cl.smid.vulneraciones.dominio.modelo.Antecedente;
import cl.smid.vulneraciones.dominio.puerto.entrada.comando.CriterioListadoFichas;
import cl.smid.vulneraciones.dominio.puerto.salida.RepositorioFichas;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Doble en memoria del repositorio de fichas para pruebas unitarias.
 *
 * <p>Emula la unicidad de {@code id_caso_alt} (un segundo intento sobre el mismo caso lanza
 * {@link FichaDuplicadaException}, como la restricción {@code uk_ficha_caso}) y un listado con filtro
 * territorial análogo al de la {@code Specification} de producción. Al almacenar la misma referencia
 * del agregado, las mutaciones del dominio (vulneraciones, antecedentes, transiciones) quedan
 * reflejadas sin trabajo adicional.</p>
 */
public class RepositorioFichasEnMemoria implements RepositorioFichas {

    private final Map<String, Ficha> porAltKey = new LinkedHashMap<>();
    private final Map<String, Ficha> porCaso = new LinkedHashMap<>();
    private final AtomicLong secuencia = new AtomicLong(0);

    @Override
    public Ficha materializar(Ficha nueva) {
        if (porCaso.containsKey(nueva.getIdCasoAlt())) {
            throw new FichaDuplicadaException(
                    "Ya existe una ficha para el caso " + nueva.getIdCasoAlt() + ".");
        }
        nueva.asignarIdInterno(secuencia.incrementAndGet());
        porAltKey.put(nueva.getAltKey(), nueva);
        porCaso.put(nueva.getIdCasoAlt(), nueva);
        return nueva;
    }

    @Override
    public boolean existePorCaso(String idCasoAlt) {
        return porCaso.containsKey(idCasoAlt);
    }

    @Override
    public Optional<Ficha> buscarPorAltKey(String altKey) {
        return Optional.ofNullable(porAltKey.get(altKey));
    }

    @Override
    public ResultadoPagina<Ficha> listar(CriterioListadoFichas criterio) {
        List<Ficha> filtradas = new ArrayList<>();
        for (Ficha f : porAltKey.values()) {
            if (!f.isVigente()) {
                continue;
            }
            if (!dentroDeAlcance(f, criterio)) {
                continue;
            }
            if (criterio.idCasoAlt() != null && !criterio.idCasoAlt().equals(f.getIdCasoAlt())) {
                continue;
            }
            if (criterio.estado() != null && criterio.estado() != f.getEstado()) {
                continue;
            }
            filtradas.add(f);
        }
        filtradas.sort(Comparator.comparing(Ficha::getIdInterno).reversed());
        long total = filtradas.size();
        int desde = Math.max(0, criterio.pagina()) * Math.max(1, criterio.tamano());
        int hasta = Math.min(filtradas.size(), desde + Math.max(1, criterio.tamano()));
        List<Ficha> pagina = desde >= filtradas.size() ? List.of() : filtradas.subList(desde, hasta);
        return new ResultadoPagina<>(new ArrayList<>(pagina), criterio.pagina(), criterio.tamano(), total);
    }

    private boolean dentroDeAlcance(Ficha f, CriterioListadoFichas criterio) {
        return switch (criterio.alcance()) {
            case NACIONAL -> true;
            case SEDE -> criterio.idSedeUsuario() != null
                    && criterio.idSedeUsuario().equals(f.getIdSedeAlt());
            case UNIDAD -> criterio.idUnidadUsuario() != null
                    && criterio.idUnidadUsuario().equals(f.getIdUnidadAlt());
        };
    }

    @Override
    public Vulneracion agregarVulneracion(Ficha ficha, Vulneracion vulneracion) {
        // El agregado ya incorporó la vulneración; el doble solo confirma la persistencia.
        return vulneracion;
    }

    @Override
    public Antecedente agregarAntecedente(Ficha ficha, Antecedente antecedente) {
        return antecedente;
    }

    @Override
    public Transicion aplicarTransicion(Ficha ficha, Transicion transicion) {
        return transicion;
    }
}
