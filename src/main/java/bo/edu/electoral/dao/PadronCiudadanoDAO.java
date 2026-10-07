package bo.edu.electoral.dao;

import bo.edu.electoral.config.DatabaseConnection;
import bo.edu.electoral.model.PadronCiudadano;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class PadronCiudadanoDAO {

    public String insert(PadronCiudadano ciudadano) throws SQLException {
        String sql = "INSERT INTO padron_ciudadano (ci, nombres, apellidos, id_mesa, ha_votado, hora_sufragio) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        Connection cn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, ciudadano.getCi());
            ps.setString(2, ciudadano.getNombres());
            ps.setString(3, ciudadano.getApellidos());
            ps.setInt(4, ciudadano.getIdMesa());
            ps.setBoolean(5, ciudadano.isHaVotado());
            setTimestampONulo(ps, 6, ciudadano.getHoraSufragio());
            ps.executeUpdate();
            return ciudadano.getCi();
        }
    }

    public boolean update(PadronCiudadano ciudadano) throws SQLException {
        String sql = "UPDATE padron_ciudadano SET nombres = ?, apellidos = ?, id_mesa = ?, "
                + "ha_votado = ?, hora_sufragio = ? WHERE ci = ?";
        Connection cn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, ciudadano.getNombres());
            ps.setString(2, ciudadano.getApellidos());
            ps.setInt(3, ciudadano.getIdMesa());
            ps.setBoolean(4, ciudadano.isHaVotado());
            setTimestampONulo(ps, 5, ciudadano.getHoraSufragio());
            ps.setString(6, ciudadano.getCi());
            return ps.executeUpdate() > 0;
        }
    }

    public PadronCiudadano findById(String ci) throws SQLException {
        String sql = "SELECT ci, nombres, apellidos, id_mesa, ha_votado, hora_sufragio "
                + "FROM padron_ciudadano WHERE ci = ?";
        Connection cn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, ci);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    public List<PadronCiudadano> findAll() throws SQLException {
        List<PadronCiudadano> lista = new ArrayList<>();
        String sql = "SELECT ci, nombres, apellidos, id_mesa, ha_votado, hora_sufragio "
                + "FROM padron_ciudadano ORDER BY apellidos, nombres";
        Connection cn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public long contarVotantesPorMesa(int idMesa) throws SQLException {
        String sql = "SELECT COUNT(*) FROM padron_ciudadano WHERE id_mesa = ? AND ha_votado = TRUE";
        Connection cn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idMesa);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        throw new SQLException("No se pudo contar los ciudadanos que votaron en la mesa " + idMesa);
    }

    public boolean delete(String ci) throws SQLException {
        String sql = "DELETE FROM padron_ciudadano WHERE ci = ?";
        Connection cn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, ci);
            return ps.executeUpdate() > 0;
        }
    }

    private void setTimestampONulo(PreparedStatement ps, int indice, Timestamp valor) throws SQLException {
        if (valor == null) {
            ps.setNull(indice, Types.TIMESTAMP);
        } else {
            ps.setTimestamp(indice, valor);
        }
    }

    private PadronCiudadano mapear(ResultSet rs) throws SQLException {
        PadronCiudadano ciudadano = new PadronCiudadano();
        ciudadano.setCi(rs.getString("ci"));
        ciudadano.setNombres(rs.getString("nombres"));
        ciudadano.setApellidos(rs.getString("apellidos"));
        ciudadano.setIdMesa(rs.getInt("id_mesa"));
        ciudadano.setHaVotado(rs.getBoolean("ha_votado"));
        ciudadano.setHoraSufragio(rs.getTimestamp("hora_sufragio"));
        return ciudadano;
    }
}
