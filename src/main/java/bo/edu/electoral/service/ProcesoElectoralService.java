package bo.edu.electoral.service;

import bo.edu.electoral.config.DatabaseConnection;
import java.sql.*;
import java.util.Locale;
import java.util.Objects;

/** Cada operación usa conexión propia y bloquea la mesa antes de modificarla.
 * Los bloqueos serializan asistencia, escrutinio y cierre de la misma mesa. */
public final class ProcesoElectoralService {
    @FunctionalInterface public interface Conexion { Connection abrir() throws SQLException; }
    @FunctionalInterface private interface Operacion<T> { T ejecutar(Connection c) throws SQLException; }
    private final Conexion conexiones;
    public ProcesoElectoralService() { this(DatabaseConnection::openConnection); }
    public ProcesoElectoralService(Conexion conexiones) { this.conexiones = Objects.requireNonNull(conexiones); }
    private <T> T transaccion(Operacion<T> operacion) throws SQLException {
        try (Connection c = conexiones.abrir()) {
            c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            c.setAutoCommit(false);
            try {
                T resultado = operacion.ejecutar(c);
                c.commit();
                return resultado;
            } catch (SQLException | RuntimeException e) {
                try { c.rollback(); } catch (SQLException rollback) { e.addSuppressed(rollback); }
                throw e;
            }
        }
    }
    private long escalar(Connection c, String sql, Object... parametros) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) p.setObject(i + 1, parametros[i]);
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) throw new IllegalArgumentException("Registro no encontrado");
                return r.getLong(1);
            }
        }
    }
    private int bloquearMesa(Connection c, int id) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(
                "SELECT estado, cantidad_inscritos FROM mesa WHERE id_mesa=? FOR UPDATE")) {
            p.setInt(1, id);
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) throw new IllegalArgumentException("Mesa inexistente");
                if (!"HABILITADA".equals(r.getString(1)))
                    throw new IllegalArgumentException("No se permite operar una mesa ANULADA o COMPUTADA");
                int inscritos = r.getInt(2);
                if (escalar(c, "SELECT COUNT(*) FROM acta WHERE id_mesa=?", id) != 0)
                    throw new IllegalArgumentException("La mesa ya tiene acta");
                return inscritos;
            }
        }
    }
    public void marcarAsistencia(String ci) throws SQLException {
        if (ci == null || ci.isBlank()) throw new IllegalArgumentException("Ingrese un CI");
        transaccion(c -> {
            exigirEstado(c, "ABIERTA");
            int mesa = Math.toIntExact(escalar(c, "SELECT id_mesa FROM padron_ciudadano WHERE ci=?", ci.trim().toUpperCase(Locale.ROOT)));
            int inscritos = bloquearMesa(c, mesa);
            if (escalar(c,"SELECT COUNT(*) FROM padron_ciudadano WHERE ci=? AND ha_votado",ci.trim().toUpperCase(Locale.ROOT))>0)
                throw new IllegalArgumentException("Este CI ya tiene asistencia registrada. No puede votar dos veces.");
            long asistentes = escalar(c, "SELECT COUNT(*) FROM padron_ciudadano WHERE id_mesa=? AND ha_votado", mesa);
            if (asistentes >= inscritos) throw new IllegalArgumentException("Se alcanzó la cantidad de inscritos");
            try (PreparedStatement p = c.prepareStatement(
                    "UPDATE padron_ciudadano SET ha_votado=TRUE, hora_sufragio=CURRENT_TIMESTAMP "
                    + "WHERE ci=? AND id_mesa=? AND ha_votado=FALSE")) {
                p.setString(1, ci.trim().toUpperCase(Locale.ROOT)); p.setInt(2, mesa);
                if (p.executeUpdate() != 1)
                    throw new IllegalArgumentException("El CI ya votó o cambió de mesa");
            }
            return null;
        });
    }
    public int registrarPapeleta(int mesa, String tipo, Integer partido) throws SQLException {
        String normalizado = tipo == null ? "" : tipo.trim().toUpperCase(Locale.ROOT);
        if (!normalizado.equals("VALIDO") && !normalizado.equals("BLANCO") && !normalizado.equals("NULO"))
            throw new IllegalArgumentException("Tipo permitido: VALIDO, BLANCO o NULO");
        if (normalizado.equals("VALIDO") != (partido != null) || (partido != null && partido <= 0))
            throw new IllegalArgumentException("Solo el voto válido debe indicar un partido");
        return transaccion(c -> {
            exigirEstado(c, "ABIERTA");
            int inscritos = bloquearMesa(c, mesa);
            long asistieron = escalar(c, "SELECT COUNT(*) FROM padron_ciudadano WHERE id_mesa=? AND ha_votado", mesa);
            long papeletas = escalar(c, "SELECT COUNT(*) FROM papeleta_escrutinio WHERE id_mesa=?", mesa);
            if (papeletas >= asistieron || papeletas >= inscritos)
                throw new IllegalArgumentException("No puede haber más papeletas que asistentes o inscritos");
            int orden = Math.toIntExact(escalar(c,
                    "SELECT COALESCE(MAX(orden_extraccion),0)+1 FROM papeleta_escrutinio WHERE id_mesa=?", mesa));
            try (PreparedStatement p = c.prepareStatement(
                    "INSERT INTO papeleta_escrutinio(id_mesa,orden_extraccion,tipo_voto,id_partido) VALUES (?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                p.setInt(1, mesa); p.setInt(2, orden); p.setString(3, normalizado);
                p.setObject(4, partido, Types.INTEGER);
                p.executeUpdate();
                try (ResultSet r = p.getGeneratedKeys()) {
                    if (!r.next()) throw new SQLException("No se obtuvo el ID de la papeleta");
                    return r.getInt(1);
                }
            }
        });
    }
    public int cerrarMesa(int mesa) throws SQLException {
        return transaccion(c -> { exigirEstado(c, "ABIERTA"); return cerrarMesa(c, mesa); });
    }
    private int cerrarMesa(Connection c, int mesa) throws SQLException {
            int inscritos = bloquearMesa(c, mesa);
            long asistieron = escalar(c, "SELECT COUNT(*) FROM padron_ciudadano WHERE id_mesa=? AND ha_votado", mesa);
            long blancos = escalar(c, "SELECT COUNT(*) FROM papeleta_escrutinio WHERE id_mesa=? AND tipo_voto='BLANCO'", mesa);
            long nulos = escalar(c, "SELECT COUNT(*) FROM papeleta_escrutinio WHERE id_mesa=? AND tipo_voto='NULO'", mesa);
            long validos = escalar(c, "SELECT COUNT(*) FROM papeleta_escrutinio WHERE id_mesa=? AND tipo_voto='VALIDO'", mesa);
            long papeletas = escalar(c, "SELECT COUNT(*) FROM papeleta_escrutinio WHERE id_mesa=?", mesa);
            new ValidadorActaService().validar("HABILITADA", false, inscritos, asistieron, blancos, nulos, validos, papeletas);
            int acta;
            try (PreparedStatement p = c.prepareStatement(
                    "INSERT INTO acta(id_mesa,votos_blancos,votos_nulos,total_ciudadanos_votaron) VALUES (?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                p.setInt(1, mesa); p.setInt(2, Math.toIntExact(blancos));
                p.setInt(3, Math.toIntExact(nulos)); p.setInt(4, Math.toIntExact(asistieron));
                p.executeUpdate();
                try (ResultSet r = p.getGeneratedKeys()) {
                    if (!r.next()) throw new SQLException("No se obtuvo el ID del acta");
                    acta = r.getInt(1);
                }
            }
            try (PreparedStatement p = c.prepareStatement(
                    "INSERT INTO detalle_voto_partido(id_acta,id_partido,votos_validos) "
                    + "SELECT ?,id_partido,COUNT(*) FROM papeleta_escrutinio WHERE id_mesa=? AND tipo_voto='VALIDO' GROUP BY id_partido")) {
                p.setInt(1, acta); p.setInt(2, mesa); p.executeUpdate();
            }
            try (PreparedStatement p = c.prepareStatement("UPDATE mesa SET estado='COMPUTADA' WHERE id_mesa=?")) {
                p.setInt(1, mesa); p.executeUpdate();
            }
            return acta;
    }

    public String estado() throws SQLException {
        try (Connection c = conexiones.abrir()) { return leerEstado(c, false); }
    }
    private String leerEstado(Connection c, boolean bloquear) throws SQLException {
        try (var p = c.prepareStatement("SELECT estado FROM jornada_electoral WHERE id=1" + (bloquear ? " FOR UPDATE" : ""));
             var r = p.executeQuery()) {
            if (!r.next()) throw new IllegalArgumentException("Falta configurar la jornada. Ejecute INSTALAR_SISTEMA_MYSQL.sql.");
            return r.getString(1);
        }
    }
    private void exigirEstado(Connection c, String requerido) throws SQLException {
        String actual = leerEstado(c, true);
        if (!actual.equals(requerido)) throw new IllegalArgumentException(
                "Estado actual: " + actual + ". Esta operación requiere " + requerido +
                (actual.equals("CERRADA") ? ". Esta jornada terminó; el cierre es definitivo." :
                 requerido.equals("ABIERTA") ? ". Use la opción 6 para abrir votaciones." : ". El registro termina al abrir votaciones."));
    }
    private String campo(String valor, int max, String nombre) {
        if (valor == null || valor.trim().isEmpty() || valor.trim().length() > max)
            throw new IllegalArgumentException(nombre + " es obligatorio y admite hasta " + max + " caracteres.");
        return valor.trim();
    }
    public void crearCiudadano(String ci, String nombres, String apellidos, int mesa) throws SQLException {
        String id = campo(ci,15,"CI").toUpperCase(Locale.ROOT);
        String n = campo(nombres,50,"Nombres"), a = campo(apellidos,50,"Apellidos");
        transaccion(c -> {
            exigirEstado(c,"PREPARACION");
            if (escalar(c,"SELECT COUNT(*) FROM padron_ciudadano WHERE ci=?",id)>0)
                throw new IllegalArgumentException("El ciudadano con CI " + id + " ya está registrado. No se creó otro.");
            bloquearMesa(c,mesa);
            try (var q=c.prepareStatement("INSERT INTO padron_ciudadano(ci,nombres,apellidos,id_mesa) VALUES(?,?,?,?)")) {
                q.setString(1,id); q.setString(2,n); q.setString(3,a); q.setInt(4,mesa); q.executeUpdate();
            }
            try (var q=c.prepareStatement("UPDATE mesa SET cantidad_inscritos=(SELECT COUNT(*) FROM padron_ciudadano WHERE id_mesa=?) WHERE id_mesa=?")) {
                q.setInt(1,mesa); q.setInt(2,mesa); q.executeUpdate();
            }
            return null;
        });
    }
    public int crearPartido(String sigla, String nombre, String candidato) throws SQLException {
        String sig = campo(sigla,20,"Sigla").toUpperCase(Locale.ROOT);
        String n=campo(nombre,150,"Nombre"), ca=campo(candidato,100,"Candidato");
        return transaccion(c -> {
            exigirEstado(c,"PREPARACION");
            if (escalar(c,"SELECT COUNT(*) FROM partido_politico WHERE UPPER(sigla)=?",sig)>0)
                throw new IllegalArgumentException("La sigla " + sig + " ya está registrada. No se creó otro partido.");
            try(var q=c.prepareStatement("INSERT INTO partido_politico(sigla,nombre_completo,candidato_presidente) VALUES(?,?,?)",Statement.RETURN_GENERATED_KEYS)) {
                q.setString(1,sig); q.setString(2,n); q.setString(3,ca); q.executeUpdate();
                try(var r=q.getGeneratedKeys()) { if(!r.next()) throw new SQLException("No se obtuvo el ID"); return r.getInt(1); }
            }
        });
    }
    public void abrirVotaciones() throws SQLException {
        transaccion(c -> {
            exigirEstado(c,"PREPARACION");
            if(escalar(c,"SELECT COUNT(*) FROM partido_politico")<2)
                throw new IllegalArgumentException("Registre al menos dos partidos antes de abrir.");
            if(escalar(c,"SELECT COUNT(*) FROM padron_ciudadano p JOIN mesa m ON m.id_mesa=p.id_mesa WHERE m.estado='HABILITADA'")==0)
                throw new IllegalArgumentException("Registre al menos un ciudadano en una mesa habilitada.");
            if(escalar(c,"SELECT COUNT(*) FROM padron_ciudadano WHERE ha_votado")>0 ||
               escalar(c,"SELECT COUNT(*) FROM papeleta_escrutinio")>0 || escalar(c,"SELECT COUNT(*) FROM acta")>0)
                throw new IllegalArgumentException("La base contiene una votación previa. Use una base nueva.");
            try(var q=c.prepareStatement("UPDATE mesa m SET cantidad_inscritos=(SELECT COUNT(*) FROM padron_ciudadano p WHERE p.id_mesa=m.id_mesa) WHERE estado='HABILITADA'")) { q.executeUpdate(); }
            try(var q=c.prepareStatement("UPDATE jornada_electoral SET estado='ABIERTA',fecha_apertura=CURRENT_TIMESTAMP WHERE id=1")) { q.executeUpdate(); }
            return null;
        });
    }
    /** Cierre atómico: si falta una papeleta, no cierra ninguna mesa ni la jornada. */
    public int cerrarVotaciones() throws SQLException {
        return transaccion(c -> {
            exigirEstado(c,"ABIERTA");
            java.util.List<Integer> mesas = new java.util.ArrayList<>();
            try(var q=c.prepareStatement("SELECT id_mesa FROM mesa WHERE estado='HABILITADA' ORDER BY id_mesa");var r=q.executeQuery()) {
                while(r.next()) mesas.add(r.getInt(1));
            }
            for(int mesa:mesas) {
                try { cerrarMesa(c,mesa); }
                catch(IllegalArgumentException e) { throw new IllegalArgumentException("No se cerró la jornada. Revise la mesa " + mesa + ": " + e.getMessage()); }
            }
            try(var q=c.prepareStatement("UPDATE jornada_electoral SET estado='CERRADA',fecha_cierre=CURRENT_TIMESTAMP WHERE id=1")) { q.executeUpdate(); }
            return mesas.size();
        });
    }
}

