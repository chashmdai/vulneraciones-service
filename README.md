# vulneraciones-service (SMID 6.5) — Ficha Interna Reservada (FIR)

Microservicio del ecosistema **SMID** de la Defensoría de los Derechos de la Niñez. Gestiona la
**Ficha Interna Reservada (FIR)**: el expediente interno y confidencial asociado 1:1 a un Caso, que
consolida las **vulneraciones** de derechos detectadas y sus **antecedentes** de apoyo. La FIR **no se
crea por API**: se materializa de forma **asíncrona** al consumir el evento `caso.abierto` que emite
casos-service (6.4).

---

## 1. Stack

- **Java 21**, **Spring Boot 3.5.15**, **Maven** (sin wrapper)
- **MySQL 8** (InnoDB, `utf8mb4_0900_ai_ci`), **Flyway** (`ddl-auto=validate`)
- **RabbitMQ** (consumo de `caso.abierto` y publicación de eventos; transporte conmutable a log)
- **jjwt 0.12.5** (validación de JWT HS256 con `kid`)
- **SpringDoc OpenAPI 2.8.x** (Swagger UI)
- **Testcontainers** (integración y concurrencia), **JUnit 5**, **AssertJ**

Puerto por defecto: **8091**. Tras el `StripPrefix=1` del gateway, los controladores cuelgan de
`/vulneraciones/...`.

---

## 2. Arquitectura (hexagonal estricta)

```
api/                 Controladores REST, DTOs, ensamblador de respuestas, manejo de errores
dominio/             Núcleo POJO puro (sin Spring)
  ├── modelo/        Agregado Ficha, hijas (Vulneracion, Antecedente), VOs, enums, vistas
  ├── puerto/entrada Casos de uso (MaterializarFicha, ConsultarFichas, Registrar*, Transicionar...)
  ├── puerto/salida  Contratos de salida (RepositorioFichas, CorrelativoFirPort, clientes, etc.)
  ├── servicio/      Servicios de dominio (MaquinaEstadosFicha, GeneradorNumeroFir, EvaluadorAlcance)
  │                  y el orquestador ServicioVulneraciones
  └── excepcion/     Jerarquía de errores de dominio con su código
infraestructura/     Adaptadores: persistencia JPA/JDBC, mensajería, seguridad JWT, clientes REST,
                     protector de datos reservados, soporte (reloj, uuid, directorio de sedes)
config/              Propiedades tipadas (@ConfigurationProperties) y raíz de composición (CableadoDominio)
```

El dominio no conoce Spring; el cableado vive en `config/CableadoDominio`. **Un único bean**
(`ServicioVulneraciones`) implementa los seis puertos de entrada.

---

## 3. Numeración de la FIR

Formato: `FIR-{CÓDIGO_SEDE}-{[B]CORRELATIVO}/{AÑO}`.

- Oficial: `FIR-RM-1/2027`. Beta (marcha blanca): `FIR-RM-B1/2027`.
- El **código de sede** se resuelve desde `smid.sedes.codigos.<alt_key>` (defecto `SD`).
- El **correlativo** se reserva de forma **atómica y segura ante concurrencia** mediante el modismo
  MySQL `INSERT ... ON DUPLICATE KEY UPDATE ultimo = LAST_INSERT_ID(ultimo + 1)` ejecutado sobre la
  misma conexión, dentro de la transacción del listener. La clave primaria compuesta
  `(id_sede_alt, anio, serie)` **aísla la serie BETA de la OFICIAL**, de modo que la serie oficial
  arranca limpia en su correlativo unificado.
- La **serie** se decide por la fecha de corte `smid.fir.inicio-oficial` (defecto `2027-01-01`):
  apertura anterior ⇒ BETA. Si el evento informa `esBeta`, esa bandera **tiene prioridad**.

---

## 4. Ciclo de vida (máquina de estados)

```
            CERRAR (rol Coordinación)
EN_ELABORACION ───────────────────────────▶ CERRADA
       ▲                                        │
       └──────────── REABRIR (rol Coordinación) ┘
```

- Estado inicial al materializar: `EN_ELABORACION`.
- La **ventana de mutabilidad** (alta de vulneraciones/antecedentes) existe **solo** en
  `EN_ELABORACION`; fuera de ella ⇒ **VUL-409**.
- `CERRAR` y `REABRIR` son acciones administrativas y exigen **rol de Coordinación** (si falta ⇒
  **AUTZ-004 / 403**).
- El asiento de apertura del historial usa `accion = MATERIALIZACION`, `estado_origen = NULL` y el
  actor de sistema.

---

## 5. Materialización asíncrona (consumo de `caso.abierto`)

- **Exchange** (topic): `smid.eventos` · **routing key**: `caso.abierto`.
- **Cola**: `vulneraciones.caso-abierto` (durable) con DLX `vulneraciones.dlx` y DLQ
  `vulneraciones.caso-abierto.dlq`. Concurrencia 1, 3 reintentos con backoff exponencial,
  `defaultRequeueRejected=false`.
