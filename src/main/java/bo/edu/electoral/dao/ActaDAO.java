package bo.edu.electoral.dao;

import bo.edu.electoral.config.DatabaseConnection;
import bo.edu.electoral.model.Acta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ActaDAO {

    public int insert(Acta acta) throws SQLException {
        String sql = "INSERT INTO acta (id_mesa, votos_blancos, votos_nulos, total_ciudadanos_votaron) "
                + "VALUES (?, ?, ?, ?) RETURNING id_acta";
        Connection cn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, acta.getIdMesa());
            ps.setInt(2, acta.getVotosBlancos());
            ps.setInt(3, acta.getVotosNulos());
            ps.setInt(4, acta.getTotalCiudadanosVotaron());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    acta.setIdActa(id);
                    return id;
                }
            }
        }
        throw new SQLException("No se pudo insertar el acta");
    }

    public boolean update(Acta acta) throws SQLException {
        String sql = "UPDATE acta SET id_mesa = ?, votos_blancos = ?, votos_nulos = ?, "
                + "total_ciudadanos_votaron = ? WHERE id_acta = ?";
        Connection cn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, acta.getIdMesa());
            ps.setInt(2, acta.getVotosBlancos());
            ps.setInt(3, acta.getVotosNulos());
            ps.setInt(4, acta.getTotalCiudadanosVotaron());
            ps.setInt(5, acta.getIdActa());
            return ps.executeUpdate() > 0;
        }
    }

    public Acta findById(int id) throws SQLException {
        String sql = "SELECT id_acta, id_mesa, votos_blancos, votos_nulos, total_ciudadanos_votaron, fecha_registro "
                + "FROM acta WHERE id_acta = ?";
        Connection cn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    public List<Acta> findAll() throws SQLException {
        List<Acta> lista = new ArrayList<>();
        String sql = "SELECT id_acta, id_mesa, votos_blancos, votos_nulos, total_ciudadanos_votaron, fecha_registro "
                + "FROM acta ORDER BY id_acta";
        Connection cn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM acta WHERE id_acta = ?";
        Connection cn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Acta mapear(ResultSet rs) throws SQLException {
        Acta acta = new Acta();
        acta.setIdActa(rs.getInt("id_acta"));
        acta.setIdMesa(rs.getInt("id_mesa"));
        acta.setVotosBlancos(rs.getInt("votos_blancos"));
        acta.setVotosNulos(rs.getInt("votos_nulos"));
        acta.setTotalCiudadanosVotaron(rs.getInt("total_ciudadanos_votaron"));
        acta.setFechaRegistro(rs.getTimestamp("fecha_registro"));
        return acta;
    }
}
