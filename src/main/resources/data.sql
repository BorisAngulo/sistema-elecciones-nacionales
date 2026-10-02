-- Ejecutar el archivo completo en MySQL Workbench sobre una base con schema.sql.
-- Requiere tablas vacías; no modifica una carga existente.
DROP PROCEDURE IF EXISTS cargar_datos_electorales;
DELIMITER $$
CREATE PROCEDURE cargar_datos_electorales()
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
INSERT INTO departamento (id_departamento, nombre) VALUES
(1, 'La Paz'),
(2, 'Cochabamba'),
(3, 'Santa Cruz'),
(4, 'Oruro'),
(5, 'Potosí'),
(6, 'Chuquisaca'),
(7, 'Tarija'),
(8, 'Beni'),
(9, 'Pando');

-- AUTO_INCREMENT se ajusta al insertar IDs explícitos.


-- INSERCIÓN DE TODOS LOS MUNICIPIOS DE BOLIVIA (340)

-- ============================================================================
-- 1. LA PAZ (87 Municipios)
-- ============================================================================
INSERT INTO municipio (id_departamento, nombre) VALUES
(1, 'Nuestra Señora de La Paz'),
(1, 'Palca'),
(1, 'Mecapaca'),
(1, 'Achocalla'),
(1, 'El Alto'),
(1, 'Achacachi'),
(1, 'Ancoraimes'),
(1, 'Huarina'),
(1, 'Santiago de Huata'),
(1, 'Huatajata'),
(1, 'Chua Cocani'),
(1, 'Corocoro'),
(1, 'Caquiaviri'),
(1, 'Calacoto'),
(1, 'Comanche'),
(1, 'Charaña'),
(1, 'Waldo Ballivián'),
(1, 'Nazacara de Pacajes'),
(1, 'Callapa'),
(1, 'Puerto Acosta'),
(1, 'Mocomoco'),
(1, 'Puerto Carabuco'),
(1, 'Humanata'),
(1, 'Escoma'),
(1, 'Chuma'),
(1, 'Ayata'),
(1, 'Aucapata'),
(1, 'Sorata'),
(1, 'Guanay'),
(1, 'Tacacoma'),
(1, 'Quiabaya'),
(1, 'Combaya'),
(1, 'Tipuani'),
(1, 'Mapiri'),
(1, 'Teoponte'),
(1, 'Apolo'),
(1, 'Pelechuco'),
(1, 'Viacha'),
(1, 'Guaqui'),
(1, 'Tiahuanaco'),
(1, 'Desaguadero'),
(1, 'San Andrés de Machaca'),
(1, 'Jesús de Machaca'),
(1, 'Taraco'),
(1, 'Luribay'),
(1, 'Sapahaqui'),
(1, 'Yaco'),
(1, 'Malla'),
(1, 'Cairoma'),
(1, 'Inquisivi'),
(1, 'Quime'),
(1, 'Cajuata'),
(1, 'Colquiri'),
(1, 'Ichoca'),
(1, 'Licoma Pampa'),
(1, 'Chulumani'),
(1, 'Irupana'),
(1, 'Yanacachi'),
(1, 'Palos Blancos'),
(1, 'La Asunta'),
(1, 'Pucarani'),
(1, 'Laja'),
(1, 'Batallas'),
(1, 'Puerto Pérez'),
(1, 'Sica Sica'),
(1, 'Umala'),
(1, 'Ayo Ayo'),
(1, 'Calamarca'),
(1, 'Patacamaya'),
(1, 'Colquencha'),
(1, 'Collana'),
(1, 'Coroico'),
(1, 'Coripata'),
(1, 'Ixiamas'),
(1, 'San Buenaventura'),
(1, 'Charazani (General Juan José Pérez)'),
(1, 'Curva'),
(1, 'Copacabana'),
(1, 'San Pedro de Tiquina'),
(1, 'Tito Yupanqui'),
(1, 'San Pedro de Curahuara'),
(1, 'Papel Pampa'),
(1, 'Chacarilla'),
(1, 'Santiago de Machaca'),
(1, 'Catacora'),
(1, 'Caranavi'),
(1, 'Alto Beni');

