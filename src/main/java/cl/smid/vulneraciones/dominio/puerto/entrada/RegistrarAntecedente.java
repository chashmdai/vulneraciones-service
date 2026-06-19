package cl.smid.vulneraciones.dominio.puerto.entrada;

import cl.smid.vulneraciones.dominio.modelo.ContextoUsuario;
import cl.smid.vulneraciones.dominio.modelo.vista.VistaAntecedente;
import cl.smid.vulneraciones.dominio.puerto.entrada.comando.ComandoRegistrarAntecedente;

/**
 * Caso de uso: registro de un antecedente dentro de una ficha mutable.
 */
public interface RegistrarAntecedente {

    /**
     * Registra un antecedente (cifra la descripción en reposo).
     *
     * @param comando datos del antecedente
     * @param ctx     contexto de sesión
     * @return la vista del antecedente creado (descripción redactada según rol)
     * @throws cl.smid.vulneraciones.dominio.excepcion.RecursoNoEncontradoException si la ficha no
     *         existe o está fuera de alcance (VUL-404)
     * @throws cl.smid.vulneraciones.dominio.excepcion.FichaNoMutableException si la ficha no es mutable
     *         (VUL-409)
     * @throws cl.smid.vulneraciones.dominio.excepcion.ReglaNegocioException si faltan datos obligatorios
     *         (VUL-422)
     */
    VistaAntecedente registrar(ComandoRegistrarAntecedente comando, ContextoUsuario ctx);
}
