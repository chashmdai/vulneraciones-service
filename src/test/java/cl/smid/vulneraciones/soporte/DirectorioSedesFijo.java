package cl.smid.vulneraciones.soporte;

import cl.smid.vulneraciones.dominio.puerto.salida.DirectorioSedes;

/** Doble del directorio de sedes: devuelve siempre el mismo código. */
public class DirectorioSedesFijo implements DirectorioSedes {

    private final String codigo;

    public DirectorioSedesFijo(String codigo) {
        this.codigo = codigo;
    }

    @Override
    public String codigoDe(String idSedeAlt) {
        return codigo;
    }
}
