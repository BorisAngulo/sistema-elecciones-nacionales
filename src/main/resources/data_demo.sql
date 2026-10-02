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
