package cl.smid.vulneraciones.dominio.puerto.entrada;

import cl.smid.vulneraciones.dominio.modelo.ContextoUsuario;
import cl.smid.vulneraciones.dominio.modelo.vista.ContenidoReservado;

/**
 * Caso de uso: lectura del contenido reservado de una ficha (relatos y descripciones en claro).
 * Exige rol de acceso reservado; en su ausencia responde AUTZ-004 (403).
 */
public interface ConsultarContenidoReservado {

    /**
     * Devuelve el contenido reservado en claro de la ficha.
     *
     * @param altKey identificador opaco de la ficha
     * @param ctx    contexto de sesión
     * @return el contenido reservado
     * @throws cl.smid.vulneraciones.dominio.excepcion.RecursoNoEncontradoException si la ficha no
     *         existe o está fuera de alcance (VUL-404)
     * @throws cl.smid.vulneraciones.dominio.excepcion.AccesoDenegadoException si falta rol de acceso
     *         reservado (AUTZ-004)
     */
    ContenidoReservado obtener(String altKey, ContextoUsuario ctx);
}
