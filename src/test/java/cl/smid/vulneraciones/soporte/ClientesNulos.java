package cl.smid.vulneraciones.soporte;

import cl.smid.vulneraciones.dominio.puerto.salida.ClienteCasos;
import cl.smid.vulneraciones.dominio.puerto.salida.ClienteCatalogo;
import cl.smid.vulneraciones.dominio.puerto.salida.ClientePersonas;

import java.util.Optional;

/** Dobles inertes de las costuras de enriquecimiento (siempre vacío). */
public final class ClientesNulos {

    private ClientesNulos() {
    }

    public static ClienteCasos casos() {
        return idCasoAlt -> Optional.empty();
    }

    public static ClientePersonas personas() {
        return idNnaAlt -> Optional.empty();
    }

    public static ClienteCatalogo catalogo() {
        return new ClienteCatalogo() {
            @Override
            public Optional<String> etiquetaDerecho(String idDerechoAlt) {
                return Optional.empty();
            }

            @Override
            public Optional<String> etiquetaCausa(String idCausaAlt) {
                return Optional.empty();
            }
        };
    }
}
