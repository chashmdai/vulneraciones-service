package cl.smid.vulneraciones.soporte;

import cl.smid.vulneraciones.dominio.puerto.salida.Reloj;

import java.time.Instant;

/** Doble del reloj: instante fijo y reproducible. */
public class RelojFijo implements Reloj {

    private Instant instante;

    public RelojFijo(Instant instante) {
        this.instante = instante;
    }

    public void fijar(Instant instante) {
        this.instante = instante;
    }

    @Override
    public Instant ahora() {
        return instante;
    }
}
