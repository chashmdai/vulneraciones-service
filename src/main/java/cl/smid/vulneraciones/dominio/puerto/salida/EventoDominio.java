package cl.smid.vulneraciones.dominio.puerto.salida;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Sobre de evento de dominio emitido por el servicio (metadata-only, G7).
 *
 * <p>La carga útil contiene exclusivamente metadatos no sensibles: jamás relatos, nombres ni RUT.</p>
 *
 * @param tipo       tipo del evento (también es la routing key)
 * @param altKey     identificador opaco del recurso afectado
 * @param ocurridoEn instante del hecho (UTC)
 * @param metadatos  metadatos no sensibles (mapa inmutable)
 */
public record EventoDominio(String tipo, String altKey, Instant ocurridoEn, Map<String, Object> metadatos) {

    public EventoDominio {
        Map<String, Object> copia = new LinkedHashMap<>();
        if (metadatos != null) {
            copia.putAll(metadatos);
        }
        metadatos = Collections.unmodifiableMap(copia);
    }

    /**
     * Factoría de conveniencia.
     *
     * @param tipo       tipo/routing key
     * @param altKey     identificador opaco del recurso
     * @param ocurridoEn instante (UTC)
     * @param metadatos  metadatos no sensibles
     * @return el sobre de evento
     */
    public static EventoDominio de(String tipo, String altKey, Instant ocurridoEn, Map<String, Object> metadatos) {
        return new EventoDominio(tipo, altKey, ocurridoEn, metadatos);
    }
}