-- ============================================================================
-- 2. COCHABAMBA (47 Municipios)
-- ============================================================================
INSERT INTO municipio (id_departamento, nombre) VALUES
(2, 'Cochabamba (Cercado)'),
(2, 'Aiquile'),
(2, 'Pasorapa'),
(2, 'Omereque'),
(2, 'Ayopaya (Independencia)'),
(2, 'Morochata'),
(2, 'Cocapata'),
(2, 'Tarata'),
(2, 'Anzaldo'),
(2, 'Arbieto'),
(2, 'Sacabamba'),
(2, 'Arani'),
(2, 'Vacas'),
(2, 'Arque'),
(2, 'Tacopaya'),
(2, 'Capinota'),
(2, 'Santiváñez'),
(2, 'Sicaya'),
(2, 'Cliza'),
(2, 'Toco'),
(2, 'Tolata'),
(2, 'Quillacollo'),
(2, 'Sipe Sipe'),
(2, 'Tiquipaya'),
(2, 'Vinto'),
(2, 'Colcapirhua'),
(2, 'Sacaba'),
(2, 'Colomi'),
(2, 'Villa Tunari'),
(2, 'Tapacarí'),
(2, 'Totora'),
(2, 'Pojo'),
(2, 'Pocona'),
(2, 'Chimoré'),
(2, 'Puerto Villarroel'),
(2, 'Entre Ríos'),
(2, 'Mizque'),
(2, 'Vila Vila'),
(2, 'Alalay'),
(2, 'Punata'),
(2, 'Villa Rivero'),
(2, 'San Benito'),
(2, 'Tacachi'),
(2, 'Cuchumuela (Villa Gualberto Villarroel)'),
(2, 'Bolívar'),
(2, 'Tiraque'),
(2, 'Shinahota');

-- ============================================================================
-- 3. SANTA CRUZ (56 Municipios)
-- ============================================================================
INSERT INTO municipio (id_departamento, nombre) VALUES
(3, 'Santa Cruz de la Sierra'),
(3, 'Cotoca'),
(3, 'Ayacucho (Porongo)'),
(3, 'La Guardia'),
(3, 'El Torno'),
(3, 'Warnes'),
(3, 'Okinawa Uno'),
(3, 'San Ignacio de Velasco'),
(3, 'San Miguel de Velasco'),
(3, 'San Rafael'),
(3, 'San Matías'),
(3, 'Buena Vista'),
(3, 'San Carlos'),
(3, 'Yapacaní'),
(3, 'San Juan de Yapacaní'),
(3, 'San José de Chiquitos'),
(3, 'Pailón'),
(3, 'Roboré'),
(3, 'El Carmen Rivero Tórrez'),
(3, 'Portachuelo'),
(3, 'Santa Rosa del Sara'),
(3, 'Colpa Bélgica'),
(3, 'Lagunillas'),
(3, 'Charagua (Kereimba Iyaambae)'),
(3, 'Cabezas'),
(3, 'Cuevo'),
(3, 'Gutiérrez (Kereimba Iyaambae)'),
(3, 'Camiri'),
(3, 'Boyuibe'),
(3, 'Vallegrande'),
(3, 'Trigal'),
(3, 'Moro Moro'),
(3, 'Postrervalle'),
(3, 'Pucara'),
(3, 'Samaipata'),
(3, 'Pampa Grande'),
(3, 'Mairana'),
(3, 'Quirusillas'),
(3, 'Montero'),
(3, 'General Saavedra'),
(3, 'Mineros'),
(3, 'Fernández Alonso'),
(3, 'San Pedro'),
(3, 'Concepción'),
(3, 'San Javier'),
(3, 'San Ramón'),
(3, 'San Julián'),
(3, 'San Antonio de Lomerío'),
(3, 'Cuatro Cañadas'),
(3, 'San Matías de Chiquitos'),
(3, 'Puerto Suárez'),
(3, 'Puerto Quijarro'),
(3, 'Carmen Rivero Tórrez'),
(3, 'Ascensión de Guarayos'),
(3, 'Urubichá'),
(3, 'El Puente');

