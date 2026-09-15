-- Sistema Electoral Nacional - Esquema PostgreSQL
-- Ejecutar contra la base elecciones_nacionales (ver README).

-- 1. TABLA: Departamentos de Bolivia
CREATE TABLE IF NOT EXISTS departamento (
    id_departamento SERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);

-- 2. TABLA: Municipios
CREATE TABLE IF NOT EXISTS municipio (
    id_municipio SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    id_departamento INT NOT NULL,
    FOREIGN KEY (id_departamento) REFERENCES departamento(id_departamento)
        ON DELETE RESTRICT
);

-- 3. TABLA: Recintos Electorales
CREATE TABLE IF NOT EXISTS recinto (
    id_recinto SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    id_municipio INT NOT NULL,
    FOREIGN KEY (id_municipio) REFERENCES municipio(id_municipio)
        ON DELETE RESTRICT
);

-- 4. TABLA: Mesas Electorales
-- estado: HABILITADA, COMPUTADA, ANULADA
CREATE TABLE IF NOT EXISTS mesa (
    id_mesa SERIAL PRIMARY KEY,
    numero_mesa INT NOT NULL,
    id_recinto INT NOT NULL,
    cantidad_inscritos INT NOT NULL DEFAULT 240,
    estado VARCHAR(20) DEFAULT 'HABILITADA',
    FOREIGN KEY (id_recinto) REFERENCES recinto(id_recinto)
        ON DELETE RESTRICT
);

-- 5. TABLA: Partidos / Alianzas Políticas
CREATE TABLE IF NOT EXISTS partido_politico (
    id_partido SERIAL PRIMARY KEY,
    sigla VARCHAR(20) NOT NULL UNIQUE,
    nombre_completo VARCHAR(150) NOT NULL,
    candidato_presidente VARCHAR(100) NOT NULL
);

-- 6. TABLA: Actas de Escrutinio por Mesa
CREATE TABLE IF NOT EXISTS acta (
    id_acta SERIAL PRIMARY KEY,
    id_mesa INT NOT NULL UNIQUE,
    votos_blancos INT NOT NULL DEFAULT 0,
    votos_nulos INT NOT NULL DEFAULT 0,
    total_ciudadanos_votaron INT NOT NULL DEFAULT 0,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_mesa) REFERENCES mesa(id_mesa)
        ON DELETE RESTRICT
);

-- 7. TABLA: Detalle de Votos por Partido Político
CREATE TABLE IF NOT EXISTS detalle_voto_partido (
    id_detalle SERIAL PRIMARY KEY,
    id_acta INT NOT NULL,
    id_partido INT NOT NULL,
    votos_validos INT NOT NULL DEFAULT 0,
    FOREIGN KEY (id_acta) REFERENCES acta(id_acta)
        ON DELETE RESTRICT,
    FOREIGN KEY (id_partido) REFERENCES partido_politico(id_partido)
        ON DELETE RESTRICT,
    CONSTRAINT uq_acta_partido UNIQUE (id_acta, id_partido)
);

-- 8. Padrón electoral nominal por mesa (Control de Asistencia)
CREATE TABLE IF NOT EXISTS padron_ciudadano (
    ci VARCHAR(15) PRIMARY KEY,
    nombres VARCHAR(50) NOT NULL,
    apellidos VARCHAR(50) NOT NULL,
    id_mesa INT NOT NULL,
    ha_votado BOOLEAN DEFAULT FALSE,
    hora_sufragio TIMESTAMP NULL,
    FOREIGN KEY (id_mesa) REFERENCES mesa(id_mesa)
        ON DELETE RESTRICT
);

-- 9. Registro unitario de papeletas extraídas del ánfora (Escrutinio 1 a 1)
-- Esta tabla NO tiene el CI del votante (garantiza el voto secreto)
-- tipo_voto: VALIDO, BLANCO, NULO
CREATE TABLE IF NOT EXISTS papeleta_escrutinio (
    id_papeleta SERIAL PRIMARY KEY,
    id_mesa INT NOT NULL,
    orden_extraccion INT NOT NULL,
    tipo_voto VARCHAR(20) NOT NULL,
    id_partido INT NULL,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_mesa) REFERENCES mesa(id_mesa)
        ON DELETE RESTRICT,
    FOREIGN KEY (id_partido) REFERENCES partido_politico(id_partido)
        ON DELETE RESTRICT,
    CONSTRAINT uq_mesa_orden UNIQUE (id_mesa, orden_extraccion)
);
