package cl.smid.vulneraciones.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Roles habilitantes del servicio ({@code smid.seguridad.*}).
 *
 * @param rolesCoordinacion     roles que pueden ejecutar acciones administrativas (CERRAR/REABRIR)
 * @param rolesAccesoReservado  roles que pueden ver el contenido reservado en claro
 */
@ConfigurationProperties(prefix = "smid.seguridad")
public record PropiedadesSeguridad(
        List<String> rolesCoordinacion,
        List<String> rolesAccesoReservado) {
}
