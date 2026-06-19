package cl.smid.vulneraciones.infraestructura.cliente;

import cl.smid.vulneraciones.config.PropiedadesEnriquecimiento;
import cl.smid.vulneraciones.dominio.puerto.salida.ClienteCasos;
import cl.smid.vulneraciones.infraestructura.seguridad.ProveedorContexto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Objects;
import java.util.Optional;

/**
 * Cliente REST del puerto {@link ClienteCasos}: valida/etiqueta el caso contra casos-service (6.4) en
 * {@code GET /casos/{altKey}}, propagando el token del usuario.
 *
 * <p>Best-effort: ante cualquier fallo degrada a {@link Optional#empty()}; el bloque de
 * enriquecimiento de la ficha queda "no disponible".</p>
 */
@Component
@ConditionalOnProperty(prefix = "smid.enriquecimiento.casos", name = "activo", havingValue = "true")
public class ClienteCasosRest implements ClienteCasos {

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record CasoDto(String estado, String numeroExpediente) {
    }

    private final RestClient cliente;
    private final ProveedorContexto proveedorContexto;

    public ClienteCasosRest(PropiedadesEnriquecimiento propiedades, ProveedorContexto proveedorContexto) {
        var casos = Objects.requireNonNull(propiedades.casos(), "La configuración de casos es obligatoria");
        String urlBase = Objects.requireNonNull(casos.url(), "La URL de casos es obligatoria");
        this.cliente = RestClient.builder().baseUrl(urlBase).build();
        this.proveedorContexto = proveedorContexto;
    }

    @Override
    public Optional<ResumenCaso> obtener(String idCasoAlt) {
        String token = proveedorContexto.tokenBruto();
        if (idCasoAlt == null || idCasoAlt.isBlank() || token == null || token.isBlank()) {
            return Optional.empty();
        }
        try {
            CasoDto dto = cliente.get()
                    .uri("/casos/{altKey}", idCasoAlt)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .body(CasoDto.class);
            if (dto == null) {
                return Optional.empty();
            }
            return Optional.of(new ResumenCaso(dto.estado(), dto.numeroExpediente()));
        } catch (RuntimeException ex) {
            return Optional.empty();
        }
    }
}
