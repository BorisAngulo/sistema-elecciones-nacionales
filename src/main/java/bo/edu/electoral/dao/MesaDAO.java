package bo.edu.electoral.dao;

import bo.edu.electoral.config.DatabaseConnection;
import bo.edu.electoral.model.Mesa;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MesaDAO {

    public int insert(Mesa mesa) throws SQLException {
        String sql = "INSERT INTO mesa (numero_mesa, id_recinto, cantidad_inscritos, estado) "
                + "VALUES (?, ?, ?, ?)";
        Connection cn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = cn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, mesa.getNumeroMesa());
            ps.setInt(2, mesa.getIdRecinto());
            ps.setInt(3, mesa.getCantidadInscritos());
            ps.setString(4, mesa.getEstado());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    mesa.setIdMesa(id);
                    return id;
                }
            }
        }
        throw new SQLException("No se pudo insertar la mesa");
    }

    public boolean update(Mesa mesa) throws SQLException {
        String sql = "UPDATE mesa SET numero_mesa = ?, id_recinto = ?, cantidad_inscritos = ?, estado = ? "
                + "WHERE id_mesa = ?";
        Connection cn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, mesa.getNumeroMesa());
            ps.setInt(2, mesa.getIdRecinto());
            ps.setInt(3, mesa.getCantidadInscritos());
            ps.setString(4, mesa.getEstado());
            ps.setInt(5, mesa.getIdMesa());
            return ps.executeUpdate() > 0;
        }
    }

    public Mesa findById(int id) throws SQLException {
        String sql = "SELECT id_mesa, numero_mesa, id_recinto, cantidad_inscritos, estado FROM mesa WHERE id_mesa = ?";
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

    public List<Mesa> findAll() throws SQLException {
        List<Mesa> lista = new ArrayList<>();
        String sql = "SELECT id_mesa, numero_mesa, id_recinto, cantidad_inscritos, estado FROM mesa ORDER BY id_mesa";
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
        String sql = "DELETE FROM mesa WHERE id_mesa = ?";
        Connection cn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Mesa mapear(ResultSet rs) throws SQLException {
        Mesa mesa = new Mesa();
        mesa.setIdMesa(rs.getInt("id_mesa"));
        mesa.setNumeroMesa(rs.getInt("numero_mesa"));
        mesa.setIdRecinto(rs.getInt("id_recinto"));
        mesa.setCantidadInscritos(rs.getInt("cantidad_inscritos"));
        mesa.setEstado(rs.getString("estado"));
        return mesa;
    }
}
