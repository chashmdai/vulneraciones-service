package cl.smid.vulneraciones.dominio.puerto.salida;

/**
 * Puerto de salida: resolución del código corto de una sede a partir de su {@code alt_key}.
 *
 * <p>El token solo transporta el {@code alt_key} de la sede; el código corto (p. ej. {@code RM}) que
 * compone el número de FIR se resuelve por configuración a través de este puerto.</p>
 */
public interface DirectorioSedes {

    /**
     * @param idSedeAlt {@code alt_key} de la sede
     * @return el código corto configurado, o un código de respaldo si la sede no está mapeada
     */
    String codigoDe(String idSedeAlt);
}
