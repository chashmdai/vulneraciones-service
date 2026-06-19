package cl.smid.vulneraciones.infraestructura.persistencia.repositorio;

import cl.smid.vulneraciones.infraestructura.persistencia.entidad.FichaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * Repositorio Spring Data del agregado FIR.
 *
 * <p>Extiende {@link JpaSpecificationExecutor} para construir el filtro territorial y los filtros
 * opcionales del listado de forma componible.</p>
 */
public interface FichaJpaRepository
        extends JpaRepository<FichaEntity, Long>, JpaSpecificationExecutor<FichaEntity> {

    /**
     * @param altKey identificador opaco
     * @return la ficha si existe
     */
    Optional<FichaEntity> findByAltKey(String altKey);

    /**
     * @param idCasoAlt {@code alt_key} del caso
     * @return {@code true} si ya existe una ficha para ese caso (pre-chequeo de idempotencia)
     */
    boolean existsByIdCasoAlt(String idCasoAlt);
}
