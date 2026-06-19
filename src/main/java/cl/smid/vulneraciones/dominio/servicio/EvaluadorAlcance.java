package cl.smid.vulneraciones.dominio.servicio;

import cl.smid.vulneraciones.dominio.modelo.ContextoUsuario;

import java.util.Objects;

/**
 * Evalúa el acceso territorial registro a registro según el {@code alcance} del usuario.
 *
 * <ul>
 *   <li>{@code NACIONAL}: acceso a todo.</li>
 *   <li>{@code SEDE}: el registro debe pertenecer a la sede del usuario.</li>
 *   <li>{@code UNIDAD}: el registro debe pertenecer a la unidad del usuario.</li>
 * </ul>
 *
 * <p>La denegación se traduce en 404 (no 403) en la capa de aplicación, para no revelar la
 * existencia de registros fuera de alcance. POJO puro.</p>
 */
public class EvaluadorAlcance {

    /**
     * Indica si el usuario puede acceder a un registro dado su emplazamiento territorial.
     *
     * @param contexto       contexto de sesión (alcance y emplazamiento del usuario)
     * @param idSedeRegistro {@code alt_key} de la sede del registro
     * @param idUnidadRegistro {@code alt_key} de la unidad del registro (puede ser nulo)
     * @return {@code true} si el registro cae dentro del alcance del usuario
     */
    public boolean puedeAcceder(ContextoUsuario contexto, String idSedeRegistro, String idUnidadRegistro) {
        Objects.requireNonNull(contexto, "contexto es obligatorio");
        return switch (contexto.alcance()) {
            case NACIONAL -> true;
            case SEDE -> iguales(contexto.idSede(), idSedeRegistro);
            case UNIDAD -> iguales(contexto.idUnidad(), idUnidadRegistro);
        };
    }

    private boolean iguales(String a, String b) {
        return a != null && a.equals(b);
    }
}