-- ============================================================================
-- 4. ORURO (35 Municipios)
-- ============================================================================
INSERT INTO municipio (id_departamento, nombre) VALUES
(4, 'Oruro'),
(4, 'Caracollo'),
(4, 'El Choro'),
(4, 'Soracachi (Paria)'),
(4, 'Challapata'),
(4, 'Santuario de Quillacas'),
(4, 'Corque'),
(4, 'Choquecota'),
(4, 'Curahuara de Carangas'),
(4, 'Turco'),
(4, 'Huachacalla'),
(4, 'Escara'),
(4, 'Cruz de Machacamarca'),
(4, 'Yunguyo del Litoral'),
(4, 'Esmeralda'),
(4, 'Poopó'),
(4, 'Pazña'),
(4, 'Antequera'),
(4, 'Huanuni'),
(4, 'Machacamarca'),
(4, 'Salinas de Garci Mendoza'),
(4, 'Pampa Aullagas'),
(4, 'Sabaya'),
(4, 'Coipasa'),
(4, 'Chipaya (Uru Chipaya)'),
(4, 'Toledo'),
(4, 'Eucaliptus'),
(4, 'Andamarca (Santiago de Andamarca)'),
(4, 'Belén de Andamarca'),
(4, 'Totora (San Pedro de Totora)'),
(4, 'Santiago de Huari'),
(4, 'La Rivera'),
(4, 'Todos Santos'),
(4, 'Carangas'),
(4, 'Huayllamarca');

-- ============================================================================
-- 5. POTOSÍ (41 Municipios)
-- ============================================================================
INSERT INTO municipio (id_departamento, nombre) VALUES
(5, 'Potosí'),
(5, 'Tinguipaya'),
(5, 'Yocalla'),
(5, 'Belén de Urmiri'),
(5, 'Uncía'),
(5, 'Chayanta'),
(5, 'Llallagua'),
(5, 'Chuquihuta'),
(5, 'Betanzos'),
(5, 'Chaqui'),
(5, 'Tacobamba'),
(5, 'Colquechaca'),
(5, 'Ravelo'),
(5, 'Pocoata'),
(5, 'Ocurí'),
(5, 'San Pedro de Buena Vista'),
(5, 'Toro Toro'),
(5, 'Cotagaita'),
(5, 'Vitichi'),
(5, 'Sacaca'),
(5, 'Caripuyo'),
(5, 'Tupiza'),
(5, 'Atocha'),
(5, 'Colcha K (Villa Martín)'),
(5, 'San Pedro de Quemes'),
(5, 'San Agustín'),
(5, 'Uyuni'),
(5, 'Tomave'),
(5, 'Porco'),
(5, 'Arampampa'),
(5, 'Acacio'),
(5, 'Llica'),
(5, 'Tahua'),
(5, 'Villazón'),
(5, 'San Pablo de Lípez'),
(5, 'Mojinete'),
(5, 'San Antonio de Esmoruco'),
(5, 'Cerdas'),
(5, 'Puna'),
(5, 'Caiza D'),
(5, 'Ckochas');

-- ============================================================================
-- 6. CHUQUISACA (29 Municipios)
-- ============================================================================
INSERT INTO municipio (id_departamento, nombre) VALUES
(6, 'Sucre'),
(6, 'Yotala'),
(6, 'Poroma'),
(6, 'Azurduy'),
(6, 'Tarvita'),
(6, 'Zudáñez'),
(6, 'Presto'),
(6, 'Mojocoya'),
(6, 'Icla'),
(6, 'Padilla'),
(6, 'Tomina'),
(6, 'Sopachuy'),
(6, 'Villa Alcalá'),
(6, 'El Villar'),
(6, 'Monteagudo'),
(6, 'Huacareta'),
(6, 'Tarabuco'),
(6, 'Yamparáez'),
(6, 'Camargo'),
(6, 'San Lucas'),
(6, 'Incahuasi'),
(6, 'Villa Charcas'),
(6, 'Villa Serrano'),
(6, 'Villa Abecia'),
(6, 'Culpina'),
(6, 'Las Carreras'),
(6, 'Muyupampa (Villa Vaca Guzmán)'),
(6, 'Huacaya'),
(6, 'Macharetí');

-- ============================================================================
-- 7. TARIJA (11 Municipios)
-- ============================================================================
INSERT INTO municipio (id_departamento, nombre) VALUES
(7, 'Tarija (Cercado)'),
(7, 'Padcaya'),
(7, 'Bermejo'),
(7, 'Yacuiba'),
(7, 'Caraparí'),
(7, 'Villamontes'),
(7, 'Uriondo (Concepción)'),
(7, 'Yunchará'),
(7, 'San Lorenzo'),
(7, 'El Puente'),
(7, 'Entre Ríos');

