package cl.smid.vulneraciones.infraestructura.reservado;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Protectores de datos reservados (DT-1): AES-GCM y passthrough")
class ProtectorReservadoTest {

    private static String claveBase64() {
        byte[] clave = new byte[32];
        for (int i = 0; i < clave.length; i++) {
            clave[i] = (byte) (i + 1);
        }
        return Base64.getEncoder().encodeToString(clave);
    }

    @Test
    @DisplayName("AES-GCM cifra y descifra recuperando el texto original")
    void aesGcmIdaYVuelta() {
        ProtectorAesGcm protector = new ProtectorAesGcm(claveBase64());
        String claro = "Relato reservado sobre un NNA (datos sintéticos de prueba).";

        String cifrado = protector.cifrar(claro);

        assertThat(cifrado).isNotNull().isNotEqualTo(claro).contains(":");
        assertThat(protector.descifrar(cifrado)).isEqualTo(claro);
    }

    @Test
    @DisplayName("AES-GCM usa IV aleatorio: dos cifrados del mismo texto difieren")
    void aesGcmIvAleatorio() {
        ProtectorAesGcm protector = new ProtectorAesGcm(claveBase64());
        String claro = "Texto repetido";
        assertThat(protector.cifrar(claro)).isNotEqualTo(protector.cifrar(claro));
    }

    @Test
    @DisplayName("AES-GCM es tolerante a entradas nulas")
    void aesGcmTolerante() {
        ProtectorAesGcm protector = new ProtectorAesGcm(claveBase64());
        assertThat(protector.cifrar(null)).isNull();
        assertThat(protector.descifrar(null)).isNull();
    }

    @Test
    @DisplayName("Passthrough no transforma el contenido")
    void passthrough() {
        ProtectorPassthrough protector = new ProtectorPassthrough();
        String claro = "Sin cifrar";
        assertThat(protector.cifrar(claro)).isEqualTo(claro);
        assertThat(protector.descifrar(claro)).isEqualTo(claro);
    }
}
