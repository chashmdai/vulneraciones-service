-- =====================================================================================
--  vulneraciones-service (SMID 6.5) — Ficha Interna Reservada (FIR)
--  Migración inicial del esquema  (BD: db_vulneraciones)
-- =====================================================================================
--  DECISIONES ARQUITECTÓNICAS VINCULANTES (OVERRIDES) APLICADAS EN ESTE ESQUEMA:
--   1. Arranque limpio: sin migración ni ETL del sistema legado. La costura SIGER es futura.
--   2. Identificadores públicos: SOLO 'alt_key' (UUID, VARCHAR(36)). La PK BIGINT 'id' es
--      interna y jamás cruza la frontera del servicio.
--   3. Enumerados: VARCHAR(N) + CHECK, nunca el tipo ENUM nativo de MySQL.
--   4. Marcas temporales: DATETIME(6) interpretadas en UTC (hibernate.jdbc.time_zone=UTC).
--   5. Booleanos: TINYINT(1) (con tinyInt1isBit=true en la URL del driver).
--   6. Denegación territorial: se resuelve como 404 en la capa de aplicación (no se modela aquí).
--   8. Datos reservados (relato/descripcion) se guardan CIFRADOS en reposo (DT-1); nunca se indexan.
--  Motor: InnoDB · Cotejo: utf8mb4_0900_ai_ci
-- =====================================================================================