-- ============================================================================
-- 8. BENI (19 Municipios)
-- ============================================================================
INSERT INTO municipio (id_departamento, nombre) VALUES
(8, 'Trinidad'),
(8, 'San Javier'),
(8, 'Riberalta'),
(8, 'Guayaramerín'),
(8, 'Reyes'),
(8, 'San Borja'),
(8, 'Santa Rosa'),
(8, 'Rurrenabaque'),
(8, 'Santa Ana del Yacuma'),
(8, 'Exaltación'),
(8, 'San Ignacio de Moxos'),
(8, 'Loreto'),
(8, 'San Andrés'),
(8, 'San Joaquín'),
(8, 'San Ramón'),
(8, 'Puerto Siles'),
(8, 'Magdalena'),
(8, 'Baures'),
(8, 'Huacaraje');

-- ============================================================================
-- 9. PANDO (15 Municipios)
-- ============================================================================
INSERT INTO municipio (id_departamento, nombre) VALUES
(9, 'Cobija'),
(9, 'Porvenir'),
(9, 'Bolpebra'),
(9, 'Bella Flor'),
(9, 'Puerto Rico'),
(9, 'San Pedro'),
(9, 'Filadelfia'),
(9, 'Puerto Gonzalo Moreno'),
(9, 'San Lorenzo'),
(9, 'Sena'),
(9, 'Santa Rosa del Abuná'),
(9, 'Ingavi'),
(9, 'Nueva Esperanza'),
(9, 'Villa Nueva (Loma Alta)'),
(9, 'Santos Mercado');


-- ============================================================================
-- INSERCIÓN DE RECINTOS ELECTORALES (Colegios y Unidades Educativas reales)
-- Mapeados a las IDs generadas en la tabla 'municipio'
-- ============================================================================

-- 1. LA PAZ (id_municipio: 1 = La Paz, 5 = El Alto, 6 = Achacachi, 38 = Viacha, 86 = Caranavi)
INSERT INTO recinto (id_municipio, nombre) VALUES
(1, 'Colegio Nacional Simón Bolívar'),
(1, 'Colegio San Calixto'),
(1, 'Unidad Educativa del Ejército'),
(1, 'Colegio Nacional Ayacucho'),
(1, 'Unidad Educativa Hugo Dávila'),
(1, 'Colegio La Salle'),
(1, 'Liceo de Señoritas Venezuela'),
(5, 'Unidad Educativa Puerto de Mejillones'),
(5, 'Colegio Marcelo Quiroga Santa Cruz'),
(5, 'Unidad Educativa Fuerza Aérea Boliviana'),
(5, 'Colegio Franz Tamayo'),
(5, 'Unidad Educativa 12 de Octubre'),
(6, 'Colegio Mariscal Andrés de Santa Cruz (Achacachi)'),
(38, 'Unidad Educativa José Ballivián (Viacha)'),
(86, 'Colegio Nacional Bolivia (Caranavi)');

-- 1. COCHABAMBA (CERCADO) - ZONA CENTRO Y CASCO VIEJO
INSERT INTO recinto (id_municipio, nombre) VALUES
((SELECT id_municipio FROM municipio WHERE nombre = 'Cochabamba (Cercado)' AND id_departamento = 2), 'Colegio Nacional Sucre'),
((SELECT id_municipio FROM municipio WHERE nombre = 'Cochabamba (Cercado)' AND id_departamento = 2), 'Colegio Instituto Americano (Amerinst)'),
((SELECT id_municipio FROM municipio WHERE nombre = 'Cochabamba (Cercado)' AND id_departamento = 2), 'Colegio Don Bosco'),
((SELECT id_municipio FROM municipio WHERE nombre = 'Cochabamba (Cercado)' AND id_departamento = 2), 'Liceo de Señoritas Adela Zamudio'),
((SELECT id_municipio FROM municipio WHERE nombre = 'Cochabamba (Cercado)' AND id_departamento = 2), 'Colegio San Agustín'),
((SELECT id_municipio FROM municipio WHERE nombre = 'Cochabamba (Cercado)' AND id_departamento = 2), 'Colegio La Salle'),
((SELECT id_municipio FROM municipio WHERE nombre = 'Cochabamba (Cercado)' AND id_departamento = 2), 'Unidad Educativa Carrillo'),
((SELECT id_municipio FROM municipio WHERE nombre = 'Cochabamba (Cercado)' AND id_departamento = 2), 'Colegio Maryknoll'),
((SELECT id_municipio FROM municipio WHERE nombre = 'Cochabamba (Cercado)' AND id_departamento = 2), 'Colegio Abaroa'),
((SELECT id_municipio FROM municipio WHERE nombre = 'Cochabamba (Cercado)' AND id_departamento = 2), 'Colegio Bolívar');

