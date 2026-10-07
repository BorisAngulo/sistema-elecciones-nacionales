# Sistema Electoral Nacional

Proyecto de apoyo para Programación II. Incluye el **modelo de datos**, el **esquema PostgreSQL**, la **conexión JDBC**, los **DAOs** y un **menú de consola** para crear, listar, editar y eliminar registros. Los estudiantes implementan la lógica electoral (validaciones, Ley 026, estadísticas) y la interfaz definitiva.

## Qué ya está listo

| Pieza | Ubicación | Para qué sirve |
| --- | --- | --- |
| Modelos (POJO con getters/setters) | `src/main/java/bo/edu/electoral/model/` | Representan las 9 tablas. |
| Conexión singleton JDBC | `src/main/java/bo/edu/electoral/config/DatabaseConnection.java` | Abre una conexión a PostgreSQL leyendo `.env`. |
| DAOs (insert, update, findById, findAll, delete) | `src/main/java/bo/edu/electoral/dao/` | Persistencia con SQL y `PreparedStatement`. |
| Menú de consola | `src/main/java/bo/edu/electoral/ui/MainConsola.java` | Alta, baja, consulta, edición y simulación de voto. |
| Simulación de votación | `src/main/java/bo/edu/electoral/service/SimuladorVotacionService.java` | Valida CI, registra papeleta (sin CI) y marca `ha_votado`. |
| Cierre y escrutinio de mesa | `src/main/java/bo/edu/electoral/service/CierreMesaService.java` | Consolida papeletas en un acta y detalle por partido, y marca la mesa como computada en una transacción. |
| Validación del acta | `src/main/java/bo/edu/electoral/service/ValidadorActaService.java` | Comprueba que votos y papeletas coincidan, que no se supere el número de inscritos y que papeletas y ciudadanos que votaron coincidan. |
| Resultados y gráficos | `src/main/java/bo/edu/electoral/service/ResultadoSimulacionService.java` y `src/main/java/bo/edu/electoral/ui/GraficoBarrasVentana.java` | Muestra votos válidos, blancos, nulos y totales; permite filtrar el gráfico por departamento. |
| Resultados oficiales | `src/main/java/bo/edu/electoral/service/ResultadoElectoralService.java` | Suma votos de actas por partido a nivel nacional y por departamento, con porcentajes sobre votos válidos. |
| Resultado final presidencial | `src/main/java/bo/edu/electoral/service/ResultadoFinalService.java` | Aplica la Ley 026 al cómputo nacional para determinar ganador en primera vuelta o segunda vuelta. |
| Esquema de base de datos | `src/main/resources/schema.sql` | Crea las tablas en PostgreSQL. |
| Variables de entorno de ejemplo | `.env.example` | Plantilla de credenciales. Se copia a `.env`. |

## Requisitos

- JDK 21 o superior
- Maven 3.9+ (o el que traiga IntelliJ)
- PostgreSQL 14 o superior, en ejecución
- IntelliJ IDEA (u otro IDE Java)

## Cómo hacer funcionar lo que hay

### 1. Clonar o abrir el proyecto

Abre la carpeta del proyecto en IntelliJ y espera a que recargue Maven (`pom.xml`). Debe bajar el driver `org.postgresql:postgresql`.

### 2. Crear el archivo `.env`

En la **raíz del proyecto** (junto a `pom.xml`):

```bash
cp .env.example .env
```

Edita `.env` con tu usuario y contraseña reales de PostgreSQL:

```env
DB_HOST=localhost
DB_PORT=5432
DB_NAME=elecciones_nacionales
DB_USER=postgres
DB_PASSWORD=tu_contraseña
```

`DatabaseConnection` busca `.env` en el directorio de trabajo (`user.dir`). En IntelliJ suele ser la raíz del proyecto. Si al conectar dice que no encuentra `.env`, revisa Run → Edit Configurations → Working directory.

### 3. Crear la base de datos

```bash
createdb elecciones_nacionales
```

Si `createdb` no está en el PATH, desde `psql`:

```sql
CREATE DATABASE elecciones_nacionales;
```

### 4. Crear las tablas

```bash
psql -d elecciones_nacionales -f src/main/resources/schema.sql
```

Comprueba que existan las 9 tablas:

```bash
psql -d elecciones_nacionales -c '\dt'
```

Deben aparecer: `departamento`, `municipio`, `recinto`, `mesa`, `partido_politico`, `acta`, `detalle_voto_partido`, `padron_ciudadano`, `papeleta_escrutinio`.

#### 4.1 Cargar datos

```bash
psql -U postgres -d elecciones_100000 -v ON_ERROR_STOP=1 -f src/main/resources/data.sql
psql -U postgres -d elecciones_100000 -v ON_ERROR_STOP=1 -f src/main/resources/simulacion.sql
```

Ejecuta ambos archivos una sola vez en una base vacía, después de cargar `schema.sql`: `data.sql` crea los datos base y `simulacion.sql` genera 100.000 ciudadanos distribuidos entre las mesas de los nueve departamentos y simula su asistencia y voto. No ejecutes `simulacion.sql` otra vez sobre una base que ya tenga padrón o papeletas. `-v ON_ERROR_STOP=1` detiene la carga si ocurre un error.

### 5. Ejecutar el menú de consola

En IntelliJ: abre `MainConsola.java` y pulsa Run (el working directory debe ser la raíz del proyecto).

Por terminal, desde la raíz:

```bash
mvn compile exec:java
```

