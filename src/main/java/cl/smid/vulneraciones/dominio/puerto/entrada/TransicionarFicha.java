package cl.smid.vulneraciones.dominio.puerto.entrada;

import cl.smid.vulneraciones.dominio.modelo.ContextoUsuario;
import cl.smid.vulneraciones.dominio.modelo.vista.VistaFicha;
import cl.smid.vulneraciones.dominio.puerto.entrada.comando.ComandoTransicion;

/**
 * Caso de uso: transición administrativa de una ficha (CERRAR/REABRIR). Exige rol de Coordinación.
 */
public interface TransicionarFicha {

    /**
     * Aplica una transición administrativa.
     *
     * @param comando acción y observación
     * @param ctx     contexto de sesión
     * @return el detalle actualizado de la ficha (reservado redactado según rol)
     * @throws cl.smid.vulneraciones.dominio.excepcion.RecursoNoEncontradoException si la ficha no
     *         existe o está fuera de alcance (VUL-404)
     * @throws cl.smid.vulneraciones.dominio.excepcion.AccesoDenegadoException si falta rol de
     *         Coordinación (AUTZ-004)
     * @throws cl.smid.vulneraciones.dominio.excepcion.TransicionInvalidaException si la transición no
     *         es válida (VUL-409)
     */
    VistaFicha transicionar(ComandoTransicion comando, ContextoUsuario ctx);
}