-- 2. COCHABAMBA (CERCADO) - PREDIOS UNIVERSITARIOS UMSS Y ZONA ESTE
INSERT INTO recinto (id_municipio, nombre) VALUES
((SELECT id_municipio FROM municipio WHERE nombre = 'Cochabamba (Cercado)' AND id_departamento = 2), 'UMSS - Campus Central (Facultad de Tecnología)'),
((SELECT id_municipio FROM municipio WHERE nombre = 'Cochabamba (Cercado)' AND id_departamento = 2), 'UMSS - Facultad de Ciencias Económicas'),
((SELECT id_municipio FROM municipio WHERE nombre = 'Cochabamba (Cercado)' AND id_departamento = 2), 'UMSS - Facultad de Humanidades y Ciencias de la Educación'),
((SELECT id_municipio FROM municipio WHERE nombre = 'Cochabamba (Cercado)' AND id_departamento = 2), 'Colegio Federico Froebel'),
((SELECT id_municipio FROM municipio WHERE nombre = 'Cochabamba (Cercado)' AND id_departamento = 2), 'Unidad Educativa Buenas Nuevas (Zona Este)');

-- 3. COCHABAMBA (CERCADO) - ZONA NORTE Y CALA CALA
INSERT INTO recinto (id_municipio, nombre) VALUES
((SELECT id_municipio FROM municipio WHERE nombre = 'Cochabamba (Cercado)' AND id_departamento = 2), 'Colegio Alemán Federico Froebel (Cala Cala)'),
((SELECT id_municipio FROM municipio WHERE nombre = 'Cochabamba (Cercado)' AND id_departamento = 2), 'Colegio Irlandés'),
((SELECT id_municipio FROM municipio WHERE nombre = 'Cochabamba (Cercado)' AND id_departamento = 2), 'Colegio Tiquipaya / Juan XXIII (Límite Norte)'),
((SELECT id_municipio FROM municipio WHERE nombre = 'Cochabamba (Cercado)' AND id_departamento = 2), 'Unidad Educativa Franz Tamayo (Queru Queru)'),
((SELECT id_municipio FROM municipio WHERE nombre = 'Cochabamba (Cercado)' AND id_departamento = 2), 'Colegio Loyola de Fe y Alegría');

-- 4. COCHABAMBA (CERCADO) - ZONA SUR (Distritos 8, 9, 14, Alalay, Jaihuayco, Tamborada)
INSERT INTO recinto (id_municipio, nombre) VALUES
((SELECT id_municipio FROM municipio WHERE nombre = 'Cochabamba (Cercado)' AND id_departamento = 2), 'Unidad Educativa 27 de Mayo (Jaihuayco)'),
((SELECT id_municipio FROM municipio WHERE nombre = 'Cochabamba (Cercado)' AND id_departamento = 2), 'Unidad Educativa Cobija (Zona Sud)'),
((SELECT id_municipio FROM municipio WHERE nombre = 'Cochabamba (Cercado)' AND id_departamento = 2), 'Colegio René Barrientos Ortuño'),
((SELECT id_municipio FROM municipio WHERE nombre = 'Cochabamba (Cercado)' AND id_departamento = 2), 'Unidad Educativa San Antonio de Padua'),
((SELECT id_municipio FROM municipio WHERE nombre = 'Cochabamba (Cercado)' AND id_departamento = 2), 'Unidad Educativa Santo Domingo Savio'),
((SELECT id_municipio FROM municipio WHERE nombre = 'Cochabamba (Cercado)' AND id_departamento = 2), 'Colegio República de México (Villa Sebastián Pagador)'),
((SELECT id_municipio FROM municipio WHERE nombre = 'Cochabamba (Cercado)' AND id_departamento = 2), 'Unidad Educativa Valle Hermoso'),
((SELECT id_municipio FROM municipio WHERE nombre = 'Cochabamba (Cercado)' AND id_departamento = 2), 'Unidad Educativa Tamborada'),
((SELECT id_municipio FROM municipio WHERE nombre = 'Cochabamba (Cercado)' AND id_departamento = 2), 'Colegio Sebastián Pagador'),
((SELECT id_municipio FROM municipio WHERE nombre = 'Cochabamba (Cercado)' AND id_departamento = 2), 'Unidad Educativa Ushpa Ushpa');

