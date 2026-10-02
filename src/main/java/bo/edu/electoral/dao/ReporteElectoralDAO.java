package bo.edu.electoral.dao;

import bo.edu.electoral.config.DatabaseConnection;
import java.sql.*;
import java.util.*;

/** Agrega actas de mesas COMPUTADAS. Blancos/nulos quedan fuera del denominador válido. */
public final class ReporteElectoralDAO {
    public record TotalPartido(int idPartido, String sigla, long votos, double porcentaje) {}
    public record Resumen(List<TotalPartido> partidos, long validos, long blancos, long nulos,
                          long asistentes, long computadas, long habilitadas, long anuladas) {
        public boolean completo() { return computadas > 0 && habilitadas == 0; }
    }
    public record TotalDepartamento(String departamento, String sigla, long votos) {}
    public Resumen nacional() throws SQLException {
        try (Connection c = DatabaseConnection.openConnection()) {
            c.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            c.setAutoCommit(false);
            c.setReadOnly(true);
            try {
                Resumen resultado = nacional(c);
                c.commit();
                return resultado;
            } catch (SQLException | RuntimeException e) { c.rollback(); throw e; }
        }
    }
    public Resumen nacional(Connection c) throws SQLException {
        List<TotalPartido> partidos = new ArrayList<>();
        long validos = 0;
        String sql = """
            SELECT p.id_partido,p.sigla,COALESCE(SUM(v.votos_validos),0) votos
            FROM partido_politico p
            LEFT JOIN (
                SELECT d.id_partido,d.votos_validos FROM detalle_voto_partido d
                JOIN acta a ON a.id_acta=d.id_acta
                JOIN mesa m ON m.id_mesa=a.id_mesa WHERE m.estado='COMPUTADA'
            ) v ON v.id_partido=p.id_partido
            GROUP BY p.id_partido,p.sigla ORDER BY votos DESC,p.id_partido
            """;
        try (PreparedStatement p = c.prepareStatement(sql); ResultSet r = p.executeQuery()) {
            while (r.next()) {
                long votos = r.getLong(3); validos = Math.addExact(validos, votos);
                partidos.add(new TotalPartido(r.getInt(1), r.getString(2), votos, 0));
            }
        }
        for (int i = 0; i < partidos.size(); i++) {
            var p = partidos.get(i);
            partidos.set(i, new TotalPartido(p.idPartido(),p.sigla(),p.votos(),
                    validos == 0 ? 0 : p.votos() * 100.0 / validos));
        }
        long blancos, nulos, asistentes;
        try (PreparedStatement p = c.prepareStatement("""
                SELECT COALESCE(SUM(a.votos_blancos),0),COALESCE(SUM(a.votos_nulos),0),
                       COALESCE(SUM(a.total_ciudadanos_votaron),0)
                FROM acta a JOIN mesa m ON m.id_mesa=a.id_mesa WHERE m.estado='COMPUTADA'
                """); ResultSet r = p.executeQuery()) {
            r.next(); blancos = r.getLong(1); nulos = r.getLong(2); asistentes = r.getLong(3);
        }
        long computadas = 0, habilitadas = 0, anuladas = 0;
        try (PreparedStatement p = c.prepareStatement("SELECT estado,COUNT(*) FROM mesa GROUP BY estado");
             ResultSet r = p.executeQuery()) {
            while (r.next()) switch (r.getString(1)) {
                case "COMPUTADA" -> computadas = r.getLong(2);
                case "HABILITADA" -> habilitadas = r.getLong(2);
                case "ANULADA" -> anuladas = r.getLong(2);
                default -> throw new SQLException("Estado de mesa inválido");
            }
        }
        return new Resumen(List.copyOf(partidos),validos,blancos,nulos,asistentes,computadas,habilitadas,anuladas);
    }
    public List<TotalDepartamento> porDepartamento() throws SQLException {
        List<TotalDepartamento> resultado = new ArrayList<>();
        try (Connection c = DatabaseConnection.openConnection();
             PreparedStatement p = c.prepareStatement("""
                SELECT dep.nombre,p.sigla,SUM(d.votos_validos)
                FROM detalle_voto_partido d JOIN partido_politico p ON p.id_partido=d.id_partido
                JOIN acta a ON a.id_acta=d.id_acta JOIN mesa m ON m.id_mesa=a.id_mesa
                JOIN recinto r ON r.id_recinto=m.id_recinto
                JOIN municipio mu ON mu.id_municipio=r.id_municipio
                JOIN departamento dep ON dep.id_departamento=mu.id_departamento
                WHERE m.estado='COMPUTADA' GROUP BY dep.id_departamento,dep.nombre,p.id_partido,p.sigla
                ORDER BY dep.nombre,SUM(d.votos_validos) DESC,p.id_partido
                """); ResultSet r = p.executeQuery()) {
            while (r.next()) resultado.add(new TotalDepartamento(r.getString(1), r.getString(2),r.getLong(3)));
        }
        return List.copyOf(resultado);
    }
}

