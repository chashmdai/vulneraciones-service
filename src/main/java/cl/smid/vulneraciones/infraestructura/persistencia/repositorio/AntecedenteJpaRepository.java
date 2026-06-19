package cl.smid.vulneraciones.infraestructura.persistencia.repositorio;

import cl.smid.vulneraciones.infraestructura.persistencia.entidad.AntecedenteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio Spring Data de antecedentes (hijos de la FIR).
 */
public interface AntecedenteJpaRepository extends JpaRepository<AntecedenteEntity, Long> {

    /**
     * @param idFicha llave interna de la ficha
     * @return los antecedentes de la ficha, ordenados por registro ascendente
     */
    List<AntecedenteEntity> findByIdFichaOrderByRegistradoEnAscIdAsc(Long idFicha);
}
