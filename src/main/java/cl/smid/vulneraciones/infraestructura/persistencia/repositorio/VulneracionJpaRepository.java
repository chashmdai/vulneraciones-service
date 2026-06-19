package cl.smid.vulneraciones.infraestructura.persistencia.repositorio;

import cl.smid.vulneraciones.infraestructura.persistencia.entidad.VulneracionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio Spring Data de vulneraciones (hijas de la FIR).
 */
public interface VulneracionJpaRepository extends JpaRepository<VulneracionEntity, Long> {

    /**
     * @param idFicha llave interna de la ficha
     * @return las vulneraciones de la ficha, ordenadas por registro ascendente
     */
    List<VulneracionEntity> findByIdFichaOrderByRegistradoEnAscIdAsc(Long idFicha);
}