-- 5. COCHABAMBA (CERCADO) - ZONA OESTE (Chimita, Coña Coña, Sarco)
INSERT INTO recinto (id_municipio, nombre) VALUES
((SELECT id_municipio FROM municipio WHERE nombre = 'Cochabamba (Cercado)' AND id_departamento = 2), 'Unidad Educativa Melchor Urquidi'),
((SELECT id_municipio FROM municipio WHERE nombre = 'Cochabamba (Cercado)' AND id_departamento = 2), 'Colegio San Antonio'),
((SELECT id_municipio FROM municipio WHERE nombre = 'Cochabamba (Cercado)' AND id_departamento = 2), 'Unidad Educativa Coña Coña'),
((SELECT id_municipio FROM municipio WHERE nombre = 'Cochabamba (Cercado)' AND id_departamento = 2), 'Colegio Urkupiña (Límite Coña Coña)');

-- 6. QUILLACOLLO (Eje Metropolitano Oeste)
INSERT INTO recinto (id_municipio, nombre) VALUES
((SELECT id_municipio FROM municipio WHERE nombre = 'Quillacollo' AND id_departamento = 2), 'Colegio Calama'),
((SELECT id_municipio FROM municipio WHERE nombre = 'Quillacollo' AND id_departamento = 2), 'Unidad Educativa Darío Montaño'),
((SELECT id_municipio FROM municipio WHERE nombre = 'Quillacollo' AND id_departamento = 2), 'Colegio Franz Tamayo (Quillacollo)'),
((SELECT id_municipio FROM municipio WHERE nombre = 'Quillacollo' AND id_departamento = 2), 'Unidad Educativa Cristina Prado'),
((SELECT id_municipio FROM municipio WHERE nombre = 'Quillacollo' AND id_departamento = 2), 'Colegio América');

-- 7. SACABA (Eje Metropolitano Este)
INSERT INTO recinto (id_municipio, nombre) VALUES
((SELECT id_municipio FROM municipio WHERE nombre = 'Sacaba' AND id_departamento = 2), 'Colegio Germán Busch'),
((SELECT id_municipio FROM municipio WHERE nombre = 'Sacaba' AND id_departamento = 2), 'Colegio Monseñor Ricardo Baccigaluppi'),
((SELECT id_municipio FROM municipio WHERE nombre = 'Sacaba' AND id_departamento = 2), 'Unidad Educativa Bicentenario (Huayllani)'),
((SELECT id_municipio FROM municipio WHERE nombre = 'Sacaba' AND id_departamento = 2), 'Colegio Simón Bolívar (El Abra)'),
((SELECT id_municipio FROM municipio WHERE nombre = 'Sacaba' AND id_departamento = 2), 'Unidad Educativa Leoncio Capriles (Chimorecito/Curubamba)');

-- 8. TIQUIPAYA Y COLCAPIRHUA
INSERT INTO recinto (id_municipio, nombre) VALUES
((SELECT id_municipio FROM municipio WHERE nombre = 'Tiquipaya' AND id_departamento = 2), 'Colegio San Miguel (Tiquipaya)'),
((SELECT id_municipio FROM municipio WHERE nombre = 'Tiquipaya' AND id_departamento = 2), 'Unidad Educativa Cuarto Centenario'),
((SELECT id_municipio FROM municipio WHERE nombre = 'Colcapirhua' AND id_departamento = 2), 'Colegio Daniel Salamanca (Colcapirhua)'),
((SELECT id_municipio FROM municipio WHERE nombre = 'Colcapirhua' AND id_departamento = 2), 'Unidad Educativa Sixto Montero');

-- 9. VINTO Y SIPE SIPE
INSERT INTO recinto (id_municipio, nombre) VALUES
((SELECT id_municipio FROM municipio WHERE nombre = 'Vinto' AND id_departamento = 2), 'Colegio Nacional Vinto'),
((SELECT id_municipio FROM municipio WHERE nombre = 'Vinto' AND id_departamento = 2), 'Unidad Educativa Marcelo Quiroga Santa Cruz'),
((SELECT id_municipio FROM municipio WHERE nombre = 'Sipe Sipe' AND id_departamento = 2), 'Colegio 18 de Mayo (Sipe Sipe)'),
((SELECT id_municipio FROM municipio WHERE nombre = 'Sipe Sipe' AND id_departamento = 2), 'Unidad Educativa Mallco Rancho');

