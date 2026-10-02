package bo.edu.electoral.dao;

import bo.edu.electoral.config.DatabaseConnection;
import bo.edu.electoral.model.Recinto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RecintoDAO {

    public int insert(Recinto recinto) throws SQLException {
        String sql = "INSERT INTO recinto (nombre, id_municipio) VALUES (?, ?)";
        Connection cn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = cn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, recinto.getNombre());
            ps.setInt(2, recinto.getIdMunicipio());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    recinto.setIdRecinto(id);
                    return id;
                }
            }
        }
        throw new SQLException("No se pudo insertar el recinto");
    }

    public boolean update(Recinto recinto) throws SQLException {
        String sql = "UPDATE recinto SET nombre = ?, id_municipio = ? WHERE id_recinto = ?";
        Connection cn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, recinto.getNombre());
            ps.setInt(2, recinto.getIdMunicipio());
            ps.setInt(3, recinto.getIdRecinto());
            return ps.executeUpdate() > 0;
        }
    }

    public Recinto findById(int id) throws SQLException {
        String sql = "SELECT id_recinto, nombre, id_municipio FROM recinto WHERE id_recinto = ?";
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

    public List<Recinto> findAll() throws SQLException {
        List<Recinto> lista = new ArrayList<>();
        String sql = "SELECT id_recinto, nombre, id_municipio FROM recinto ORDER BY id_recinto";
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
        String sql = "DELETE FROM recinto WHERE id_recinto = ?";
        Connection cn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Recinto mapear(ResultSet rs) throws SQLException {
        Recinto recinto = new Recinto();
        recinto.setIdRecinto(rs.getInt("id_recinto"));
        recinto.setNombre(rs.getString("nombre"));
        recinto.setIdMunicipio(rs.getInt("id_municipio"));
        return recinto;
    }
}
