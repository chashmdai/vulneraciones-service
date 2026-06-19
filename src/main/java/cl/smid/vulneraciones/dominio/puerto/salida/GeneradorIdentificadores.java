package cl.smid.vulneraciones.dominio.puerto.salida;

/**
 * Puerto de salida: generación de identificadores opacos públicos ({@code alt_key}).
 */
public interface GeneradorIdentificadores {

    /** @return un nuevo identificador UUID en su representación canónica de 36 caracteres. */
    String nuevoUuid();
}
