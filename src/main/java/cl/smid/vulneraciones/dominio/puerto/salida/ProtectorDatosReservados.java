package cl.smid.vulneraciones.dominio.puerto.salida;

/**
 * Puerto de salida (DT-1): protección en reposo de los datos reservados (relatos y descripciones).
 *
 * <p>El dominio cifra antes de persistir y descifra al exponer contenido reservado, sin conocer el
 * algoritmo concreto. Las implementaciones (AES-GCM o passthrough) concentran la criptografía. Las
 * operaciones son tolerantes a {@code null} y cadenas en blanco (se devuelven tal cual).</p>
 */
public interface ProtectorDatosReservados {

    /**
     * Cifra un texto en claro para su almacenamiento en reposo.
     *
     * @param claro texto en claro (admite nulos/blancos)
     * @return la representación a almacenar, o el propio valor si es nulo/en blanco
     */
    String cifrar(String claro);

    /**
     * Descifra una representación almacenada hacia su texto en claro.
     *
     * @param almacenado representación en reposo (admite nulos/blancos)
     * @return el texto en claro, o el propio valor si es nulo/en blanco
     */
    String descifrar(String almacenado);
}
