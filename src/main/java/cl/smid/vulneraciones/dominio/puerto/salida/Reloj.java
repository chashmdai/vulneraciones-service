package cl.smid.vulneraciones.dominio.puerto.salida;

import java.time.Instant;

/**
 * Puerto de salida: reloj del sistema. Aísla al dominio del acceso directo a la hora, permitiendo
 * relojes fijos en pruebas. Todas las marcas temporales se manejan en UTC.
 */
public interface Reloj {

    /** @return el instante actual (UTC). */
    Instant ahora();
}
