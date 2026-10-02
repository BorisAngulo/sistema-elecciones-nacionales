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
            int mesa = Math.toIntExact(escalar(c, "SELECT id_mesa FROM padron_ciudadano WHERE ci=?", ci.trim()));
            int inscritos = bloquearMesa(c, mesa);
            long asistentes = escalar(c, "SELECT COUNT(*) FROM padron_ciudadano WHERE id_mesa=? AND ha_votado", mesa);
            if (asistentes >= inscritos) throw new IllegalArgumentException("Se alcanzó la cantidad de inscritos");
            try (PreparedStatement p = c.prepareStatement(
                    "UPDATE padron_ciudadano SET ha_votado=TRUE, hora_sufragio=CURRENT_TIMESTAMP "
                    + "WHERE ci=? AND id_mesa=? AND ha_votado=FALSE")) {
                p.setString(1, ci.trim()); p.setInt(2, mesa);
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
        return transaccion(c -> {
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
        });
    }
}