- **Gating**: la FIR se crea **solo** si `metadatos.requiereFichaReservada == true`; en caso contrario
  se confirma (ack) sin crear nada.
- **Idempotencia**: la unicidad de `id_caso_alt` (`uk_ficha_caso`) más un pre-chequeo
  (`existePorCaso`) hacen que las reentregas *at-least-once* sean no-op. Una carrera entre instancias
  se traduce en `FichaDuplicadaException`, que propaga ⇒ reintento ⇒ no-op.
- **Mensaje malformado** (estructura mínima ausente) ⇒ se deriva a la **DLQ** sin reencolar.

Cuerpo esperado:

```json
{
  "tipo": "caso.abierto",
  "altKey": "<alt_key del caso>",
  "ocurridoEn": "2027-05-01T12:00:00Z",
  "metadatos": {
    "numeroExpediente": "EXP-2027-000123",
    "estado": "ABIERTO",
    "idSede": "<alt_key sede>",
    "idUnidad": "<alt_key unidad>",
    "complejidad": "ALTA",
    "requiereFichaReservada": true,
    "esBeta": false
  }
}
```

### Eventos emitidos (metadata-only, G7)

Nunca contienen relatos, nombres ni RUT. Transporte conmutable (log/RabbitMQ), publicación tolerante
a fallos.

| Evento                   | Metadatos                                                        |
|--------------------------|------------------------------------------------------------------|
| `ficha.abierta`          | `numeroFicha`, `idCasoAlt`, `idSede`, `idUnidad`, `complejidad`, `esBeta` |
| `vulneracion.registrada` | `idFichaAlt`, `idDerechoAlt`, `gravedad`                          |
| `antecedente.registrado` | `idFichaAlt`, `tipo`                                              |
| `ficha.cerrada`          | `idFichaAlt`, `numeroFicha`                                       |

---

## 6. API REST

Todas las rutas requieren `Authorization: Bearer <JWT>`. Identificadores públicos: **solo `alt_key`**.
Swagger documenta las rutas internas del servicio (`/vulneraciones/**`); el frontend consume siempre
vía Gateway anteponiendo `/api`, por ejemplo `/api/vulneraciones/fichas`.

Documentación local:

- `GET /v3/api-docs`
- `GET /swagger-ui/index.html`

| Método | Ruta                                              | Descripción                                  |
|--------|---------------------------------------------------|----------------------------------------------|
| GET    | `/vulneraciones/fichas/{altKey}`                  | Detalle (reservado redactado según rol)      |
| GET    | `/vulneraciones/fichas?idCaso=&estado=&pagina=&tamano=` | Listado paginado dentro del alcance     |
| GET    | `/vulneraciones/fichas/{altKey}/reservado`        | Contenido reservado en claro (rol reservado) |
| POST   | `/vulneraciones/fichas/{altKey}/vulneraciones`    | Registrar vulneración (201)                  |
| POST   | `/vulneraciones/fichas/{altKey}/antecedentes`     | Registrar antecedente (201)                  |
| POST   | `/vulneraciones/fichas/{altKey}/transiciones`     | CERRAR/REABRIR (rol Coordinación)            |

### Ejemplos de payload

Registrar vulneración:

```json
{
  "idDerechoAlt": "8b1d...e1",
  "idCausaAlt": null,
  "idNnaAlt": "2f9c...a7",
  "gravedad": "GRAVE",
  "relato": "Texto reservado del relato",
  "fechaHecho": "2027-04-20"
}
```

Registrar antecedente:

```json
{ "tipo": "ESCOLAR", "descripcion": "Texto reservado", "fecha": "2027-03-15", "fuente": "Establecimiento educacional" }
```

Transición:

```json
{ "accion": "CERRAR", "observacion": "Cierre por resolución del caso" }
```

Sobre de error unificado (campo **`ruta`**, no `path`):

```json
{
  "status": 404,
  "error": "Not Found",
  "codigo": "VUL-404",
  "mensaje": "No existe una ficha accesible con identificador ...",
  "ruta": "/vulneraciones/fichas/...",
  "timestamp": "2027-05-01T12:00:00.123456Z"
}
```

### Códigos de error

| Código   | HTTP | Significado                                            |
|----------|------|--------------------------------------------------------|
| VUL-001  | 400  | Validación / cuerpo ilegible / evento sin datos mínimos|
| VUL-404  | 404  | Ficha inexistente **o** fuera del alcance territorial  |
| VUL-409  | 409  | Conflicto de estado (ficha no mutable / transición inválida / duplicado) |
| VUL-422  | 422  | Regla de negocio incumplida                            |
| VUL-500  | 500  | Error interno                                          |
| AUTZ-003 | 401  | No autenticado (token ausente o inválido)              |
| AUTZ-004 | 403  | Autenticado sin rol suficiente                         |

---

## 7. Seguridad y confidencialidad

- **JWT HS256** validado en el borde (defensa en profundidad): firma por `kid` (par
  **activo/previo** para rotación), emisor `smid-auth`, audiencia `smid-servicios`, expiración.
