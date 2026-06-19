package cl.smid.vulneraciones.dominio.modelo;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Contexto de sesión corporativa: proyección inmutable de los claims del token relevantes para la
 * autorización territorial y por rol.
 *
 * <p>Es un objeto de valor puro. Los roles se normalizan a mayúsculas para comparar de forma
 * robusta contra las listas de configuración.</p>
 *
 * @param sub     identificador opaco ({@code alt_key}) del usuario autor
 * @param roles   conjunto de roles del usuario (normalizados a mayúsculas)
 * @param idSede  {@code alt_key} de la sede del usuario
 * @param idUnidad {@code alt_key} de la unidad del usuario
 * @param alcance radio territorial del usuario
 * @param nombre  nombre legible del usuario (solo para trazas/contexto, no es PII sensible)
 */
public record ContextoUsuario(
        String sub,
        Set<String> roles,
        String idSede,
        String idUnidad,
        Alcance alcance,
        String nombre) {

    public ContextoUsuario {
        Set<String> normalizados = new LinkedHashSet<>();
        if (roles != null) {
            for (String rol : roles) {
                if (rol != null && !rol.isBlank()) {
                    normalizados.add(rol.trim().toUpperCase());
                }
            }
        }
        roles = Set.copyOf(normalizados);
        alcance = alcance == null ? Alcance.UNIDAD : alcance;
    }

    /**
     * Indica si el usuario posee al menos uno de los roles requeridos.
     *
     * @param requeridos colección de roles habilitantes (se comparan en mayúsculas)
     * @return {@code true} si hay intersección no vacía
     */
    public boolean tieneAlgunRol(Collection<String> requeridos) {
        if (requeridos == null || requeridos.isEmpty()) {
            return false;
        }
        for (String requerido : requeridos) {
            if (requerido != null && roles.contains(requerido.trim().toUpperCase())) {
                return true;
            }
        }
        return false;
    }
}
