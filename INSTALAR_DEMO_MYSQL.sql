

-- Ejecutar primero en MySQL Workbench.
CREATE DATABASE IF NOT EXISTS elecciones_nacionales
    CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs;
USE elecciones_nacionales;



-- 1. TABLA: Departamentos de Bolivia
CREATE TABLE IF NOT EXISTS departamento (
    id_departamento INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs;

-- 2. TABLA: Municipios
CREATE TABLE IF NOT EXISTS municipio (
    id_municipio INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    id_departamento INT NOT NULL,
    FOREIGN KEY (id_departamento) REFERENCES departamento(id_departamento)
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs;

-- 3. TABLA: Recintos Electorales
CREATE TABLE IF NOT EXISTS recinto (
    id_recinto INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    id_municipio INT NOT NULL,
    FOREIGN KEY (id_municipio) REFERENCES municipio(id_municipio)
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs;

-- 4. TABLA: Mesas Electorales
-- estado: HABILITADA, COMPUTADA, ANULADA
CREATE TABLE IF NOT EXISTS mesa (
    id_mesa INT AUTO_INCREMENT PRIMARY KEY,
    numero_mesa INT NOT NULL,
    id_recinto INT NOT NULL,
    cantidad_inscritos INT NOT NULL DEFAULT 240,
    estado VARCHAR(20) NOT NULL DEFAULT 'HABILITADA',
    FOREIGN KEY (id_recinto) REFERENCES recinto(id_recinto)
        ON DELETE RESTRICT,
    CONSTRAINT ck_mesa_valida CHECK (cantidad_inscritos >= 0 AND numero_mesa > 0 AND estado IN ('HABILITADA','COMPUTADA','ANULADA'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs;

-- 5. TABLA: Partidos / Alianzas Políticas
CREATE TABLE IF NOT EXISTS partido_politico (
    id_partido INT AUTO_INCREMENT PRIMARY KEY,
    sigla VARCHAR(20) NOT NULL UNIQUE,
    nombre_completo VARCHAR(150) NOT NULL,
    candidato_presidente VARCHAR(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs;

-- 6. TABLA: Actas de Escrutinio por Mesa
CREATE TABLE IF NOT EXISTS acta (
    id_acta INT AUTO_INCREMENT PRIMARY KEY,
    id_mesa INT NOT NULL UNIQUE,
    votos_blancos INT NOT NULL DEFAULT 0,
    votos_nulos INT NOT NULL DEFAULT 0,
    total_ciudadanos_votaron INT NOT NULL DEFAULT 0,
    fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_mesa) REFERENCES mesa(id_mesa)
        ON DELETE RESTRICT,
    CONSTRAINT ck_acta_conteos CHECK (votos_blancos >= 0 AND votos_nulos >= 0 AND total_ciudadanos_votaron >= 0 AND CAST(votos_blancos AS SIGNED) + votos_nulos <= total_ciudadanos_votaron)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs;

-- 7. TABLA: Detalle de Votos por Partido Político
CREATE TABLE IF NOT EXISTS detalle_voto_partido (
    id_detalle INT AUTO_INCREMENT PRIMARY KEY,
    id_acta INT NOT NULL,
    id_partido INT NOT NULL,
    votos_validos INT NOT NULL DEFAULT 0,
    FOREIGN KEY (id_acta) REFERENCES acta(id_acta)
        ON DELETE RESTRICT,
    FOREIGN KEY (id_partido) REFERENCES partido_politico(id_partido)
        ON DELETE RESTRICT,
    CONSTRAINT uq_acta_partido UNIQUE (id_acta, id_partido),
    CONSTRAINT ck_detalle_no_negativo CHECK (votos_validos >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs;

-- 8. Padrón electoral nominal por mesa (Control de Asistencia)
CREATE TABLE IF NOT EXISTS padron_ciudadano (
    ci VARCHAR(15) PRIMARY KEY,
    nombres VARCHAR(50) NOT NULL,
    apellidos VARCHAR(50) NOT NULL,
    id_mesa INT NOT NULL,
    ha_votado BOOLEAN NOT NULL DEFAULT FALSE,
    hora_sufragio DATETIME NULL,
    FOREIGN KEY (id_mesa) REFERENCES mesa(id_mesa)
        ON DELETE RESTRICT,
    CONSTRAINT ck_asistencia_hora CHECK (ha_votado IN (0,1) AND ((ha_votado=1 AND hora_sufragio IS NOT NULL) OR (ha_votado=0 AND hora_sufragio IS NULL)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs;

-- 9. Registro unitario de papeletas extraídas del ánfora (Escrutinio 1 a 1)
-- Esta tabla NO tiene el CI del votante (garantiza el voto secreto)
-- tipo_voto: VALIDO, BLANCO, NULO
CREATE TABLE IF NOT EXISTS papeleta_escrutinio (
    id_papeleta INT AUTO_INCREMENT PRIMARY KEY,
    id_mesa INT NOT NULL,
    orden_extraccion INT NOT NULL,
    tipo_voto VARCHAR(20) NOT NULL,
    id_partido INT NULL,
    fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_mesa) REFERENCES mesa(id_mesa)
        ON DELETE RESTRICT,
    FOREIGN KEY (id_partido) REFERENCES partido_politico(id_partido)
        ON DELETE RESTRICT,
    CONSTRAINT uq_mesa_orden UNIQUE (id_mesa, orden_extraccion),
    CONSTRAINT ck_papeleta_valida CHECK (orden_extraccion > 0 AND ((tipo_voto='VALIDO' AND id_partido IS NOT NULL) OR (tipo_voto IN ('BLANCO','NULO') AND id_partido IS NULL)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_as_cs;


-- Ejecutar el archivo completo en MySQL Workbench sobre una base con schema.sql.
-- Requiere tablas vacías; no modifica una carga existente.
DROP PROCEDURE IF EXISTS cargar_demo_electoral;
DELIMITER $$
CREATE PROCEDURE cargar_demo_electoral()
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    START TRANSACTION;
    IF EXISTS(SELECT 1 FROM departamento) OR EXISTS(SELECT 1 FROM partido_politico) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='La carga requiere tablas vacías. Use una base nueva.';
    END IF;
INSERT INTO departamento(nombre) VALUES('Departamento Demo');
INSERT INTO municipio(nombre,id_departamento)
SELECT 'Municipio Demo',id_departamento FROM departamento WHERE nombre='Departamento Demo';
INSERT INTO recinto(nombre,id_municipio)
SELECT 'Recinto Demo',id_municipio FROM municipio WHERE nombre='Municipio Demo';
INSERT INTO mesa(numero_mesa,id_recinto,cantidad_inscritos)
SELECT 1,id_recinto,5 FROM recinto WHERE nombre='Recinto Demo';
INSERT INTO partido_politico(sigla,nombre_completo,candidato_presidente)
VALUES ('A','Partido Demo A','Candidatura A'),('B','Partido Demo B','Candidatura B');
INSERT INTO padron_ciudadano(ci,nombres,apellidos,id_mesa)
SELECT CONCAT('DEMO-',n),CONCAT('Ciudadano ',n),'Prueba',m.id_mesa
FROM mesa m CROSS JOIN (SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5) numeros;

    COMMIT;
END$$
DELIMITER ;
CALL cargar_demo_electoral();
DROP PROCEDURE IF EXISTS cargar_demo_electoral;
