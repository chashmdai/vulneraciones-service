package cl.smid.vulneraciones.dominio.puerto.salida;

import java.util.Optional;

/**
 * Puerto de salida (costura on-demand, opcional): resolución del nombre legible de un NNA contra
 * personas-service (6.2). Best-effort: degrada a vacío ante cualquier fallo o si está desactivado.
 */
public interface ClientePersonas {

    /**
     * @param idNnaAlt {@code alt_key} de la persona NNA en Personas
     * @return el nombre legible si pudo resolverse; {@link Optional#empty()} en caso contrario
     */
    Optional<String> nombreLegible(String idNnaAlt);
}
