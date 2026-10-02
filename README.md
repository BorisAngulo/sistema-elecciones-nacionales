# Sistema Electoral Nacional — versión MySQL

Proyecto de Programación II: Java 21+, Maven 3.9+ y **MySQL Server 8.0.16 o superior**.
Probado con MySQL Server 8.0.46. Usa MySQL Connector/J 8.4.0 e InnoDB.
La interfaz sigue siendo la consola ampliada: padrón, asistencia, papeletas, cierre,
Ley 026, resultados y estadísticas.

## 1. Conexión en MySQL Workbench

En la ventana de configuración:

| Campo | Valor |
|---|---|
| Caption / Connection Name | Sistema Electoral |
| Database Type | MySQL |
| Connection Method | mysql / Standard TCP/IP |
| Host Name | localhost |
| Port | 3306, salvo que tu servidor utilice otro |
| User Name | root, o tu usuario de MySQL |
| Store Password | La contraseña real de esa cuenta |
| Default Schema | Déjalo vacío durante la primera conexión |

Description, Tags y Folder Path pueden dejarse como están.
Pulsa **Test Connection** y luego **OK**. Workbench es el cliente:
el servicio MySQL Server debe estar iniciado. No hay una contraseña universal de root.

## 2. Crear la base y cargar la demo

1. Abre la conexión.
2. En Workbench, usa **File → Open SQL Script**.
3. Abre **INSTALAR_DEMO_MYSQL.sql**, ubicado junto a este README.
4. Ejecuta **todo el archivo** con el botón del rayo para el script completo, sin seleccionar
   solo una sentencia. No ejecutes únicamente la línea del cursor.
5. Actualiza SCHEMAS y selecciona **elecciones_nacionales** como esquema predeterminado.

El archivo crea la base, las nueve tablas y una demo con una mesa, dos partidos
y los CIs DEMO-1 a DEMO-5. Los datos son ficticios.
Comprueba:

    USE elecciones_nacionales;
    SHOW TABLES;
    SELECT ci,nombres,id_mesa,ha_votado FROM padron_ciudadano;

Usa una base nueva. Si ya tienes una base con ese nombre y datos,
conserva esos datos y elige otro nombre en CREATE DATABASE, USE y DB_NAME.
La carga rechaza tablas pobladas; no se debe ejecutar una segunda vez ni usar
la opción de continuar ignorando errores.

Alternativa para una práctica grande: sobre una base nueva, ejecuta
src/main/resources/00_crear_base.sql, schema.sql y data.sql, en ese orden.
data.sql crea los catálogos geográficos, una mesa de 240 inscritos por recinto
y exactamente 240 ciudadanos ficticios por mesa. No combines data.sql y data_demo.sql.

Los archivos de carga usan DELIMITER y un procedimiento temporal con rollback
para mantener todos sus INSERT en una transacción. La cuenta necesita permisos
CREATE ROUTINE, ALTER ROUTINE y EXECUTE para esa carga, además de los permisos
de creación de tablas. root local normalmente dispone de ellos.
El esquema usa DDL de MySQL, que realiza commits implícitos: la atomicidad de la
carga de datos no equivale a una instalación completa del esquema transaccional.

## 3. Configurar Java

Abre la carpeta de este proyecto en IntelliJ y recarga pom.xml con Maven.
Copia .env.example a **.env** al lado de pom.xml, con los datos de tu conexión:

    DB_HOST=localhost
    DB_PORT=3306
    DB_NAME=elecciones_nacionales
    DB_USER=root
    DB_PASSWORD=tu_contraseña_real

No uses literalmente tu_contraseña_real. Escribe la contraseña con la que conectas
en Workbench y no la compartas. Si tu cuenta local no tiene contraseña, deja
DB_PASSWORD= vacío. No se incluye un .env con credenciales en esta entrega.

La conexión se configura para MySQL local, Unicode y la zona horaria local.
DB_URL es una opción avanzada que reemplaza host/puerto/base y sus parámetros.
No reutilices un .env con una URL jdbc:postgresql de la versión anterior.

## 4. Ejecutar

En IntelliJ abre src/main/java/bo/edu/electoral/ui/MainConsola.java y pulsa Run.
El directorio de trabajo debe ser la carpeta que contiene pom.xml y .env.

Con Maven disponible en la terminal:

    mvn test
    mvn compile exec:java

Maven descarga el driver y las dependencias. El JDK debe ser 21 o superior.
La base se prepara en Workbench; el programa Java se ejecuta desde IntelliJ o Maven.

## 5. Demostración de cinco votos

1. Opción 9: consulta los IDs de mesa y partidos.
2. Opción 1: busca DEMO-1.
3. Opción 2: registra asistencia para DEMO-1 hasta DEMO-5.
4. Opción 3: registra dos papeletas VALIDO para A, una VALIDO para B,
   una BLANCO y una NULO.