-- -------------------------------------------------------------------------------------
--  Tabla: ficha  (agregado raíz; 1:1 con el caso de 6.4)
-- -------------------------------------------------------------------------------------
CREATE TABLE ficha (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    alt_key             VARCHAR(36)     NOT NULL,
    numero_ficha        VARCHAR(32)     NOT NULL,
    codigo_sede         VARCHAR(8)      NOT NULL,
    serie               VARCHAR(8)      NOT NULL,
    correlativo         BIGINT          NOT NULL,
    anio                INT             NOT NULL,
    id_caso_alt         VARCHAR(36)     NOT NULL,
    numero_expediente   VARCHAR(32)     NOT NULL,
    id_sede_alt         VARCHAR(36)     NOT NULL,
    id_unidad_alt       VARCHAR(36)     NULL,
    complejidad         VARCHAR(12)     NULL,
    es_beta             TINYINT(1)      NOT NULL,
    estado              VARCHAR(20)     NOT NULL,
    abierta_en          DATETIME(6)     NOT NULL,
    cerrada_en          DATETIME(6)     NULL,
    creada_en           DATETIME(6)     NOT NULL,
    actualizada_en      DATETIME(6)     NOT NULL,
    creada_por          VARCHAR(36)     NULL,
    vigente             TINYINT(1)      NOT NULL,
    CONSTRAINT pk_ficha PRIMARY KEY (id),
    CONSTRAINT uk_ficha_alt_key UNIQUE (alt_key),
    CONSTRAINT uk_ficha_numero UNIQUE (numero_ficha),
    CONSTRAINT uk_ficha_caso UNIQUE (id_caso_alt),
    CONSTRAINT ck_ficha_serie CHECK (serie IN ('OFICIAL', 'BETA')),
    CONSTRAINT ck_ficha_estado CHECK (estado IN ('EN_ELABORACION', 'CERRADA')),
    CONSTRAINT ck_ficha_complejidad CHECK (complejidad IS NULL OR complejidad IN ('BAJA', 'MEDIANA', 'ALTA')),
    CONSTRAINT ck_ficha_es_beta CHECK (es_beta IN (0, 1)),
    CONSTRAINT ck_ficha_vigente CHECK (vigente IN (0, 1))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX ix_ficha_sede ON ficha (id_sede_alt);
CREATE INDEX ix_ficha_unidad ON ficha (id_unidad_alt);
CREATE INDEX ix_ficha_estado ON ficha (estado);

-- -------------------------------------------------------------------------------------
--  Tabla: ficha_vulneracion  (hija; relato CIFRADO en reposo)
-- -------------------------------------------------------------------------------------
CREATE TABLE ficha_vulneracion (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    alt_key             VARCHAR(36)     NOT NULL,
    id_ficha            BIGINT          NOT NULL,
    id_derecho_alt      VARCHAR(36)     NOT NULL,
    id_causa_alt        VARCHAR(36)     NULL,
    id_nna_alt          VARCHAR(36)     NOT NULL,
    nna_nombre_legible  VARCHAR(200)    NULL,
    gravedad            VARCHAR(12)     NOT NULL,
    relato_cifrado      VARCHAR(8000)   NULL,
    fecha_hecho         DATE            NULL,
    registrado_en       DATETIME(6)     NOT NULL,
    actualizado_en      DATETIME(6)     NOT NULL,
    registrado_por      VARCHAR(36)     NULL,
    CONSTRAINT pk_ficha_vulneracion PRIMARY KEY (id),
    CONSTRAINT uk_vulneracion_alt_key UNIQUE (alt_key),
    CONSTRAINT fk_vulneracion_ficha FOREIGN KEY (id_ficha) REFERENCES ficha (id),
    CONSTRAINT ck_vulneracion_gravedad CHECK (gravedad IN ('LEVE', 'MEDIA', 'GRAVE', 'GRAVISIMA'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX ix_vulneracion_ficha ON ficha_vulneracion (id_ficha);
CREATE INDEX ix_vulneracion_nna ON ficha_vulneracion (id_nna_alt);
CREATE INDEX ix_vulneracion_derecho ON ficha_vulneracion (id_derecho_alt);

-- -------------------------------------------------------------------------------------
--  Tabla: ficha_antecedente  (hija; descripcion CIFRADA en reposo)
-- -------------------------------------------------------------------------------------
CREATE TABLE ficha_antecedente (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    alt_key             VARCHAR(36)     NOT NULL,
    id_ficha            BIGINT          NOT NULL,
    tipo                VARCHAR(24)     NOT NULL,
    descripcion_cifrada VARCHAR(8000)   NULL,
    fecha               DATE            NULL,
    fuente              VARCHAR(160)    NULL,
    registrado_en       DATETIME(6)     NOT NULL,
    registrado_por      VARCHAR(36)     NULL,
    CONSTRAINT pk_ficha_antecedente PRIMARY KEY (id),
    CONSTRAINT uk_antecedente_alt_key UNIQUE (alt_key),
    CONSTRAINT fk_antecedente_ficha FOREIGN KEY (id_ficha) REFERENCES ficha (id),
    CONSTRAINT ck_antecedente_tipo CHECK (tipo IN ('DERIVACION', 'PERICIA', 'ANTECEDENTE_FAMILIAR', 'ESCOLAR', 'SALUD', 'OTRO'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX ix_antecedente_ficha ON ficha_antecedente (id_ficha);

-- -------------------------------------------------------------------------------------
--  Tabla: ficha_transicion  (historial append-only de la máquina de estados)
--    El asiento de apertura usa estado_origen = NULL y accion = 'MATERIALIZACION'.
-- -------------------------------------------------------------------------------------
CREATE TABLE ficha_transicion (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    alt_key             VARCHAR(36)     NOT NULL,
    id_ficha            BIGINT          NOT NULL,
    estado_origen       VARCHAR(20)     NULL,
    estado_destino      VARCHAR(20)     NOT NULL,
    accion              VARCHAR(32)     NOT NULL,
    observacion         VARCHAR(2000)   NULL,
    actor_alt           VARCHAR(36)     NULL,
    ocurrido_en         DATETIME(6)     NOT NULL,
    CONSTRAINT pk_ficha_transicion PRIMARY KEY (id),
    CONSTRAINT uk_transicion_alt_key UNIQUE (alt_key),
    CONSTRAINT fk_transicion_ficha FOREIGN KEY (id_ficha) REFERENCES ficha (id),
    CONSTRAINT ck_transicion_origen CHECK (estado_origen IS NULL OR estado_origen IN ('EN_ELABORACION', 'CERRADA')),
    CONSTRAINT ck_transicion_destino CHECK (estado_destino IN ('EN_ELABORACION', 'CERRADA')),
    CONSTRAINT ck_transicion_accion CHECK (accion IN ('MATERIALIZACION', 'CERRAR', 'REABRIR'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX ix_transicion_ficha ON ficha_transicion (id_ficha);

-- -------------------------------------------------------------------------------------
--  Tabla: correlativo_fir  (contador atómico del número de FIR)
--    Clave primaria compuesta (id_sede_alt, anio, serie): la serie BETA queda aislada de
--    la OFICIAL en filas distintas. La reserva usa INSERT ... ON DUPLICATE KEY UPDATE
--    ... LAST_INSERT_ID(ultimo + 1) (ver CorrelativoFirJdbc). No tiene entidad JPA: la
--    administra exclusivamente el adaptador JDBC, por lo que ddl-auto=validate no la exige.
-- -------------------------------------------------------------------------------------
CREATE TABLE correlativo_fir (
    id_sede_alt         VARCHAR(36)     NOT NULL,
    anio                INT             NOT NULL,
    serie               VARCHAR(8)      NOT NULL,
    ultimo              BIGINT          NOT NULL,
    CONSTRAINT pk_correlativo_fir PRIMARY KEY (id_sede_alt, anio, serie),
    CONSTRAINT ck_correlativo_serie CHECK (serie IN ('OFICIAL', 'BETA'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
