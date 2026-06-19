package cl.smid.vulneraciones.infraestructura.persistencia;

import cl.smid.vulneraciones.dominio.modelo.SerieCorrelativo;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.MySQLContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueba de integración de la reserva atómica de correlativos bajo concurrencia real (MySQL en
 * contenedor). Verifica que N hilos compitiendo por la misma combinación {@code (sede, año, serie)}
 * obtienen valores únicos y contiguos {@code 1..N}, y que la serie BETA está aislada de la OFICIAL.
 *
 * <p>Requiere Docker; si no está disponible, la prueba se omite (no falla) mediante {@code Assumptions}.</p>
 */
@DisplayName("CorrelativoFirJdbc (IT): reserva atómica bajo concurrencia y aislamiento de series")
class CorrelativoFirConcurrenciaIT {

    private static MySQLContainer<?> mysql;
    private static HikariDataSource dataSource;
    private static CorrelativoFirJdbc adaptador;

    @BeforeAll
    static void iniciar() {
        Assumptions.assumeTrue(DockerClientFactory.instance().isDockerAvailable(),
                "Docker no disponible; se omite la IT de concurrencia del correlativo.");

        mysql = crearMysql();
        mysql.start();

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(mysql.getJdbcUrl() + "?tinyInt1isBit=true&serverTimezone=UTC");
        config.setUsername(mysql.getUsername());
        config.setPassword(mysql.getPassword());
        config.setMaximumPoolSize(32);
        dataSource = new HikariDataSource(config);

        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("""
                CREATE TABLE correlativo_fir (
                    id_sede_alt VARCHAR(36) NOT NULL,
                    anio        INT         NOT NULL,
                    serie       VARCHAR(8)  NOT NULL,
                    ultimo      BIGINT      NOT NULL,
                    PRIMARY KEY (id_sede_alt, anio, serie)
                ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci
                """);
        adaptador = new CorrelativoFirJdbc(jdbc);
    }

    @SuppressWarnings("resource")
    private static MySQLContainer<?> crearMysql() {
        return new MySQLContainer<>("mysql:8.0").withDatabaseName("db_vulneraciones");
    }

    @AfterAll
    static void cerrar() {
        if (dataSource != null) {
            dataSource.close();
        }
        if (mysql != null) {
            mysql.stop();
        }
    }

    @Test
    @DisplayName("N reservas concurrentes sobre la misma serie ⇒ valores únicos y contiguos 1..N")
    void reservasConcurrentesUnicasYContiguas() throws Exception {
        int hilos = 16;
        ExecutorService pool = Executors.newFixedThreadPool(hilos);
        CountDownLatch arranque = new CountDownLatch(1);
        List<Future<Long>> futuros = new ArrayList<>();

        for (int i = 0; i < hilos; i++) {
            futuros.add(pool.submit(() -> {
                arranque.await();
                return adaptador.reservar("sede-rm", 2027, SerieCorrelativo.OFICIAL);
            }));
        }
        arranque.countDown(); // libera a todos los hilos a la vez

        TreeSet<Long> valores = new TreeSet<>();
        for (Future<Long> futuro : futuros) {
            valores.add(futuro.get());
        }
        pool.shutdown();

        assertThat(valores).hasSize(hilos);
        assertThat(valores).containsExactlyElementsOf(
                LongStream.rangeClosed(1, hilos).boxed().toList());
    }

    @Test
    @DisplayName("La serie BETA está aislada de la OFICIAL (cada una arranca en 1)")
    void seriesAisladas() {
        long oficial1 = adaptador.reservar("sede-aislada", 2027, SerieCorrelativo.OFICIAL);
        long oficial2 = adaptador.reservar("sede-aislada", 2027, SerieCorrelativo.OFICIAL);
        long beta1 = adaptador.reservar("sede-aislada", 2027, SerieCorrelativo.BETA);
        long beta2 = adaptador.reservar("sede-aislada", 2027, SerieCorrelativo.BETA);

        assertThat(oficial1).isEqualTo(1);
        assertThat(oficial2).isEqualTo(2);
        assertThat(beta1).isEqualTo(1);
        assertThat(beta2).isEqualTo(2);
    }
}
