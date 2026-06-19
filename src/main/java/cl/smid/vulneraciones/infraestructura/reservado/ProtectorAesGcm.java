package cl.smid.vulneraciones.infraestructura.reservado;

import cl.smid.vulneraciones.dominio.puerto.salida.ProtectorDatosReservados;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Adaptador de cifrado en reposo (DT-1) con AES-256-GCM.
 *
 * <p>La clave se entrega como Base64 de 32 bytes (256 bits) por variable de entorno. Para cada
 * registro se genera un IV aleatorio de 12 bytes; el formato almacenado es
 * {@code base64(iv):base64(ciphertext||tag)} (etiqueta de autenticación de 128 bits embebida). El
 * descifrado verifica la integridad: una manipulación del texto cifrado provoca un fallo de
 * autenticación.</p>
 *
 * <p>POJO puro: la criptografía se concentra aquí (única dependencia de {@code javax.crypto}). Es
 * tolerante a {@code null}/blancos (los devuelve tal cual), de modo que un relato/descipción ausente
 * no genera contenido cifrado.</p>
 */
public class ProtectorAesGcm implements ProtectorDatosReservados {

    private static final int LONGITUD_IV = 12;          // 96 bits, recomendado para GCM
    private static final int LONGITUD_TAG_BITS = 128;   // etiqueta de autenticación
    private static final String TRANSFORMACION = "AES/GCM/NoPadding";
    private static final String ALGORITMO = "AES";
    private static final char SEPARADOR = ':';

    private final byte[] clave;
    private final SecureRandom aleatorio = new SecureRandom();

    /**
     * @param claveBase64 clave AES de 256 bits codificada en Base64 (debe decodificar a 32 bytes)
     * @throws IllegalArgumentException si la clave no decodifica a exactamente 32 bytes
     */
    public ProtectorAesGcm(String claveBase64) {
        if (claveBase64 == null || claveBase64.isBlank()) {
            throw new IllegalArgumentException(
                    "La clave de cifrado reservado (RESERVADO_CLAVE) es obligatoria para el perfil aes-gcm.");
        }
        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(claveBase64.trim());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("RESERVADO_CLAVE no es Base64 válido.", ex);
        }
        if (bytes.length != 32) {
            throw new IllegalArgumentException(
                    "RESERVADO_CLAVE debe decodificar a 32 bytes (AES-256); se obtuvieron " + bytes.length + ".");
        }
        this.clave = bytes;
    }

    @Override
    public String cifrar(String claro) {
        if (claro == null || claro.isBlank()) {
            return claro;
        }
        try {
            byte[] iv = new byte[LONGITUD_IV];
            aleatorio.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMACION);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(clave, ALGORITMO),
                    new GCMParameterSpec(LONGITUD_TAG_BITS, iv));
            byte[] cifrado = cipher.doFinal(claro.getBytes(StandardCharsets.UTF_8));
            Base64.Encoder b64 = Base64.getEncoder();
            return b64.encodeToString(iv) + SEPARADOR + b64.encodeToString(cifrado);
        } catch (Exception ex) {
            throw new IllegalStateException("Fallo al cifrar el dato reservado.", ex);
        }
    }

    @Override
    public String descifrar(String almacenado) {
        if (almacenado == null || almacenado.isBlank()) {
            return almacenado;
        }
        int corte = almacenado.indexOf(SEPARADOR);
        if (corte <= 0 || corte >= almacenado.length() - 1) {
            throw new IllegalStateException("Formato de dato reservado inválido (se esperaba iv:ciphertext).");
        }
        try {
            Base64.Decoder b64 = Base64.getDecoder();
            byte[] iv = b64.decode(almacenado.substring(0, corte));
            byte[] cifrado = b64.decode(almacenado.substring(corte + 1));
            Cipher cipher = Cipher.getInstance(TRANSFORMACION);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(clave, ALGORITMO),
                    new GCMParameterSpec(LONGITUD_TAG_BITS, iv));
            byte[] claro = cipher.doFinal(cifrado);
            return new String(claro, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new IllegalStateException("Fallo al descifrar el dato reservado (clave o integridad).", ex);
        }
    }
}
