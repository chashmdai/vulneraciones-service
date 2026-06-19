package cl.smid.vulneraciones.soporte;

import cl.smid.vulneraciones.dominio.puerto.salida.EventoDominio;
import cl.smid.vulneraciones.dominio.puerto.salida.PublicadorEventos;

import java.util.ArrayList;
import java.util.List;

/** Doble del publicador: captura los eventos emitidos para verificarlos en las pruebas. */
public class PublicadorEventosCapturador implements PublicadorEventos {

    private final List<EventoDominio> eventos = new ArrayList<>();

    @Override
    public void publicar(EventoDominio evento) {
        eventos.add(evento);
    }

    public List<EventoDominio> eventos() {
        return eventos;
    }

    public long contarPorTipo(String tipo) {
        return eventos.stream().filter(e -> tipo.equals(e.tipo())).count();
    }
}