-- 10. VALLE ALTO (Punata, Cliza, Arani, Tarata)
INSERT INTO recinto (id_municipio, nombre) VALUES
((SELECT id_municipio FROM municipio WHERE nombre = 'Punata' AND id_departamento = 2), 'Colegio Nacional Gualberto Villarroel (Punata)'),
((SELECT id_municipio FROM municipio WHERE nombre = 'Punata' AND id_departamento = 2), 'Unidad Educativa Insituto Manuel Ascencio Villarroel'),
((SELECT id_municipio FROM municipio WHERE nombre = 'Cliza' AND id_departamento = 2), 'Colegio Calatayud (Cliza)'),
((SELECT id_municipio FROM municipio WHERE nombre = 'Cliza' AND id_departamento = 2), 'Unidad Educativa Toco'),
((SELECT id_municipio FROM municipio WHERE nombre = 'Arani' AND id_departamento = 2), 'Colegio Nemecio Antonio Mariscal (Arani)'),
((SELECT id_municipio FROM municipio WHERE nombre = 'Tarata' AND id_departamento = 2), 'Colegio Esteban Arze (Tarata)');

-- 11. TRÓPICO DE COCHABAMBA (Villa Tunari, Shinahota, Chimoré, Puerto Villarroel, Entre Ríos)
INSERT INTO recinto (id_municipio, nombre) VALUES
((SELECT id_municipio FROM municipio WHERE nombre = 'Villa Tunari' AND id_departamento = 2), 'Unidad Educativa Villa Tunari'),
((SELECT id_municipio FROM municipio WHERE nombre = 'Villa Tunari' AND id_departamento = 2), 'Colegio Chipiriri'),
((SELECT id_municipio FROM municipio WHERE nombre = 'Shinahota' AND id_departamento = 2), 'Colegio Germán Busch (Shinahota)'),
((SELECT id_municipio FROM municipio WHERE nombre = 'Chimoré' AND id_departamento = 2), 'Colegio Chimoré'),
((SELECT id_municipio FROM municipio WHERE nombre = 'Puerto Villarroel' AND id_departamento = 2), 'Colegio Ivirgarzama'),
((SELECT id_municipio FROM municipio WHERE nombre = 'Entre Ríos' AND id_departamento = 2), 'Unidad Educativa Bulo Bulo');

-- 3. SANTA CRUZ (id_municipio: 135 = Santa Cruz de la Sierra, 140 = Warnes, 173 = Montero, 162 = Camiri, 148 = San Ignacio)
INSERT INTO recinto (id_municipio, nombre) VALUES
(135, 'Colegio Nacional Florida'),
(135, 'Colegio Alemán'),
(135, 'Unidad Educativa Gabriel René Moreno'),
(135, 'Colegio Santa Ana'),
(135, 'Colegio San Agustín'),
(135, 'Unidad Educativa Enrique Finot'),
(140, 'Colegio Salomón Rivero (Warnes)'),
(173, 'Colegio Marceliano Montero (Montero)'),
(173, 'Unidad Educativa San Silvestre (Montero)'),
(162, 'Colegio Gran Chaco (Camiri)'),
(148, 'Unidad Educativa San Ignacio de Loyola');

-- 4. ORURO (id_municipio: 191 = Oruro, 195 = Challapata, 209 = Huanuni)
INSERT INTO recinto (id_municipio, nombre) VALUES
(191, 'Colegio Nacional Simón Bolívar'),
(191, 'Colegio Nacional Antonio José de Sucre'),
(191, 'Colegio Anglo Americano'),
(191, 'Liceo Pantaleón Dalence'),
(195, 'Colegio Eduardo Abaroa (Challapata)'),
(209, 'Colegio Mixto Huanuni');

-- 5. POTOSÍ (id_municipio: 226 = Potosí, 232 = Llallagua, 247 = Tupiza, 252 = Uyuni)
INSERT INTO recinto (id_municipio, nombre) VALUES
(226, 'Colegio Nacional Pichincha'),
(226, 'Liceo de Señoritas Santa Rosa'),
(226, 'Colegio Franciscano'),
(232, 'Colegio Siglo XX (Llallagua)'),
(247, 'Colegio Nacional Sucre (Tupiza)'),
(252, 'Colegio Antonio Quijarro (Uyuni)');

-- 6. CHUQUISACA (id_municipio: 267 = Sucre, 281 = Monteagudo, 283 = Tarabuco, 285 = Camargo)
INSERT INTO recinto (id_municipio, nombre) VALUES
(267, 'Colegio Nacional Junín'),
(267, 'Colegio Sagrado Corazón'),
(267, 'Colegio Bernardo Monteagudo'),
(267, 'Liceo María Josefa Mujía'),
(281, 'Colegio Adhemar Carvajal (Monteagudo)'),
(283, 'Unidad Educativa Faustino Suárez (Tarabuco)'),
(285, 'Colegio 3 de Abril (Camargo)');

