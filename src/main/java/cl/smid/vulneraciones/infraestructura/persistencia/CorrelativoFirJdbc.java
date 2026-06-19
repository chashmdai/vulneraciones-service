package cl.smid.vulneraciones.infraestructura.persistencia;

import cl.smid.vulneraciones.dominio.modelo.SerieCorrelativo;
import cl.smid.vulneraciones.dominio.puerto.salida.CorrelativoFirPort;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Adaptador de salida del puerto {@link CorrelativoFirPort}: reserva atómica del correlativo de FIR.
 *
 * <p>Usa el modismo de MySQL {@code INSERT ... ON DUPLICATE KEY UPDATE ... LAST_INSERT_ID(...)}, que
 * incrementa el contador y, en la misma sentencia, fija el nuevo valor en {@code LAST_INSERT_ID()}.
 * Ambas operaciones (el UPSERT y el {@code SELECT LAST_INSERT_ID()}) se ejecutan sobre <strong>la
 * misma conexión física</strong> mediante un {@link ConnectionCallback}; como corre dentro de la
 * transacción demarcada en el listener, un rollback no deja huecos. La serie BETA está aislada de la
 * OFICIAL en filas distintas de la clave primaria compuesta {@code (id_sede_alt, anio, serie)}.</p>
 *
 * <p>El bloqueo de fila que aplica InnoDB sobre la fila del contador durante el UPSERT serializa a los
 * hilos concurrentes que compiten por la misma combinación, garantizando valores únicos y
 * consecutivos ({@code 1..N}).</p>
 */
@Repository
public class CorrelativoFirJdbc implements CorrelativoFirPort {

    private static final String UPSERT = """
            INSERT INTO correlativo_fir (id_sede_alt, anio, serie, ultimo)
            VALUES (?, ?, ?, LAST_INSERT_ID(1))
            ON DUPLICATE KEY UPDATE ultimo = LAST_INSERT_ID(ultimo + 1)
            """;

    private final JdbcTemplate jdbcTemplate;

    public CorrelativoFirJdbc(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public long reservar(String idSedeAlt, int anio, SerieCorrelativo serie) {
        Long reservado = jdbcTemplate.execute((ConnectionCallback<Long>) conexion -> {
            try (PreparedStatement ps = conexion.prepareStatement(UPSERT)) {
                ps.setString(1, idSedeAlt);
                ps.setInt(2, anio);
                ps.setString(3, serie.name());
                ps.executeUpdate();
            }
            try (Statement st = conexion.createStatement();
                 ResultSet rs = st.executeQuery("SELECT LAST_INSERT_ID()")) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                throw new IllegalStateException(
                        "No se pudo recuperar el correlativo reservado (LAST_INSERT_ID vacío).");
            }
        });
        if (reservado == null || reservado <= 0) {
            throw new IllegalStateException("La reserva de correlativo devolvió un valor inválido.");
        }
        return reservado;
    }
}
