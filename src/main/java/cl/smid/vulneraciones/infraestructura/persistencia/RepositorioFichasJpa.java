package cl.smid.vulneraciones.infraestructura.persistencia;

import cl.smid.vulneraciones.dominio.excepcion.FichaDuplicadaException;
import cl.smid.vulneraciones.dominio.modelo.Antecedente;
import cl.smid.vulneraciones.dominio.modelo.Ficha;
import cl.smid.vulneraciones.dominio.modelo.ResultadoPagina;
import cl.smid.vulneraciones.dominio.modelo.Transicion;
import cl.smid.vulneraciones.dominio.modelo.Vulneracion;
import cl.smid.vulneraciones.dominio.puerto.entrada.comando.CriterioListadoFichas;
import cl.smid.vulneraciones.dominio.puerto.salida.RepositorioFichas;
import cl.smid.vulneraciones.infraestructura.persistencia.entidad.AntecedenteEntity;
import cl.smid.vulneraciones.infraestructura.persistencia.entidad.FichaEntity;
import cl.smid.vulneraciones.infraestructura.persistencia.entidad.TransicionEntity;
import cl.smid.vulneraciones.infraestructura.persistencia.entidad.VulneracionEntity;
import cl.smid.vulneraciones.infraestructura.persistencia.mapeo.MapeadorPersistencia;
import cl.smid.vulneraciones.infraestructura.persistencia.repositorio.AntecedenteJpaRepository;
import cl.smid.vulneraciones.infraestructura.persistencia.repositorio.FichaJpaRepository;
import cl.smid.vulneraciones.infraestructura.persistencia.repositorio.TransicionJpaRepository;
import cl.smid.vulneraciones.infraestructura.persistencia.repositorio.VulneracionJpaRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Adaptador de salida del puerto {@link RepositorioFichas} sobre Spring Data JPA.
 *
 * <p>No declara {@code @Transactional}: cada operación corre dentro de la transacción demarcada en la
 * frontera (controlador o listener). La violación de unicidad sobre {@code id_caso_alt} se traduce a
 * {@link FichaDuplicadaException} para alimentar el patrón de idempotencia del listener.</p>
 */
@Repository
public class RepositorioFichasJpa implements RepositorioFichas {

    private final FichaJpaRepository fichaRepo;
    private final VulneracionJpaRepository vulneracionRepo;
    private final AntecedenteJpaRepository antecedenteRepo;
    private final TransicionJpaRepository transicionRepo;

    public RepositorioFichasJpa(FichaJpaRepository fichaRepo, VulneracionJpaRepository vulneracionRepo,
                                AntecedenteJpaRepository antecedenteRepo, TransicionJpaRepository transicionRepo) {
        this.fichaRepo = fichaRepo;
        this.vulneracionRepo = vulneracionRepo;
        this.antecedenteRepo = antecedenteRepo;
        this.transicionRepo = transicionRepo;
    }

    @Override
    public Ficha materializar(Ficha nueva) {
        try {
            FichaEntity entidad = Objects.requireNonNull(MapeadorPersistencia.aEntidad(nueva),
                    "La entidad de ficha es obligatoria");
            FichaEntity guardada = fichaRepo.saveAndFlush(entidad);
            Long idFicha = Objects.requireNonNull(guardada.getId(), "La ficha guardada debe tener id interno");
            nueva.asignarIdInterno(idFicha);
            // Asiento de apertura (génesis): persistir las transiciones que trae el esqueleto.
            for (Transicion t : nueva.getHistorial()) {
                TransicionEntity entidadTransicion = Objects.requireNonNull(MapeadorPersistencia.aEntidad(t, idFicha),
                        "La entidad de transición es obligatoria");
                transicionRepo.save(entidadTransicion);
            }
            return nueva;
        } catch (DataIntegrityViolationException ex) {
            // Carrera entre instancias: unicidad de id_caso_alt (uk_ficha_caso) o numero_ficha.
            throw new FichaDuplicadaException(
                    "Ya existe una FIR para el caso " + nueva.getIdCasoAlt() + " (violación de unicidad).");
        }
    }

    @Override
    public boolean existePorCaso(String idCasoAlt) {
        return fichaRepo.existsByIdCasoAlt(idCasoAlt);
    }

    @Override
    public Optional<Ficha> buscarPorAltKey(String altKey) {
        return fichaRepo.findByAltKey(altKey).map(this::reconstruirCompleta);
    }

    @Override
    public ResultadoPagina<Ficha> listar(CriterioListadoFichas criterio) {
        Pageable pageable = PageRequest.of(
                Math.max(criterio.pagina(), 0),
                criterio.tamano() <= 0 ? 20 : criterio.tamano(),
                Sort.by(Sort.Direction.DESC, "id"));
        Page<FichaEntity> pagina = fichaRepo.findAll(filtro(criterio), pageable);
        List<Ficha> contenido = new ArrayList<>();
        for (FichaEntity e : pagina.getContent()) {
            // Resumen: sin cargar colecciones hijas (más liviano para el listado).
            contenido.add(MapeadorPersistencia.aDominio(e, List.of(), List.of(), List.of()));
        }
        return new ResultadoPagina<>(contenido, pagina.getNumber(), pagina.getSize(), pagina.getTotalElements());
    }

