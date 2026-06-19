package cl.smid.vulneraciones;

import cl.smid.vulneraciones.soporte.GeneradorTokensPrueba;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueba de integración end-to-end del servicio (contexto Spring real, MySQL y RabbitMQ en
 * contenedores). Cubre la materialización asíncrona desde {@code caso.abierto} (incluida idempotencia
 * y gating), la autenticación JWT, el control territorial (404), el registro de hijas, el acceso
 * reservado por rol y el cierre administrativo por rol.
 *
 * <p>Se deshabilita automáticamente si Docker no está disponible.</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
@DisplayName("vulneraciones-service (IT): flujo end-to-end con MySQL y RabbitMQ")
class VulneracionesIntegracionIT {

    @Container
    static final MySQLContainer<?> MYSQL = crearMysql();

    @Container
    static final RabbitMQContainer RABBIT = new RabbitMQContainer("rabbitmq:3.13-management");

    @SuppressWarnings("resource")
    private static MySQLContainer<?> crearMysql() {
        return new MySQLContainer<>("mysql:8.0")
                .withDatabaseName("db_vulneraciones")
                .withUrlParam("tinyInt1isBit", "true")
                .withUrlParam("serverTimezone", "UTC");
    }

    @DynamicPropertySource
    static void propiedades(DynamicPropertyRegistry registro) {
        registro.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registro.add("spring.datasource.username", MYSQL::getUsername);
        registro.add("spring.datasource.password", MYSQL::getPassword);
        registro.add("spring.rabbitmq.host", RABBIT::getHost);
        registro.add("spring.rabbitmq.port", RABBIT::getAmqpPort);
        registro.add("spring.rabbitmq.username", () -> "guest");
        registro.add("spring.rabbitmq.password", () -> "guest");
        registro.add("smid.jwt.secreto-activo", () -> GeneradorTokensPrueba.SECRETO);
        registro.add("smid.eventos.consumo", () -> "rabbitmq");
        registro.add("smid.eventos.transporte", () -> "rabbitmq");
        registro.add("smid.reservado.cifrado", () -> "none");
        registro.add("smid.enriquecimiento.casos.activo", () -> "false");
        registro.add("smid.enriquecimiento.personas.activo", () -> "false");
        registro.add("smid.enriquecimiento.catalogo.activo", () -> "false");
    }

    @LocalServerPort
    private int puerto;

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String TOKEN_COORD =
            GeneradorTokensPrueba.token("coord", List.of("ADMIN_NACIONAL"), "sede-it", "uni-it", "NACIONAL");
    private static final String TOKEN_PROF =
            GeneradorTokensPrueba.token("prof", List.of("PROFESIONAL"), "sede-it", "uni-it", "NACIONAL");
    private static final String TOKEN_OTRA_SEDE =
            GeneradorTokensPrueba.token("admin2", List.of("ADMIN_NACIONAL"), "sede-otra", "uni-otra", "SEDE");

    // ---------------------------------------------------------------- utilidades HTTP/AMQP

    private String url(String path) {
        return "http://localhost:" + puerto + path;
    }

    private HttpHeaders headers(String token) {
        HttpHeaders h = new HttpHeaders();
        if (token != null) {
            h.setBearerAuth(token);
        }
        return h;
    }

    private ResponseEntity<JsonNode> get(String path, String token) {
        return rest.exchange(url(path), HttpMethod.GET, new HttpEntity<>(null, headers(token)), JsonNode.class);
    }

    private ResponseEntity<JsonNode> post(String path, String token, Object body) {
        HttpHeaders h = headers(token);
        h.setContentType(MediaType.APPLICATION_JSON);
        return rest.exchange(url(path), HttpMethod.POST, new HttpEntity<>(body, h), JsonNode.class);
    }

    private void publicarCasoAbierto(String idCaso, boolean requiere) {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("numeroExpediente", "EXP-" + idCaso);
        meta.put("estado", "ABIERTO");
        meta.put("idSede", "sede-it");
        meta.put("idUnidad", "uni-it");
        meta.put("complejidad", "ALTA");
        meta.put("requiereFichaReservada", requiere);
        meta.put("esBeta", false);
        Map<String, Object> evento = new LinkedHashMap<>();
        evento.put("tipo", "caso.abierto");
        evento.put("altKey", idCaso);
        evento.put("ocurridoEn", Instant.parse("2027-05-01T12:00:00Z"));
        evento.put("metadatos", meta);
        rabbitTemplate.convertAndSend("smid.eventos", "caso.abierto", evento);
    }

    private long totalPorCaso(String idCaso) {
        ResponseEntity<JsonNode> r = get("/vulneraciones/fichas?idCaso=" + idCaso, TOKEN_COORD);
        JsonNode cuerpo = r.getBody();
        if (!r.getStatusCode().is2xxSuccessful() || cuerpo == null) {
            return -1;
        }
        return cuerpo.path("total").asLong();
    }

    private String materializarYEsperar(String idCaso) throws InterruptedException {
        publicarCasoAbierto(idCaso, true);
        long fin = System.currentTimeMillis() + 20_000;
        while (System.currentTimeMillis() < fin) {
            ResponseEntity<JsonNode> r = get("/vulneraciones/fichas?idCaso=" + idCaso, TOKEN_COORD);
            JsonNode cuerpo = r.getBody();
            if (r.getStatusCode().is2xxSuccessful() && cuerpo != null
                    && cuerpo.path("total").asLong() >= 1) {
                return cuerpo.path("contenido").get(0).path("altKey").asText();
            }
            Thread.sleep(300);
        }
        throw new AssertionError("La ficha no se materializó a tiempo para el caso " + idCaso);
    }

