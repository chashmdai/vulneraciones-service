package cl.smid.vulneraciones.infraestructura.cliente;

import cl.smid.vulneraciones.config.PropiedadesEnriquecimiento;
import cl.smid.vulneraciones.dominio.puerto.salida.ClientePersonas;
import cl.smid.vulneraciones.infraestructura.seguridad.ProveedorContexto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Objects;
import java.util.Optional;

/**
 * Cliente REST del puerto {@link ClientePersonas}: resuelve el nombre legible de un NNA contra
 * personas-service (6.2) en {@code GET /personas/{altKey}}, propagando el token del usuario.
 *
 * <p>Best-effort: ante cualquier fallo (red, 4xx/5xx, deserialización o token ausente) degrada a
 * {@link Optional#empty()}, dejando el snapshot {@code nnaNombreLegible} nulo.</p>
 */
@Component
@ConditionalOnProperty(prefix = "smid.enriquecimiento.personas", name = "activo", havingValue = "true")
public class ClientePersonasRest implements ClientePersonas {

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record PersonaDto(String nombreLegible) {
    }

    private final RestClient cliente;
    private final ProveedorContexto proveedorContexto;

    public ClientePersonasRest(PropiedadesEnriquecimiento propiedades, ProveedorContexto proveedorContexto) {
        var personas = Objects.requireNonNull(propiedades.personas(), "La configuración de personas es obligatoria");
        String urlBase = Objects.requireNonNull(personas.url(), "La URL de personas es obligatoria");
        this.cliente = RestClient.builder().baseUrl(urlBase).build();
        this.proveedorContexto = proveedorContexto;
    }

    @Override
    public Optional<String> nombreLegible(String idNnaAlt) {
        String token = proveedorContexto.tokenBruto();
        if (idNnaAlt == null || idNnaAlt.isBlank() || token == null || token.isBlank()) {
            return Optional.empty();
        }
        try {
            PersonaDto dto = cliente.get()
                    .uri("/personas/{altKey}", idNnaAlt)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .body(PersonaDto.class);
            if (dto == null || dto.nombreLegible() == null || dto.nombreLegible().isBlank()) {
                return Optional.empty();
            }
            return Optional.of(dto.nombreLegible());
        } catch (RuntimeException ex) {
            return Optional.empty();
        }
    }
}
