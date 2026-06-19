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

import java.time.LocalDateTime;

/**
 * Entidad JPA del agregado FIR (tabla {@code ficha}).
 *
 * <p>Adaptador de persistencia: vive en el borde, usa Lombok y JPA. La llave numérica {@code id} es
 * interna y nunca se expone; públicamente la ficha se identifica por {@code alt_key}. Los enumerados
 * se mapean como {@code VARCHAR} (cadena), las marcas temporales como {@code DATETIME(6)} en UTC
 * ({@link LocalDateTime} en hora mural ya normalizada) y los booleanos como {@code TINYINT(1)}.</p>
 */
@Entity
@Table(name = "ficha")
@Getter
@Setter
@NoArgsConstructor
public class FichaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "alt_key", nullable = false, length = 36, unique = true, updatable = false)
    private String altKey;

    @Column(name = "numero_ficha", nullable = false, length = 32, unique = true, updatable = false)
    private String numeroFicha;

    @Column(name = "codigo_sede", nullable = false, length = 8, updatable = false)
    private String codigoSede;

    @Column(name = "serie", nullable = false, length = 8, updatable = false)
    private String serie;

    @Column(name = "correlativo", nullable = false, updatable = false)
    private long correlativo;

    @Column(name = "anio", nullable = false, updatable = false)
    private int anio;

    @Column(name = "id_caso_alt", nullable = false, length = 36, unique = true, updatable = false)
    private String idCasoAlt;

    @Column(name = "numero_expediente", nullable = false, length = 32)
    private String numeroExpediente;

    @Column(name = "id_sede_alt", nullable = false, length = 36)
    private String idSedeAlt;

    @Column(name = "id_unidad_alt", length = 36)
    private String idUnidadAlt;

    @Column(name = "complejidad", length = 12)
    private String complejidad;

    @Column(name = "es_beta", nullable = false)
    private boolean esBeta;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "abierta_en", nullable = false)
    private LocalDateTime abiertaEn;

    @Column(name = "cerrada_en")
    private LocalDateTime cerradaEn;

    @Column(name = "creada_en", nullable = false, updatable = false)
    private LocalDateTime creadaEn;

    @Column(name = "actualizada_en", nullable = false)
    private LocalDateTime actualizadaEn;

    @Column(name = "creada_por", length = 36, updatable = false)
    private String creadaPor;

    @Column(name = "vigente", nullable = false)
    private boolean vigente;
}
