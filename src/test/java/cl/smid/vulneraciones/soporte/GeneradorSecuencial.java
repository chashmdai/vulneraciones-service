package cl.smid.vulneraciones.soporte;

import cl.smid.vulneraciones.dominio.puerto.salida.GeneradorIdentificadores;

import java.util.concurrent.atomic.AtomicLong;

/** Doble del generador de identificadores: UUIDs deterministas y reproducibles. */
public class GeneradorSecuencial implements GeneradorIdentificadores {

    private final AtomicLong secuencia = new AtomicLong(0);

    @Override
    public String nuevoUuid() {
        long n = secuencia.incrementAndGet();
        return String.format("00000000-0000-0000-0000-%012d", n);
    }
}
