package cl.smid.vulneraciones.dominio.puerto.entrada;

import cl.smid.vulneraciones.dominio.modelo.ContextoUsuario;
import cl.smid.vulneraciones.dominio.modelo.vista.VistaVulneracion;
import cl.smid.vulneraciones.dominio.puerto.entrada.comando.ComandoRegistrarVulneracion;

/**
 * Caso de uso: registro de una vulneración dentro de una ficha mutable.
 */
public interface RegistrarVulneracion {

    /**
     * Registra una vulneración (cifra el relato en reposo y resuelve el snapshot del NNA best-effort).
     *
     * @param comando datos de la vulneración
     * @param ctx     contexto de sesión
     * @return la vista de la vulneración creada (relato redactado según rol)
     * @throws cl.smid.vulneraciones.dominio.excepcion.RecursoNoEncontradoException si la ficha no
     *         existe o está fuera de alcance (VUL-404)
     * @throws cl.smid.vulneraciones.dominio.excepcion.FichaNoMutableException si la ficha no es mutable
     *         (VUL-409)
     * @throws cl.smid.vulneraciones.dominio.excepcion.ReglaNegocioException si faltan datos obligatorios
     *         (VUL-422)
     */
    VistaVulneracion registrar(ComandoRegistrarVulneracion comando, ContextoUsuario ctx);
}