5. Opción 4: cierra la mesa y confirma escribiendo CERRAR.
6. Opción 5: A=2 (66,67%), B=1 (33,33%), blancos=1, nulos=1, asistentes=5.
7. Opciones 6 y 7: reporte departamental y estadísticas.

La segunda asistencia del mismo CI, una sexta papeleta, el cierre incompleto
y las papeletas sobre una mesa COMPUTADA o ANULADA se rechazan.

## Reglas y arquitectura

- Nueve modelos y DAOs con PreparedStatement y claves automáticas.
- Servicios: ValidadorActaService, ProcesoElectoralService y MotorElectoralLey026.
- ReporteElectoralDAO suma únicamente actas de mesas COMPUTADAS.
- Los porcentajes se calculan sobre votos válidos; blancos y nulos van por separado.
- Primera vuelta: más de 50%, o al menos 40% con al menos 10 puntos de ventaja.
  [Ley 026, art. 52.II, OEP](https://www.oep.org.bo/wp-content/uploads/2019/07/LEY_026.pdf).
- Sin votos válidos no se determina ganador. Los empates no se deciden por ID.
- Se identifica el cómputo parcial si quedan mesas HABILITADAS; las ANULADAS se excluyen.
- Una mesa puede tener una sola acta. El cierre guarda acta, detalles y estado COMPUTADA
  en una transacción. Un error revierte todo el cierre.
- Los servicios usan conexión propia, READ COMMITTED y FOR UPDATE sobre la mesa.
  Esto permite que los conteos posteriores al bloqueo vean los cambios confirmados
  de la operación anterior, incluso cuando hubo otra sesión esperando.
- El reporte nacional usa una instantánea REPEATABLE READ.
- La papeleta no contiene CI. La asistencia y el voto se almacenan separados.
- Preparación (opción 8): CRUD antes de comenzar la votación; después, consultas.
  Actas, detalles y papeletas se crean por el flujo electoral.
- Los DAOs siguen siendo persistencia de bajo nivel: no usarlos ni ejecutar SQL directo
  para saltarse los servicios durante la votación. Preparar datos antes de abrir
  sesiones electorales concurrentes.
- Proyecto académico sin login, API REST, Spring, OCR ni reportes PDF.

## Estadísticas con arreglos

Población: votos válidos totales por partido, incluidos partidos con cero votos.

- Frecuencias absoluta, relativa y acumuladas.
- Media, mediana y todas las modas; sin moda si todos los valores aparecen una vez.
- Percentiles: interpolación lineal en (n-1)*p/100.
- Varianza poblacional /n y muestral /(n-1), con al menos dos datos en la segunda.
- Desviación estándar y CV porcentual; CV indefinido cuando la media es cero.
- Chebyshev: k>1, garantía 1-1/k² e intervalo media ± k·desviación.
- Sin bibliotecas estadísticas; rechaza arreglos vacíos y valores no finitos.

## Pruebas

mvn test ejecuta las 19 pruebas unitarias sin requerir servidor.
Las 11 pruebas de integración se habilitan con variables de entorno.
Usa una instancia dedicada de pruebas con permisos CREATE/DROP DATABASE:

    $env:ELECTORAL_TEST_URL='jdbc:mysql://127.0.0.1:3306/?allowPublicKeyRetrieval=true'
    $env:ELECTORAL_TEST_USER='tu_usuario_de_pruebas'
    $env:ELECTORAL_TEST_PASSWORD='tu_contraseña'
    mvn test

Cada prueba crea una base prueba_<uuid> y elimina exclusivamente esa base.
No se usa ni vacía elecciones_nacionales. No habilites estas pruebas contra
un servidor de producción. VERIFICACION.md documenta la ejecución realizada.

## Cambios frente a la entrega PostgreSQL

Se adaptaron driver y URL JDBC, puerto, AUTO_INCREMENT, recuperación de claves,
scripts de carga, tipos fecha/hora, controles SQL y pruebas de integración.
No se realiza una migración automática de datos de una base PostgreSQL existente.
schema.sql crea tablas nuevas; IF NOT EXISTS no actualiza tablas antiguas.
No se incluye migration_001_validaciones.sql de PostgreSQL porque es incompatible
y los controles ya están incorporados en el nuevo esquema MySQL.

El informe de Google Docs anterior describe la versión PostgreSQL.
CAMBIOS_PARA_INFORME.md indica los ajustes técnicos necesarios para esta variante.

Referencias técnicas:
[Claves generadas en Connector/J](https://dev.mysql.com/doc/connector-j/en/connector-j-usagenotes-last-insert-id.html).
[Restricciones CHECK de MySQL](https://dev.mysql.com/doc/refman/8.0/en/create-table-check-constraints.html).