- **Control territorial** por alcance del usuario (`UNIDAD`/`SEDE`/`NACIONAL`), registro a registro.
  La denegación territorial se expresa como **404** (no se revela la existencia de la ficha).
- **Cifrado en reposo (DT-1)** de relatos y descripciones: `smid.reservado.cifrado` conmuta entre
  `aes-gcm` (AES-256-GCM, IV aleatorio, formato `base64(iv):base64(ct+tag)`) y `none` (passthrough,
  **solo desarrollo con datos sintéticos**). Estos campos nunca se indexan ni se registran en logs.
- **Acceso reservado por rol**: el detalle redacta relato/descripción (`reservadoOculto=true`) salvo
  para roles habilitados; el endpoint `/reservado` responde **403** si el rol no habilita.

---

## 8. Enriquecimiento on-demand (opcional)

Costuras REST hacia casos/personas/catálogo, **desactivadas por defecto** (el servicio opera en
aislamiento). Cuando se activan (`smid.enriquecimiento.<x>.activo=true`), propagan el bearer del
solicitante y **degradan a vacío** ante cualquier ausencia o fallo. El listener nunca usa estas
costuras (no hay contexto de petición durante el consumo).

---

## 9. Variables de entorno

Ver `.env.example`. Las principales:

| Variable                         | Descripción                                             |
|----------------------------------|---------------------------------------------------------|
| `DB_HOST`/`DB_PORT`/`DB_NAME`    | Conexión MySQL (BD `db_vulneraciones`)                  |
| `DB_USER`/`DB_PASSWORD`          | Credenciales MySQL                                      |
| `JWT_SECRET`                     | Secreto HS256 (UTF-8, **≥ 32 bytes**) — obligatorio     |
| `JWT_KID_ACTIVO`                 | `kid` activo (defecto `smid-2026-06`)                   |
| `JWT_KID_PREVIO`/`JWT_SECRET_PREVIO` | Par opcional para ventana de rotación              |
| `RESERVADO_CIFRADO`              | `aes-gcm` o `none`                                      |
| `RESERVADO_CLAVE`                | Clave AES-256 en Base64 (32 bytes) para `aes-gcm`       |
| `RABBIT_HOST`/`RABBIT_PORT`/...  | Conexión RabbitMQ                                       |
| `EVENTOS_CONSUMO`/`EVENTOS_TRANSPORTE` | `rabbitmq` o `none`/`log`                         |
| `SEDE_CODIGO_DEFECTO`            | Código de sede por defecto (defecto `SD`)               |

El perfil **`local`** (`-Dspring-boot.run.profiles=local`) ejecuta en aislamiento: sin consumo de
eventos, publicación por log, enriquecimiento desactivado y datos reservados sin cifrar.

---

## 10. Construcción, pruebas y ejecución

> Salvedades operativas:
> - **Compilar** requiere acceso de red a Maven Central para resolver dependencias.
> - Las **pruebas unitarias** del dominio **no requieren Docker**.
> - Las pruebas de **integración** y de **concurrencia** requieren **Docker** (Testcontainers); si no
>   está disponible, se **omiten** automáticamente (no fallan).

```bash
# Compilar y ejecutar solo pruebas unitarias (sin Docker):
mvn -DskipITs test

# Suite completa (requiere Docker para las IT):
mvn verify

# Ejecutar en local (aislamiento):
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

El esquema lo gobierna **Flyway** (`src/main/resources/db/migration/V1__inicial.sql`); Hibernate solo
valida (`ddl-auto=validate`).

---

## 11. Decisiones arquitectónicas (OVERRIDES)

1. **Arranque limpio**: sin migración ni ETL del sistema legado; la costura SIGER es futura.
2. **Identificadores públicos**: solo `alt_key` (UUID). La PK `BIGINT` interna jamás cruza la frontera.
3. **Enumerados**: `VARCHAR(N)` + `CHECK`, nunca el tipo `ENUM` nativo.
4. **Marcas temporales**: `DATETIME(6)` en UTC (`hibernate.jdbc.time_zone=UTC`).
5. **Booleanos**: `TINYINT(1)` (`tinyInt1isBit=true`).
6. **Denegación territorial = 404** (no 403).
7. **`@Transactional` solo en la frontera** (controlador y listener).
8. **Eventos metadata-only** (G7): nunca relatos/nombres/RUT; transporte conmutable; publicación
   tolerante a fallos.
9. **Sobre de error con campo `ruta`** (no `path`).
10. **JWT con `kidActivo/secretoActivo`** y par opcional `kidPrevio/secretoPrevio`.
11. **Hexagonal estricto**: dominio POJO puro; cableado en `config/CableadoDominio`.

> **Protección de datos de NNA**: este repositorio no contiene datos reales. Los relatos/descripciones
> de prueba son sintéticos y, en producción, se almacenan cifrados (DT-1) y nunca se emiten en eventos
> ni se registran en logs.
