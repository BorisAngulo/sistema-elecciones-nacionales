# Sistema Electoral Nacional

Proyecto de apoyo para Programación II. Incluye el **modelo de datos**, el **esquema PostgreSQL**, la **conexión JDBC**, los **DAOs** y un **menú de consola** para crear, listar, editar y eliminar registros. Los estudiantes implementan la lógica electoral (validaciones, Ley 026, estadísticas) y la interfaz definitiva.

## Qué ya está listo

| Pieza | Ubicación | Para qué sirve |
| --- | --- | --- |
| Modelos (POJO con getters/setters) | `src/main/java/bo/edu/electoral/model/` | Representan las 9 tablas. |
| Conexión singleton JDBC | `src/main/java/bo/edu/electoral/config/DatabaseConnection.java` | Abre una conexión a PostgreSQL leyendo `.env`. |
| DAOs (insert, update, findById, findAll, delete) | `src/main/java/bo/edu/electoral/dao/` | Persistencia con SQL y `PreparedStatement`. |
| Menú de consola | `src/main/java/bo/edu/electoral/ui/MainConsola.java` | Alta, baja, consulta y edición al ejecutar `main`. |
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
psql -U postgres -d elecciones_nacionales -f data.sql
```

### 5. Ejecutar el menú de consola

En IntelliJ: abre `MainConsola.java` y pulsa Run (el working directory debe ser la raíz del proyecto).

Por terminal, desde la raíz:

```bash
mvn compile exec:java
```

Si conecta bien verás `Conectado a PostgreSQL.` y el menú. Ahí puedes listar, buscar, crear, editar y eliminar en las 9 tablas.

El orden de carga por claves foráneas es: departamento → municipio → recinto → mesa → padrón / partidos → actas, detalles y papeletas.

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
├── service/                             ← FALTA
│   ├── ValidadorActaService.java
│   └── MotorElectoralLey026.java
├── stats/                               ← FALTA
│   ├── DistribucionFrecuencia.java
│   ├── MedidasPosicion.java
│   └── MedidasDispersion.java
└── ui/MainConsola.java                  ← listo (menú de datos; no es la UI final)
```

El menú actual solo mantiene tablas. No valida actas, no aplica la Ley 026 ni muestra resultados agregados.

### Capa service

- Validar un acta: blancos + nulos + votos válidos no deben superar inscritos; el total de papeletas debe coincidir con quienes votaron (`ha_votado`).
- Un CI no puede votar dos veces.
- No escrutar si la mesa está `ANULADA` o `COMPUTADA`.
- El acta es 1 a 1 con la mesa.
- **Ley 026 (presidente):** gana en primera vuelta con más del 50% de los votos válidos, o con más del 40% y 10 puntos de diferencia sobre el segundo. Si no, hay segunda vuelta.

### Capa stats

Algoritmos con arreglos (sin librerías estadísticas): frecuencias, media, mediana, moda, percentiles, varianza, desviación estándar, coeficiente de variación, Chebyshev.

### Interfaz definitiva

El `MainConsola` actual es un mantenimiento de datos. Pueden reemplazarlo o ampliarlo con GUI (JavaFX/Swing) y estos flujos:

1. Padrón: buscar CI, marcar asistencia (`ha_votado`, `hora_sufragio`).
2. Escrutinio 1 a 1: registrar cada papeleta.
3. Cierre de mesa: generar `acta` y `detalle_voto_partido`.
4. Resultados: tabla de totales y porcentajes.
5. Aplicar Ley 026 al cómputo nacional.

### Pruebas (opcional pero recomendado)

```text
src/test/java/bo/edu/electoral/
├── MotorElectoralTest.java    casos de 1.ª y 2.ª vuelta
└── EstadisticaTest.java       verificar fórmulas
```

## Fuera del alcance de esta entrega docente

No viene implementado (y no hace falta para el curso): login de usuarios, Spring, JPA/Hibernate, API REST, OCR de actas ni reportes PDF.
