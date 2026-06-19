package cl.smid.vulneraciones.infraestructura.cliente;

import cl.smid.vulneraciones.config.PropiedadesEnriquecimiento;
import cl.smid.vulneraciones.dominio.puerto.salida.ClienteCatalogo;
import cl.smid.vulneraciones.infraestructura.seguridad.ProveedorContexto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Cliente REST del puerto {@link ClienteCatalogo}: etiqueta derechos y causas contra catalogo-service
 * (6.7), propagando el token del usuario.
 *
 * <ul>
 *   <li>{@link #etiquetaDerecho(String)} usa {@code GET /catalogo/derechos/{altKey}}.</li>
 *   <li>{@link #etiquetaCausa(String)} recorre {@code GET /catalogo/derechos?formato=plano} y busca el
 *   nodo cuyo {@code altKey} coincide (la API de Catálogo no expone la causa por {@code alt_key}
 *   aislado; la búsqueda en la lista plana es la vía best-effort disponible).</li>
 * </ul>
 *
 * <p>Ante cualquier fallo degrada a {@link Optional#empty()}.</p>
 */
@Component
@ConditionalOnProperty(prefix = "smid.enriquecimiento.catalogo", name = "activo", havingValue = "true")
public class ClienteCatalogoRest implements ClienteCatalogo {

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record NodoCatalogoDto(String altKey, String codigo, String nombre) {
    }

    private static final ParameterizedTypeReference<List<NodoCatalogoDto>> LISTA_NODOS =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient cliente;
    private final ProveedorContexto proveedorContexto;

    public ClienteCatalogoRest(PropiedadesEnriquecimiento propiedades, ProveedorContexto proveedorContexto) {
        var catalogo = Objects.requireNonNull(propiedades.catalogo(), "La configuración de catálogo es obligatoria");
        String urlBase = Objects.requireNonNull(catalogo.url(), "La URL de catálogo es obligatoria");
        this.cliente = RestClient.builder().baseUrl(urlBase).build();
        this.proveedorContexto = proveedorContexto;
    }

    @Override
    public Optional<String> etiquetaDerecho(String idDerechoAlt) {
        String token = proveedorContexto.tokenBruto();
        if (idDerechoAlt == null || idDerechoAlt.isBlank() || token == null || token.isBlank()) {
            return Optional.empty();
        }
        try {
            NodoCatalogoDto dto = cliente.get()
                    .uri("/catalogo/derechos/{altKey}", idDerechoAlt)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .body(NodoCatalogoDto.class);
            return etiquetar(dto);
        } catch (RuntimeException ex) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> etiquetaCausa(String idCausaAlt) {
        String token = proveedorContexto.tokenBruto();
        if (idCausaAlt == null || idCausaAlt.isBlank() || token == null || token.isBlank()) {
            return Optional.empty();
        }
        try {
            List<NodoCatalogoDto> nodos = cliente.get()
                    .uri(uri -> uri.path("/catalogo/derechos").queryParam("formato", "plano").build())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .body(Objects.requireNonNull(LISTA_NODOS, "El tipo de lista de catálogo es obligatorio"));
            if (nodos == null) {
                return Optional.empty();
            }
            return nodos.stream()
                    .filter(n -> idCausaAlt.equals(n.altKey()))
                    .findFirst()
                    .flatMap(this::etiquetar);
        } catch (RuntimeException ex) {
            return Optional.empty();
        }
    }

    private Optional<String> etiquetar(NodoCatalogoDto dto) {
        if (dto == null) {
            return Optional.empty();
        }
        String codigo = dto.codigo() == null ? "" : dto.codigo().trim();
        String nombre = dto.nombre() == null ? "" : dto.nombre().trim();
        if (codigo.isEmpty() && nombre.isEmpty()) {
            return Optional.empty();
        }
        if (codigo.isEmpty()) {
            return Optional.of(nombre);
        }
        if (nombre.isEmpty()) {
            return Optional.of(codigo);
        }
        return Optional.of(codigo + " - " + nombre);
    }
}
