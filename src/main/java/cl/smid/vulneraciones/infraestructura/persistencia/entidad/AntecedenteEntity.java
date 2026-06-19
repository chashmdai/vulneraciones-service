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
 * Entidad JPA de un antecedente hijo de la FIR (tabla {@code ficha_antecedente}).
 *
 * <p>La relación con la ficha se modela por la columna escalar {@code id_ficha}. El campo
 * {@code descripcion_cifrada} guarda la representación en reposo de la descripción reservada; nunca
 * se indexa ni se registra en logs.</p>
 */
@Entity
@Table(name = "ficha_antecedente")
@Getter
@Setter
@NoArgsConstructor
public class AntecedenteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "alt_key", nullable = false, length = 36, unique = true, updatable = false)
    private String altKey;

    @Column(name = "id_ficha", nullable = false, updatable = false)
    private Long idFicha;

    @Column(name = "tipo", nullable = false, length = 24)
    private String tipo;

    @Column(name = "descripcion_cifrada", length = 8000)
    private String descripcionCifrada;

    @Column(name = "fecha")
    private LocalDate fecha;

    @Column(name = "fuente", length = 160)
    private String fuente;

    @Column(name = "registrado_en", nullable = false, updatable = false)
    private LocalDateTime registradoEn;

    @Column(name = "registrado_por", length = 36, updatable = false)
    private String registradoPor;
}
