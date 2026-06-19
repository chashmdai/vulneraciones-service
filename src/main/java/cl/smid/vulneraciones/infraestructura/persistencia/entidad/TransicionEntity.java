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
 * Entidad JPA del historial de la máquina de estados de la FIR (tabla {@code ficha_transicion}).
 *
 * <p>Append-only. El asiento de apertura usa {@code estado_origen = null}, {@code accion =
 * MATERIALIZACION} y el actor de sistema.</p>
 */
@Entity
@Table(name = "ficha_transicion")
@Getter
@Setter
@NoArgsConstructor
public class TransicionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "alt_key", nullable = false, length = 36, unique = true, updatable = false)
    private String altKey;

    @Column(name = "id_ficha", nullable = false, updatable = false)
    private Long idFicha;

    @Column(name = "estado_origen", length = 20)
    private String estadoOrigen;

    @Column(name = "estado_destino", nullable = false, length = 20)
    private String estadoDestino;

    @Column(name = "accion", nullable = false, length = 32)
    private String accion;

    @Column(name = "observacion", length = 2000)
    private String observacion;

    @Column(name = "actor_alt", length = 36)
    private String actorAlt;

    @Column(name = "ocurrido_en", nullable = false, updatable = false)
    private LocalDateTime ocurridoEn;
}