-- 7. TARIJA (id_municipio: 296 = Tarija Cercado, 298 = Bermejo, 299 = Yacuiba, 301 = Villamontes)
INSERT INTO recinto (id_municipio, nombre) VALUES
(296, 'Colegio Nacional San Luis'),
(296, 'Colegio Belgrano'),
(296, 'Unidad Educativa Santa Ana'),
(298, 'Colegio Antonio José de Sucre (Bermejo)'),
(299, 'Colegio Héroes del Chaco (Yacuiba)'),
(301, 'Colegio Ismael Montes (Villamontes)');

-- 8. BENI (id_municipio: 307 = Trinidad, 309 = Riberalta, 310 = Guayaramerín, 314 = Rurrenabaque)
INSERT INTO recinto (id_municipio, nombre) VALUES
(307, 'Colegio Nacional 6 de Agosto'),
(307, 'Unidad Educativa La Salle'),
(307, 'Colegio El Cedro'),
(309, 'Colegio Pedro Kramer (Riberalta)'),
(310, 'Colegio Misiones del Oriente (Guayaramerín)'),
(314, 'Colegio Rurrenabaque');

-- 9. PANDO (id_municipio: 326 = Cobija, 327 = Porvenir, 330 = Puerto Rico)
INSERT INTO recinto (id_municipio, nombre) VALUES
(326, 'Unidad Educativa Antonio Vaca Díez'),
(326, 'Colegio Héroes de la Distancia'),
(326, 'Colegio Simón Bolívar'),
(327, 'Unidad Educativa 27 de Mayo (Porvenir)'),
(330, 'Colegio Puerto Rico');


-- ============================================================================
-- INSERCIÓN DE MESAS ELECTORALES
-- Se utilizan subconsultas para vincular directamente a los recintos existentes.
-- ============================================================================

-- ----------------------------------------------------------------------------
-- A. COCHABAMBA - CERCADO (Centro, UMSS, Zona Norte, Zona Sur, Zona Oeste)
-- ----------------------------------------------------------------------------

-- Colegio Nacional Sucre (Centro)
-- Una mesa por recinto existente; las claves se obtienen de la tabla, sin búsquedas ambiguas por nombre.
INSERT INTO mesa(numero_mesa,id_recinto,cantidad_inscritos,estado)
SELECT ROW_NUMBER() OVER (ORDER BY id_recinto), id_recinto, 240, 'HABILITADA' FROM recinto;

-- Partidos ficticios para la práctica.
INSERT INTO partido_politico (id_partido, sigla, nombre_completo, candidato_presidente) VALUES
(
    1,
    'FIB',
    'Frente de Integración Boliviana',
    'Rodrigo Sebastián Albarracín Zenteno'
),
(
    2,
    'AVANZA',
    'Alianza por el Valle y Renovación Nacional',
    'Valeria Jimena Torrico Claure'
),
(
    3,
    'KALLPA',
    'Movimiento Autonómico Popular Pachakuti',
    'Wilfredo Condori Mamani'
),
(
    4,
    'CONCORDIA',
    'Convergencia Republicana y Democrática',
    'Ignacio Javier Montaño Antelo'
),
(
    5,
    'SUMA-Q',
    'Solidaridad Unida por el Medio Ambiente y Soberanía',
    'Carla Andrea Justiniano Ríos'
);






-- Padrón determinista: exactamente cantidad_inscritos por mesa, sin votos precargados.
-- Los CIs DEMO son ficticios y únicos; no representan personas reales.
INSERT INTO padron_ciudadano(ci,nombres,apellidos,id_mesa,ha_votado,hora_sufragio)
SELECT CONCAT('D',m.id_mesa,'-',n), CONCAT('Ciudadano ',n), 'Demostración', m.id_mesa, FALSE, NULL
FROM mesa m JOIN (
    SELECT u.n + 10*d.n + 100*c.n + 1 AS n
    FROM (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
          UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) u
    CROSS JOIN (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
          UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) d
    CROSS JOIN (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2) c
) numeros ON numeros.n <= m.cantidad_inscritos;

    COMMIT;
END$$
DELIMITER ;
CALL cargar_datos_electorales();
DROP PROCEDURE IF EXISTS cargar_datos_electorales;
