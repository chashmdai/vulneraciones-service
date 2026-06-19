package cl.smid.vulneraciones.infraestructura.persistencia.repositorio;

import cl.smid.vulneraciones.infraestructura.persistencia.entidad.TransicionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio Spring Data del historial de transiciones de la FIR.
 */
public interface TransicionJpaRepository extends JpaRepository<TransicionEntity, Long> {

    /**
     * @param idFicha llave interna de la ficha
     * @return el historial de la ficha, ordenado cronológicamente
     */
    List<TransicionEntity> findByIdFichaOrderByOcurridoEnAscIdAsc(Long idFicha);
}
