package cl.smid.vulneraciones.infraestructura.persistencia.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad JPA de una vulneración hija de la FIR (tabla {@code ficha_vulneracion}).
 *
 * <p>La relación con la ficha se modela por la columna escalar {@code id_ficha} (sin asociación JPA),
 * para evitar carga perezosa y mantener el control explícito del agregado en el adaptador. El campo
 * {@code relato_cifrado} guarda la representación en reposo del relato reservado; nunca se indexa ni
 * se registra en logs.</p>
 */
@Entity
@Table(name = "ficha_vulneracion")
@Getter
@Setter
@NoArgsConstructor
public class VulneracionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "alt_key", nullable = false, length = 36, unique = true, updatable = false)
    private String altKey;

    @Column(name = "id_ficha", nullable = false, updatable = false)
    private Long idFicha;

    @Column(name = "id_derecho_alt", nullable = false, length = 36)
    private String idDerechoAlt;

    @Column(name = "id_causa_alt", length = 36)
    private String idCausaAlt;

    @Column(name = "id_nna_alt", nullable = false, length = 36)
    private String idNnaAlt;

    @Column(name = "nna_nombre_legible", length = 200)
    private String nnaNombreLegible;

    @Column(name = "gravedad", nullable = false, length = 12)
    private String gravedad;

    @Column(name = "relato_cifrado", length = 8000)
    private String relatoCifrado;

    @Column(name = "fecha_hecho")
    private LocalDate fechaHecho;

    @Column(name = "registrado_en", nullable = false, updatable = false)
    private LocalDateTime registradoEn;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;

    @Column(name = "registrado_por", length = 36, updatable = false)
    private String registradoPor;
}
