-- Ejecutar en la base elecciones_100000, despues de cargar schema.sql y los datos base.
-- Genera 100,000 ciudadanos y simula el sufragio de aproximadamente el 88%.
-- No se puede volver a ejecutar sobre una base que ya tenga padron o papeletas.

BEGIN;

DO $$
DECLARE
    departamentos_con_mesas INT;
    total_mesas INT;
    total_partidos INT;
BEGIN
    IF EXISTS (SELECT 1 FROM padron_ciudadano)
       OR EXISTS (SELECT 1 FROM papeleta_escrutinio)
       OR EXISTS (SELECT 1 FROM acta)
       OR EXISTS (SELECT 1 FROM detalle_voto_partido) THEN
        RAISE EXCEPTION 'La simulacion requiere tablas padron_ciudadano, papeleta_escrutinio, acta y detalle_voto_partido vacias.';
    END IF;

    SELECT COUNT(DISTINCT dep.id_departamento), COUNT(DISTINCT me.id_mesa)
    INTO departamentos_con_mesas, total_mesas
    FROM departamento dep
    JOIN municipio mun ON mun.id_departamento = dep.id_departamento
    JOIN recinto rec ON rec.id_municipio = mun.id_municipio
    JOIN mesa me ON me.id_recinto = rec.id_recinto;

    IF departamentos_con_mesas < 9 THEN
        RAISE EXCEPTION 'Se necesitan mesas en los nueve departamentos; solo se encontraron mesas en % departamentos.',
            departamentos_con_mesas;
    END IF;

    IF total_mesas = 0 THEN
        RAISE EXCEPTION 'No hay mesas disponibles para distribuir a los votantes.';
    END IF;

    SELECT COUNT(*) INTO total_partidos FROM partido_politico;
    IF total_partidos < 5 THEN
        RAISE EXCEPTION 'Se necesitan al menos cinco partidos para ejecutar esta simulacion; se encontraron %.',
            total_partidos;
    END IF;
END $$;

UPDATE mesa
SET estado = 'HABILITADA';

WITH mesas AS (
    SELECT
        me.id_mesa,
        dep.id_departamento,
        ROW_NUMBER() OVER (ORDER BY dep.id_departamento, me.numero_mesa, me.id_mesa) AS orden,
        COUNT(*) OVER () AS total_mesas
    FROM mesa me
    JOIN recinto rec ON rec.id_recinto = me.id_recinto
    JOIN municipio mun ON mun.id_municipio = rec.id_municipio
    JOIN departamento dep ON dep.id_departamento = mun.id_departamento
)
INSERT INTO padron_ciudadano (ci, nombres, apellidos, id_mesa, ha_votado, hora_sufragio)
SELECT
    (3000000 + serie.i)::TEXT || '-' || mesas.id_departamento,
    (ARRAY[
        'Juan Carlos', 'Carlos Ramiro', 'Luis Alberto', 'Jose Ernesto', 'Miguel Angel',
        'Maria Elena', 'Ana Patricia', 'Carla Andrea', 'Paola Jimena', 'Rosa Luz',
        'Rodrigo Gonzalo', 'Fernando Javier', 'Alvaro Marcelo', 'Jhonny Grover', 'Wilfredo'
    ])[1 + FLOOR(RANDOM() * 15)::INT],
    (ARRAY[
        'Mamani Quispe', 'Flores Choque', 'Condori Yujra', 'Vargas Mendoza', 'Fernandez Rios',
        'Torrico Montano', 'Gutierrez Paz', 'Rojas Morales', 'Quisbert Huanca', 'Apaza Ticona',
        'Camacho Arispe', 'Antelo Aguilera', 'Suarez Justiniano', 'Ribera Melgar', 'Claure Zenteno'
    ])[1 + FLOOR(RANDOM() * 15)::INT],
    mesas.id_mesa,
    FALSE,
    NULL
FROM GENERATE_SERIES(1, 100000) AS serie(i)
JOIN mesas
    ON mesas.orden = MOD(serie.i - 1, mesas.total_mesas) + 1;

WITH asistencia AS MATERIALIZED (
    SELECT ci, RANDOM() AS probabilidad_asistencia, RANDOM() AS hora_aleatoria
    FROM padron_ciudadano
)
UPDATE padron_ciudadano ciudadano
SET ha_votado = asistencia.probabilidad_asistencia < 0.88,
    hora_sufragio = CASE
        WHEN asistencia.probabilidad_asistencia < 0.88
            THEN TIMESTAMP '2026-10-18 08:00:00'
                + asistencia.hora_aleatoria * INTERVAL '510 minutes'
        ELSE NULL
    END