Si conecta bien verás `Conectado a PostgreSQL.` y el menú. Ahí puedes listar, buscar, crear, editar y eliminar en las 9 tablas. La opción **10. Simular votación** pide un CI, lo valida y registra el voto. La opción **11. Resultados totales** cuenta las papeletas (sin cerrar actas) y abre un gráfico de barras.

El orden de carga por claves foráneas es: departamento → municipio → recinto → mesa → padrón / partidos → actas, detalles y papeletas.

La opción **13. Cerrar mesa e iniciar escrutinio oficial** permite cerrar una mesa individual o todas las mesas habilitadas con papeletas. Cada cierre consolida las papeletas en un acta y sus detalles por partido, y marca la mesa como `COMPUTADA` para impedir nuevos votos; las mesas sin papeletas se informan y se omiten. La opción **12. Resultados oficiales por partido y departamento** usa esos detalles; cada porcentaje se calcula sobre los votos válidos del ámbito mostrado (nacional o departamento). La opción **14. Resultado final y conclusión electoral** muestra el informe y aplica la Ley 026 al cómputo nacional: más del 50% de los votos válidos, o al menos 40% con una ventaja mínima de 10 puntos porcentuales sobre la segunda candidatura. Si no se cumple ninguno de esos criterios, determina una segunda vuelta entre las dos candidaturas más votadas. La opción **11** sigue mostrando el conteo de papeletas de la simulación.

Si falla:

- PostgreSQL no está corriendo
- La base `elecciones_nacionales` no existe o no se ejecutó `schema.sql`
- Usuario o contraseña en `.env` no coinciden
- El working directory no es la raíz del proyecto (no encuentra `.env`)

Cada DAO usa `DatabaseConnection.getConnection()`. Ejemplo:

```java
Departamento d = new Departamento();
d.setNombre("La Paz");
new DepartamentoDAO().insert(d);
```

## Modelos disponibles

Cada clase mapea una tabla. Los IDs de clave foránea van como `int` y, si el DAO hace join, también como objeto relacionado.

| Tabla | Clase |
| --- | --- |
| `departamento` | `Departamento` |
| `municipio` | `Municipio` |
| `recinto` | `Recinto` |
| `mesa` | `Mesa` |
| `partido_politico` | `PartidoPolitico` |
| `acta` | `Acta` |
| `detalle_voto_partido` | `DetalleVoto` |
| `padron_ciudadano` | `PadronCiudadano` |
| `papeleta_escrutinio` | `PapeletaEscrutinio` |

Notas:

- `Mesa` usa estados `HABILITADA`, `COMPUTADA`, `ANULADA`.
- `PapeletaEscrutinio` usa tipos `VALIDO`, `BLANCO`, `NULO`. `idPartido` es `Integer` (puede ser `null` en blanco o nulo).
- `papeleta_escrutinio` **no guarda el CI**: el voto es secreto.

## Qué falta realizar (trabajo de los estudiantes)

```text
src/main/java/bo/edu/electoral/
├── config/DatabaseConnection.java       ← listo
├── model/                               ← listo
├── dao/                                 ← listo (CRUD por tabla)
│   └── ReporteElectoralDAO.java         ← FALTA (totales GROUP BY)
├── service/
│   ├── SimuladorVotacionService.java    ← listo
│   ├── ValidadorActaService.java       ← listo
│   └── MotorElectoralLey026.java        ← FALTA
├── stats/                               ← FALTA
│   ├── DistribucionFrecuencia.java
│   ├── MedidasPosicion.java
│   └── MedidasDispersion.java
└── ui/MainConsola.java                  ← listo (menú de datos; no es la UI final)
```

El menú actual mantiene tablas, simula votaciones, cierra mesas y muestra resultados agregados; el servicio de cierre, la validación del acta al cerrar una mesa y las reglas de la Ley 026 están implementados. Otras reglas descritas abajo siguen pendientes.

### Capa service

- La validación del acta se ejecuta al cerrar una mesa: blancos + nulos + válidos deben coincidir con las papeletas, no superar inscritos y coincidir con la cantidad de ciudadanos marcados como votantes (`ha_votado`).
- Un CI no puede votar dos veces.
- No escrutar si la mesa está `ANULADA` o `COMPUTADA`.
- El acta es 1 a 1 con la mesa.
- **Ley 026 (presidente):** gana en primera vuelta con más del 50% de los votos válidos, o con al menos el 40% y 10 puntos porcentuales o más de diferencia sobre el segundo. La lógica está en `ResultadoFinalService`; si no se cumple ninguno de los criterios, hay segunda vuelta.

### Capa stats

Algoritmos con arreglos (sin librerías estadísticas): frecuencias, media, mediana, moda, percentiles, varianza, desviación estándar, coeficiente de variación, Chebyshev.

### Interfaz definitiva

El `MainConsola` actual es un mantenimiento de datos. Pueden reemplazarlo o ampliarlo con GUI (JavaFX/Swing) y estos flujos:

1. Padrón: buscar CI, marcar asistencia (`ha_votado`, `hora_sufragio`).
2. Escrutinio 1 a 1: registrar cada papeleta.
3. Cierre de mesa: generar `acta` y `detalle_voto_partido`.
4. Resultados: tabla de totales y porcentajes.
5. La aplicación de la Ley 026 al cómputo nacional está disponible en la opción 14.

### Pruebas (opcional pero recomendado)

```text
src/test/java/bo/edu/electoral/
├── MotorElectoralTest.java    casos de 1.ª y 2.ª vuelta
└── EstadisticaTest.java       verificar fórmulas
```

## Fuera del alcance de esta entrega docente

No viene implementado (y no hace falta para el curso): login de usuarios, Spring, JPA/Hibernate, API REST, OCR de actas ni reportes PDF.
