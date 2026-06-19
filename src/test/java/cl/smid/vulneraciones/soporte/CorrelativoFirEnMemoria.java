package cl.smid.vulneraciones.soporte;

import cl.smid.vulneraciones.dominio.modelo.SerieCorrelativo;
import cl.smid.vulneraciones.dominio.puerto.salida.CorrelativoFirPort;

import java.util.HashMap;
import java.util.Map;

/** Doble en memoria del contador de FIR: incrementa por clave (sede, año, serie). */
public class CorrelativoFirEnMemoria implements CorrelativoFirPort {

    private final Map<String, Long> contadores = new HashMap<>();

    @Override
    public synchronized long reservar(String idSedeAlt, int anio, SerieCorrelativo serie) {
        String clave = idSedeAlt + "|" + anio + "|" + serie.name();
        long siguiente = contadores.getOrDefault(clave, 0L) + 1;
        contadores.put(clave, siguiente);
        return siguiente;
    }
}
