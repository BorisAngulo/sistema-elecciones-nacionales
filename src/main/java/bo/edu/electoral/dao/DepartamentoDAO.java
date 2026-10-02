package bo.edu.electoral.dao;

import bo.edu.electoral.config.DatabaseConnection;
import bo.edu.electoral.model.Departamento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DepartamentoDAO {

    public int insert(Departamento departamento) throws SQLException {
        String sql = "INSERT INTO departamento (nombre) VALUES (?)";
        Connection cn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = cn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, departamento.getNombre());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    departamento.setIdDepartamento(id);
                    return id;
                }
            }
        }
        throw new SQLException("No se pudo insertar el departamento");
    }

    public boolean update(Departamento departamento) throws SQLException {
        String sql = "UPDATE departamento SET nombre = ? WHERE id_departamento = ?";
        Connection cn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, departamento.getNombre());
            ps.setInt(2, departamento.getIdDepartamento());
            return ps.executeUpdate() > 0;
        }
    }

    public Departamento findById(int id) throws SQLException {
        String sql = "SELECT id_departamento, nombre FROM departamento WHERE id_departamento = ?";
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

    public List<Departamento> findAll() throws SQLException {
        List<Departamento> lista = new ArrayList<>();
        String sql = "SELECT id_departamento, nombre FROM departamento ORDER BY id_departamento";
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
        String sql = "DELETE FROM departamento WHERE id_departamento = ?";
        Connection cn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Departamento mapear(ResultSet rs) throws SQLException {
        Departamento departamento = new Departamento();
        departamento.setIdDepartamento(rs.getInt("id_departamento"));
        departamento.setNombre(rs.getString("nombre"));
        return departamento;
    }
}
