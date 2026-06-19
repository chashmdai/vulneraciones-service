package cl.smid.vulneraciones.infraestructura.reservado;

import cl.smid.vulneraciones.dominio.puerto.salida.ProtectorDatosReservados;

/**
 * Adaptador de paso directo (passthrough): guarda y devuelve el texto sin cifrar.
 *
 * <p><strong>NO apto para producción.</strong> Existe solo para desarrollo y pruebas (cuando
 * {@code smid.reservado.cifrado=none}). En este modo los datos reservados quedan en claro en la base
 * de datos; úsese exclusivamente con datos sintéticos.</p>
 */
public class ProtectorPassthrough implements ProtectorDatosReservados {

    @Override
    public String cifrar(String claro) {
        return claro;
    }

    @Override
    public String descifrar(String almacenado) {
        return almacenado;
    }
}
