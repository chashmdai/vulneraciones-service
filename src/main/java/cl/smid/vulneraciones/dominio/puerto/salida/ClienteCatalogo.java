package cl.smid.vulneraciones.dominio.puerto.salida;

import java.util.Optional;

/**
 * Puerto de salida (costura on-demand, opcional): etiquetado de derechos y causas contra
 * catalogo-service (6.7). Best-effort: degrada a vacío ante cualquier fallo o si está desactivado.
 */
public interface ClienteCatalogo {

    /**
     * @param idDerechoAlt {@code alt_key} del derecho en Catálogo
     * @return etiqueta legible del derecho (código/nombre) si pudo resolverse; vacío si no
     */
    Optional<String> etiquetaDerecho(String idDerechoAlt);

    /**
     * @param idCausaAlt {@code alt_key} de la causa en Catálogo (puede ser nulo)
     * @return etiqueta legible de la causa si pudo resolverse; vacío si no
     */
    Optional<String> etiquetaCausa(String idCausaAlt);
}
