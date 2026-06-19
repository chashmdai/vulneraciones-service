package cl.smid.vulneraciones;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Punto de arranque del microservicio de Vulneraciones (SMID 6.5) — Ficha Interna Reservada (FIR).
 *
 * <p>{@link ConfigurationPropertiesScan} habilita el enlace de todas las clases
 * {@code @ConfigurationProperties} del paquete base (configuración de JWT, seguridad, FIR, sedes,
 * enriquecimiento, reservado y eventos).</p>
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class VulneracionesApplication {

    public static void main(String[] args) {
        SpringApplication.run(VulneracionesApplication.class, args);
    }
}