    private JsonNode cuerpo(ResponseEntity<JsonNode> respuesta) {
        return Objects.requireNonNull(respuesta.getBody(), "La respuesta HTTP no incluyó cuerpo JSON");
    }

    // ---------------------------------------------------------------- pruebas

    @Test
    @DisplayName("Evento caso.abierto (requiere=true) materializa exactamente una ficha")
    void eventoCreaUnaFicha() throws InterruptedException {
        String altKey = materializarYEsperar("caso-it-1");

        ResponseEntity<JsonNode> detalle = get("/vulneraciones/fichas/" + altKey, TOKEN_COORD);
        assertThat(detalle.getStatusCode().value()).isEqualTo(200);
        JsonNode cuerpo = cuerpo(detalle);
        assertThat(cuerpo.path("numeroFicha").asText()).contains("/2027");
        assertThat(cuerpo.path("estado").asText()).isEqualTo("EN_ELABORACION");
    }

    @Test
    @DisplayName("Reentrega del mismo caso ⇒ sigue existiendo una sola ficha (idempotencia)")
    void reentregaEsIdempotente() throws InterruptedException {
        materializarYEsperar("caso-it-2");
        publicarCasoAbierto("caso-it-2", true);
        Thread.sleep(3000);
        assertThat(totalPorCaso("caso-it-2")).isEqualTo(1);
    }

    @Test
    @DisplayName("Evento con requiere=false no crea ficha (gating)")
    void gatingFalsoNoCrea() throws InterruptedException {
        publicarCasoAbierto("caso-it-3", false);
        Thread.sleep(3000);
        assertThat(totalPorCaso("caso-it-3")).isZero();
    }

    @Test
    @DisplayName("Sin token, el acceso devuelve 401")
    void sinTokenDevuelve401() {
        ResponseEntity<JsonNode> r = get("/vulneraciones/fichas", null);
        assertThat(r.getStatusCode().value()).isEqualTo(401);
        assertThat(cuerpo(r).path("codigo").asText()).isEqualTo("AUTZ-003");
    }

    @Test
    @DisplayName("Acceso fuera del alcance territorial ⇒ 404")
    void territorialDevuelve404() throws InterruptedException {
        String altKey = materializarYEsperar("caso-it-5");
        ResponseEntity<JsonNode> r = get("/vulneraciones/fichas/" + altKey, TOKEN_OTRA_SEDE);
        assertThat(r.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    @DisplayName("Registro de vulneración y antecedente devuelven 201")
    void registroDeHijas() throws InterruptedException {
        String altKey = materializarYEsperar("caso-it-6");

        ResponseEntity<JsonNode> v = post("/vulneraciones/fichas/" + altKey + "/vulneraciones", TOKEN_COORD,
                Map.of("idDerechoAlt", "der-1", "idNnaAlt", "nna-1", "gravedad", "GRAVE", "relato", "relato secreto"));
        assertThat(v.getStatusCode().value()).isEqualTo(201);

        ResponseEntity<JsonNode> a = post("/vulneraciones/fichas/" + altKey + "/antecedentes", TOKEN_COORD,
                Map.of("tipo", "ESCOLAR", "descripcion", "antecedente escolar"));
        assertThat(a.getStatusCode().value()).isEqualTo(201);
    }

    @Test
    @DisplayName("Contenido reservado: 403 sin rol; 200 y texto en claro con rol")
    void contenidoReservadoPorRol() throws InterruptedException {
        String altKey = materializarYEsperar("caso-it-7");
        post("/vulneraciones/fichas/" + altKey + "/vulneraciones", TOKEN_COORD,
                Map.of("idDerechoAlt", "der-1", "idNnaAlt", "nna-1", "gravedad", "GRAVE", "relato", "relato secreto"));

        ResponseEntity<JsonNode> sinRol = get("/vulneraciones/fichas/" + altKey + "/reservado", TOKEN_PROF);
        assertThat(sinRol.getStatusCode().value()).isEqualTo(403);

        ResponseEntity<JsonNode> conRol = get("/vulneraciones/fichas/" + altKey + "/reservado", TOKEN_COORD);
        assertThat(conRol.getStatusCode().value()).isEqualTo(200);
        assertThat(cuerpo(conRol).path("relatos").get(0).path("texto").asText()).isEqualTo("relato secreto");
    }

    @Test
    @DisplayName("Cerrar: 403 sin rol de Coordinación; 200 y estado CERRADA con rol")
    void cierrePorRol() throws InterruptedException {
        String altKey = materializarYEsperar("caso-it-8");

        ResponseEntity<JsonNode> sinRol = post("/vulneraciones/fichas/" + altKey + "/transiciones", TOKEN_PROF,
                Map.of("accion", "CERRAR", "observacion", "intento sin rol"));
        assertThat(sinRol.getStatusCode().value()).isEqualTo(403);

        ResponseEntity<JsonNode> conRol = post("/vulneraciones/fichas/" + altKey + "/transiciones", TOKEN_COORD,
                Map.of("accion", "CERRAR", "observacion", "cierre IT"));
        assertThat(conRol.getStatusCode().value()).isEqualTo(200);
        assertThat(cuerpo(conRol).path("estado").asText()).isEqualTo("CERRADA");
    }
}