    @Override
    public Vulneracion agregarVulneracion(Ficha ficha, Vulneracion vulneracion) {
        Long idFicha = idInternoObligatorio(ficha);
        VulneracionEntity entidad = Objects.requireNonNull(MapeadorPersistencia.aEntidad(vulneracion, idFicha),
                "La entidad de vulneración es obligatoria");
        VulneracionEntity guardada = vulneracionRepo.save(entidad);
        vulneracion.asignarIdInterno(guardada.getId());
        tocarActualizacion(ficha);
        return vulneracion;
    }

    @Override
    public Antecedente agregarAntecedente(Ficha ficha, Antecedente antecedente) {
        Long idFicha = idInternoObligatorio(ficha);
        AntecedenteEntity entidad = Objects.requireNonNull(MapeadorPersistencia.aEntidad(antecedente, idFicha),
                "La entidad de antecedente es obligatoria");
        AntecedenteEntity guardado = antecedenteRepo.save(entidad);
        antecedente.asignarIdInterno(guardado.getId());
        tocarActualizacion(ficha);
        return antecedente;
    }

    @Override
    public Transicion aplicarTransicion(Ficha ficha, Transicion transicion) {
        Long idFicha = idInternoObligatorio(ficha);
        TransicionEntity entidadTransicion = Objects.requireNonNull(MapeadorPersistencia.aEntidad(transicion, idFicha),
                "La entidad de transición es obligatoria");
        transicionRepo.save(entidadTransicion);
        FichaEntity entidad = fichaRepo.getReferenceById(idFicha);
        entidad.setEstado(ficha.getEstado().name());
        entidad.setCerradaEn(MapeadorPersistencia.aLocal(ficha.getCerradaEn()));
        entidad.setActualizadaEn(MapeadorPersistencia.aLocal(ficha.getActualizadaEn()));
        fichaRepo.save(entidad);
        return transicion;
    }

    // ------------------------------------------------------------------ apoyo

    /** Refresca solo la marca de actualización de la ficha (alta de hijas). */
    private void tocarActualizacion(Ficha ficha) {
        FichaEntity entidad = fichaRepo.getReferenceById(idInternoObligatorio(ficha));
        entidad.setActualizadaEn(MapeadorPersistencia.aLocal(ficha.getActualizadaEn()));
        fichaRepo.save(entidad);
    }

    @NonNull
    private Long idInternoObligatorio(Ficha ficha) {
        return Objects.requireNonNull(ficha.getIdInterno(), "La ficha debe tener id interno asignado");
    }

    /** Reconstruye el agregado completo (ficha + hijas + historial). */
    private Ficha reconstruirCompleta(FichaEntity e) {
        Long idFicha = Objects.requireNonNull(e.getId(), "La entidad de ficha debe tener id interno");
        List<Vulneracion> vulneraciones = new ArrayList<>();
        for (VulneracionEntity v : vulneracionRepo.findByIdFichaOrderByRegistradoEnAscIdAsc(idFicha)) {
            vulneraciones.add(MapeadorPersistencia.aDominio(v));
        }
        List<Antecedente> antecedentes = new ArrayList<>();
        for (AntecedenteEntity a : antecedenteRepo.findByIdFichaOrderByRegistradoEnAscIdAsc(idFicha)) {
            antecedentes.add(MapeadorPersistencia.aDominio(a));
        }
        List<Transicion> historial = new ArrayList<>();
        for (TransicionEntity t : transicionRepo.findByIdFichaOrderByOcurridoEnAscIdAsc(idFicha)) {
            historial.add(MapeadorPersistencia.aDominio(t));
        }
        return MapeadorPersistencia.aDominio(e, vulneraciones, antecedentes, historial);
    }

    /** Filtro territorial + filtros opcionales del listado, como {@link Specification} componible. */
    private Specification<FichaEntity> filtro(CriterioListadoFichas criterio) {
        return (root, query, cb) -> {
            List<Predicate> predicados = new ArrayList<>();
            predicados.add(cb.isTrue(root.get("vigente")));
            switch (criterio.alcance()) {
                case NACIONAL -> { /* sin filtro territorial */ }
                case SEDE -> predicados.add(cb.equal(root.get("idSedeAlt"), criterio.idSedeUsuario()));
                case UNIDAD -> predicados.add(cb.equal(root.get("idUnidadAlt"), criterio.idUnidadUsuario()));
            }
            if (criterio.idCasoAlt() != null && !criterio.idCasoAlt().isBlank()) {
                predicados.add(cb.equal(root.get("idCasoAlt"), criterio.idCasoAlt()));
            }
            if (criterio.estado() != null) {
                predicados.add(cb.equal(root.get("estado"), criterio.estado().name()));
            }
            return cb.and(predicados.toArray(new Predicate[0]));
        };
    }
}
