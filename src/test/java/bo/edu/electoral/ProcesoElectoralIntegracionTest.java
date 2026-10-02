package bo.edu.electoral;

import bo.edu.electoral.dao.ReporteElectoralDAO;
import bo.edu.electoral.dao.*;
import bo.edu.electoral.model.*;
import bo.edu.electoral.config.DatabaseConnection;
import bo.edu.electoral.service.ProcesoElectoralService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import java.nio.file.*;
import java.sql.*;
import java.util.UUID;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

/** Crea y elimina exclusivamente una base aleatoria en una instancia de pruebas.
 * ELECTORAL_TEST_URL, ELECTORAL_TEST_USER y ELECTORAL_TEST_PASSWORD. */
@EnabledIfEnvironmentVariable(named="ELECTORAL_TEST_URL", matches="jdbc:mysql:.*")
class ProcesoElectoralIntegracionTest {
    private String esquema;
    private ProcesoElectoralService proceso;
    private Connection conexion() throws SQLException {
        Connection c = DriverManager.getConnection(System.getenv("ELECTORAL_TEST_URL"),
                System.getenv().getOrDefault("ELECTORAL_TEST_USER","root"),
                System.getenv().getOrDefault("ELECTORAL_TEST_PASSWORD",""));
        if (esquema != null) c.setCatalog(esquema);
        return c;
    }
    @BeforeEach void preparar() throws Exception {
        String nueva = "prueba_" + UUID.randomUUID().toString().replace("-","");
        try (Connection c = conexion(); Statement s = c.createStatement()) {
            s.execute("CREATE DATABASE " + nueva + " CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs");
            esquema = nueva;
            c.setCatalog(esquema);
            ejecutarScript(c, Files.readString(Path.of("src/main/resources/schema.sql")));
            ejecutarScript(c, """
                INSERT INTO departamento VALUES(1,'Prueba');
                INSERT INTO municipio VALUES(1,'Municipio',1);
                INSERT INTO recinto VALUES(1,'Recinto',1);
                INSERT INTO mesa(id_mesa,numero_mesa,id_recinto,cantidad_inscritos) VALUES(1,1,1,3),(2,2,1,3);
                INSERT INTO partido_politico VALUES(1,'A','Partido A','Candidato A'),(2,'B','Partido B','Candidato B');
                INSERT INTO padron_ciudadano(ci,nombres,apellidos,id_mesa) VALUES
                    ('CI1','Ana','Prueba',1),('CI2','Luis','Prueba',1),('CI3','Sol','Prueba',1);
                """);
        }
        proceso = new ProcesoElectoralService(this::conexion);
    }
    @AfterEach void limpiar() throws SQLException {
        if (esquema != null && esquema.matches("prueba_[a-f0-9]{32}"))
            try (Connection c = conexion(); Statement s = c.createStatement()) { s.execute("DROP DATABASE " + esquema); }
    }
    private long contar(String sql) throws SQLException {
        try (Connection c = conexion(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            r.next(); return r.getLong(1);
        }
    }
    @Test void flujoCompletoYReporteSinDuplicarBlancos() throws Exception {
        proceso.marcarAsistencia("CI1"); proceso.marcarAsistencia("CI2"); proceso.marcarAsistencia("CI3");
        proceso.registrarPapeleta(1,"VALIDO",1); proceso.registrarPapeleta(1,"VALIDO",2);
        proceso.registrarPapeleta(1,"BLANCO",null);
        proceso.cerrarMesa(1);
        try (Connection c = conexion()) {
            var r = new ReporteElectoralDAO().nacional(c);
            assertEquals(2,r.validos()); assertEquals(1,r.blancos()); assertEquals(3,r.asistentes());
            assertEquals(50,r.partidos().get(0).porcentaje());
            assertEquals(1,r.computadas()); assertFalse(r.completo());
        }
        assertThrows(IllegalArgumentException.class,()->proceso.cerrarMesa(1));
        assertThrows(IllegalArgumentException.class,()->proceso.registrarPapeleta(1,"NULO",null));
    }
    @Test void rechazaDobleAsistenciaYExcesoPapeletas() throws Exception {
        proceso.marcarAsistencia("CI1");
        assertThrows(IllegalArgumentException.class,()->proceso.marcarAsistencia("CI1"));
        proceso.registrarPapeleta(1,"NULO",null);
        assertThrows(IllegalArgumentException.class,()->proceso.registrarPapeleta(1,"NULO",null));
        assertEquals(1,contar("SELECT COUNT(*) FROM padron_ciudadano WHERE ha_votado AND hora_sufragio IS NOT NULL"));
    }
    @Test void cierreIncompletoNoDejaActa() throws Exception {
        proceso.marcarAsistencia("CI1");
        assertThrows(IllegalArgumentException.class,()->proceso.cerrarMesa(1));
        assertEquals(0,contar("SELECT COUNT(*) FROM acta"));
        assertEquals(1,contar("SELECT COUNT(*) FROM mesa WHERE id_mesa=1 AND estado='HABILITADA'"));
    }
    @Test void falloPosteriorAlActaRevierteTodo() throws Exception {
        proceso.marcarAsistencia("CI1"); proceso.registrarPapeleta(1,"VALIDO",1);
        try (Connection c = conexion(); Statement s = c.createStatement()) {
            s.execute("""
                CREATE TRIGGER fallo BEFORE INSERT ON detalle_voto_partido FOR EACH ROW
                SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Fallo de prueba'
                """);
        }
        assertThrows(SQLException.class,()->proceso.cerrarMesa(1));
        assertEquals(0,contar("SELECT COUNT(*) FROM acta"));
        assertEquals(0,contar("SELECT COUNT(*) FROM detalle_voto_partido"));
        assertEquals(1,contar("SELECT COUNT(*) FROM mesa WHERE id_mesa=1 AND estado='HABILITADA'"));
        assertEquals(1,contar("SELECT COUNT(*) FROM papeleta_escrutinio"));
    }
    @Test void bloqueaAnuladasYTiposInvalidos() throws Exception {
        try (Connection c = conexion(); Statement s = c.createStatement()) { s.execute("UPDATE mesa SET estado='ANULADA' WHERE id_mesa=1"); }
        assertThrows(IllegalArgumentException.class,()->proceso.marcarAsistencia("CI1"));
        assertThrows(IllegalArgumentException.class,()->proceso.cerrarMesa(1));
        assertThrows(IllegalArgumentException.class,()->proceso.registrarPapeleta(1,"NULO",null));
        assertThrows(IllegalArgumentException.class,()->proceso.registrarPapeleta(2,"BLANCO",1));
        assertThrows(IllegalArgumentException.class,()->proceso.registrarPapeleta(2,"VALIDO",null));
        assertThrows(IllegalArgumentException.class,()->proceso.registrarPapeleta(2,"OTRO",null));
    }
    @Test void concurrenciaUnSoloVotoPorCI() throws Exception {
        try (ExecutorService pool = Executors.newFixedThreadPool(2)) {
            CountDownLatch inicio = new CountDownLatch(1);
            Callable<Boolean> intento = () -> {
                inicio.await();
                try { proceso.marcarAsistencia("CI1"); return true; }
                catch (IllegalArgumentException e) { return false; }
            };
            Future<Boolean> a = pool.submit(intento), b = pool.submit(intento);
            inicio.countDown();
            assertNotEquals(a.get(10,TimeUnit.SECONDS),b.get(10,TimeUnit.SECONDS));
        }
        assertEquals(1,contar("SELECT COUNT(*) FROM padron_ciudadano WHERE ha_votado"));
    }
    @Test void concurrenciaPapeletasSinOrdenDuplicado() throws Exception {
        proceso.marcarAsistencia("CI1"); proceso.marcarAsistencia("CI2");
        try (ExecutorService pool = Executors.newFixedThreadPool(2)) {
            CountDownLatch inicio = new CountDownLatch(1);
            Callable<Integer> voto = () -> { inicio.await(); return proceso.registrarPapeleta(1,"NULO",null); };
            Future<Integer> a = pool.submit(voto), b = pool.submit(voto);
            inicio.countDown();
            assertNotEquals(a.get(10,TimeUnit.SECONDS),b.get(10,TimeUnit.SECONDS));
        }
        assertEquals(2,contar("SELECT COUNT(DISTINCT orden_extraccion) FROM papeleta_escrutinio"));
        proceso.cerrarMesa(1);
    }
    @Test void cargaCompletaYEsquemaRepetible() throws Exception {
        try (Connection c = conexion(); Statement s = c.createStatement()) {
            vaciarFixture(c);
            ejecutarScript(c, Files.readString(Path.of("src/main/resources/data.sql")));
            ejecutarScript(c, Files.readString(Path.of("src/main/resources/schema.sql")));
        }
        assertEquals(0,contar("SELECT COUNT(*) FROM mesa WHERE estado='COMPUTADA'"));
        assertEquals(0,contar("SELECT COUNT(*) FROM (SELECT m.id_mesa FROM mesa m LEFT JOIN padron_ciudadano p ON p.id_mesa=m.id_mesa GROUP BY m.id_mesa,m.cantidad_inscritos HAVING COUNT(p.ci)<>m.cantidad_inscritos) x"));
        assertTrue(contar("SELECT COUNT(*) FROM padron_ciudadano")>0);
        assertEquals(0,contar("SELECT COUNT(*) FROM padron_ciudadano WHERE ha_votado"));
    }
    @Test void restriccionSQLYPartidoInexistente() throws Exception {
        proceso.marcarAsistencia("CI1");
        assertThrows(SQLException.class,()->proceso.registrarPapeleta(1,"VALIDO",999));
        assertEquals(0,contar("SELECT COUNT(*) FROM papeleta_escrutinio"));
        try (Connection c = conexion(); Statement s = c.createStatement()) {
            assertThrows(SQLException.class,()->s.execute("INSERT INTO papeleta_escrutinio(id_mesa,orden_extraccion,tipo_voto,id_partido) VALUES(1,1,'BLANCO',1)"));
        }
    }

    @Test void daosRecuperanClavesAutogeneradasYConservanCrud() throws Exception {
        // Inyectar solo durante esta prueba la conexión de la base aislada en el singleton original.
        var campo = DatabaseConnection.class.getDeclaredField("connection");
        campo.setAccessible(true);
        Object anterior = campo.get(null);
        try (Connection c = conexion()) {
            campo.set(null,c);
            try {
                var dep = new Departamento(); dep.setNombre("Departamento DAO");
                int idDep = new DepartamentoDAO().insert(dep);
                assertTrue(idDep > 1); assertEquals(idDep,dep.getIdDepartamento());
                var mu = new Municipio(); mu.setNombre("Municipio DAO"); mu.setIdDepartamento(idDep);
                int idMu = new MunicipioDAO().insert(mu);
                assertEquals(idMu,mu.getIdMunicipio());
                var re = new Recinto(); re.setNombre("Recinto DAO"); re.setIdMunicipio(idMu);
                int idRe = new RecintoDAO().insert(re);
                assertEquals(idRe,re.getIdRecinto());
                var me = new Mesa(); me.setNumeroMesa(99); me.setIdRecinto(idRe); me.setCantidadInscritos(3);
                me.setEstado("HABILITADA");
                int idMe = new MesaDAO().insert(me);
                assertEquals(idMe,me.getIdMesa());
                var pa = new PartidoPolitico(); pa.setSigla("DAO"); pa.setNombreCompleto("Partido DAO");
                pa.setCandidatoPresidente("Candidatura DAO");
                int idPa = new PartidoPoliticoDAO().insert(pa);
                assertEquals(idPa,pa.getIdPartido());
                var ci = new PadronCiudadano("DAO-1","Persona","Prueba",idMe);
                assertEquals("DAO-1",new PadronCiudadanoDAO().insert(ci));
                var papeleta = new PapeletaEscrutinio(0,idMe,1,"VALIDO",idPa);
                int idPapeleta = new PapeletaEscrutinioDAO().insert(papeleta);
                assertEquals(idPapeleta,papeleta.getIdPapeleta());
                var acta = new Acta(0,idMe,0,0,1);
                int idActa = new ActaDAO().insert(acta);
                assertEquals(idActa,acta.getIdActa());
                var detalle = new DetalleVoto(0,idActa,idPa,1);
                int idDetalle = new DetalleVotoDAO().insert(detalle);
                assertEquals(idDetalle,detalle.getIdDetalle());
                assertEquals(idDep,new MunicipioDAO().findById(idMu).getIdDepartamento());
                assertEquals(idMu,new RecintoDAO().findById(idRe).getIdMunicipio());
                assertEquals(idRe,new MesaDAO().findById(idMe).getIdRecinto());
                assertEquals("DAO",new PartidoPoliticoDAO().findById(idPa).getSigla());
                assertEquals(idMe,new PadronCiudadanoDAO().findById("DAO-1").getIdMesa());
                assertEquals(idPa,new PapeletaEscrutinioDAO().findById(idPapeleta).getIdPartido());
                assertEquals(idMe,new ActaDAO().findById(idActa).getIdMesa());
                assertEquals(1,new DetalleVotoDAO().findById(idDetalle).getVotosValidos());
                dep.setNombre("Departamento editado");
                assertTrue(new DepartamentoDAO().update(dep));
                assertEquals("Departamento editado",new DepartamentoDAO().findById(idDep).getNombre());
                assertTrue(new DetalleVotoDAO().delete(idDetalle));
                assertTrue(new ActaDAO().delete(idActa));
                assertTrue(new PapeletaEscrutinioDAO().delete(idPapeleta));
                assertTrue(new PadronCiudadanoDAO().delete("DAO-1"));
                assertTrue(new PartidoPoliticoDAO().delete(idPa));
                assertTrue(new MesaDAO().delete(idMe));
                assertTrue(new RecintoDAO().delete(idRe));
                assertTrue(new MunicipioDAO().delete(idMu));
                assertTrue(new DepartamentoDAO().delete(idDep));
                assertNull(new DepartamentoDAO().findById(idDep));
            } finally { campo.set(null,anterior); }
        }
    }
    /** Ejecuta los mismos scripts que Workbench, respetando DELIMITER. */
    private static void ejecutarScript(Connection c, String script) throws SQLException {
        String delimitador = ";";
        StringBuilder sentencia = new StringBuilder();
        try (Statement s = c.createStatement()) {
            for (String linea : script.split("\\R")) {
                String limpia = linea.trim();
                if (limpia.startsWith("--") || limpia.isEmpty()) continue;
                if (limpia.startsWith("DELIMITER ")) {
                    delimitador = limpia.substring(10).trim();
                    continue;
                }
                sentencia.append(linea).append('\n');
                String sql = sentencia.toString().trim();
                if (sql.endsWith(delimitador)) {
                    s.execute(sql.substring(0,sql.length()-delimitador.length()));
                    sentencia.setLength(0);
                }
            }
            if (!sentencia.toString().isBlank()) throw new SQLException("Sentencia SQL sin delimitador");
        }
    }
    private static void vaciarFixture(Connection c) throws SQLException {
        try (Statement s = c.createStatement()) {
            for (String tabla : new String[]{"papeleta_escrutinio","detalle_voto_partido","acta",
                    "padron_ciudadano","mesa","recinto","municipio","departamento","partido_politico"}) {
                s.executeUpdate("DELETE FROM " + tabla);
                if (!tabla.equals("padron_ciudadano")) s.execute("ALTER TABLE " + tabla + " AUTO_INCREMENT=1");
            }
        }
    }
    @Test void demoDeCincoVotantesYRecargaProtegida() throws Exception {
        try (Connection c = conexion()) {
            vaciarFixture(c);
            ejecutarScript(c,Files.readString(Path.of("src/main/resources/data_demo.sql")));
            assertThrows(SQLException.class,()->ejecutarScript(c,Files.readString(Path.of("src/main/resources/data_demo.sql"))));
        }
        assertEquals(5,contar("SELECT COUNT(*) FROM padron_ciudadano"));
        for (int i=1;i<=5;i++) proceso.marcarAsistencia("DEMO-"+i);
        proceso.registrarPapeleta(1,"VALIDO",1); proceso.registrarPapeleta(1,"VALIDO",1);
        proceso.registrarPapeleta(1,"VALIDO",2); proceso.registrarPapeleta(1,"BLANCO",null);
        proceso.registrarPapeleta(1,"NULO",null);
        proceso.cerrarMesa(1);
        try (Connection c = conexion()) {
            var r = new ReporteElectoralDAO().nacional(c);
            assertEquals(3,r.validos()); assertEquals(1,r.blancos()); assertEquals(1,r.nulos());
            assertEquals(5,r.asistentes()); assertTrue(r.completo());
            assertEquals(200.0/3,r.partidos().get(0).porcentaje(),1e-9);
        }
    }
}