FROM asistencia
WHERE ciudadano.ci = asistencia.ci;

UPDATE mesa me
SET cantidad_inscritos = padron.total_inscritos
FROM (
    SELECT id_mesa, COUNT(*)::INT AS total_inscritos
    FROM padron_ciudadano
    GROUP BY id_mesa
) padron
WHERE me.id_mesa = padron.id_mesa;

WITH partidos AS MATERIALIZED (
    SELECT id_partido, ROW_NUMBER() OVER (ORDER BY id_partido) AS orden
    FROM partido_politico
),
votantes AS MATERIALIZED (
    SELECT
        id_mesa,
        ROW_NUMBER() OVER (PARTITION BY id_mesa ORDER BY ci) AS orden_extraccion
    FROM padron_ciudadano
    WHERE ha_votado
),
azar AS MATERIALIZED (
    SELECT
        id_mesa,
        orden_extraccion,
        RANDOM() AS tipo_aleatorio,
        RANDOM() AS partido_aleatorio
    FROM votantes
),
votos AS (
    SELECT
        id_mesa,
        orden_extraccion,
        CASE
            WHEN tipo_aleatorio < 0.04 THEN 'BLANCO'
            WHEN tipo_aleatorio < 0.09 THEN 'NULO'
            ELSE 'VALIDO'
        END AS tipo_voto,
        CASE
            WHEN tipo_aleatorio < 0.09 THEN NULL
            WHEN partido_aleatorio < 0.30 THEN 1
            WHEN partido_aleatorio < 0.57 THEN 2
            WHEN partido_aleatorio < 0.74 THEN 3
            WHEN partido_aleatorio < 0.88 THEN 4
            ELSE 5
        END AS orden_partido
    FROM azar
)
INSERT INTO papeleta_escrutinio (
    id_mesa,
    orden_extraccion,
    tipo_voto,
    id_partido,
    fecha_registro
)
SELECT
    votos.id_mesa,
    votos.orden_extraccion,
    votos.tipo_voto,
    partidos.id_partido,
    TIMESTAMP '2026-10-18 17:00:00'
        + votos.orden_extraccion * INTERVAL '15 seconds'
FROM votos
LEFT JOIN partidos ON partidos.orden = votos.orden_partido;

DO $$
DECLARE
    total_ciudadanos INT;
    total_papeletas INT;
    total_votantes INT;
BEGIN
    SELECT COUNT(*) INTO total_ciudadanos FROM padron_ciudadano;
    SELECT COUNT(*) INTO total_papeletas FROM papeleta_escrutinio;
    SELECT COUNT(*) INTO total_votantes FROM padron_ciudadano WHERE ha_votado;

    IF total_ciudadanos <> 100000 OR total_papeletas <> total_votantes THEN
        RAISE EXCEPTION 'La simulacion no paso la validacion: ciudadanos %, votantes %, papeletas %.',
            total_ciudadanos, total_votantes, total_papeletas;
    END IF;
END $$;

COMMIT;

WITH resumen_padron AS (
    SELECT
        dep.id_departamento,
        COUNT(ci.ci) AS ciudadanos_inscritos,
        COUNT(ci.ci) FILTER (WHERE ci.ha_votado) AS ciudadanos_que_votaron
    FROM departamento dep
    JOIN municipio mun ON mun.id_departamento = dep.id_departamento
    JOIN recinto rec ON rec.id_municipio = mun.id_municipio
    JOIN mesa me ON me.id_recinto = rec.id_recinto
    LEFT JOIN padron_ciudadano ci ON ci.id_mesa = me.id_mesa
    GROUP BY dep.id_departamento
),
resumen_papeletas AS (
    SELECT dep.id_departamento, COUNT(pap.id_papeleta) AS papeletas_registradas
    FROM departamento dep
    JOIN municipio mun ON mun.id_departamento = dep.id_departamento
    JOIN recinto rec ON rec.id_municipio = mun.id_municipio
    JOIN mesa me ON me.id_recinto = rec.id_recinto
    LEFT JOIN papeleta_escrutinio pap ON pap.id_mesa = me.id_mesa
    GROUP BY dep.id_departamento
)
SELECT
    dep.nombre AS departamento,
    padron.ciudadanos_inscritos,
    padron.ciudadanos_que_votaron,
    papeletas.papeletas_registradas
FROM departamento dep
JOIN resumen_padron padron ON padron.id_departamento = dep.id_departamento
JOIN resumen_papeletas papeletas ON papeletas.id_departamento = dep.id_departamento
ORDER BY dep.id_departamento;
