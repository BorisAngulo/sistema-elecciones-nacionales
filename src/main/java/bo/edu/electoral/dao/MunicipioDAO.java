package bo.edu.electoral.dao;

import bo.edu.electoral.config.DatabaseConnection;
import bo.edu.electoral.model.Municipio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MunicipioDAO {

    public int insert(Municipio municipio) throws SQLException {
        String sql = "INSERT INTO municipio (nombre, id_departamento) VALUES (?, ?) RETURNING id_municipio";
        Connection cn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, municipio.getNombre());
            ps.setInt(2, municipio.getIdDepartamento());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    municipio.setIdMunicipio(id);
                    return id;
                }
            }
        }
        throw new SQLException("No se pudo insertar el municipio");
    }

    public boolean update(Municipio municipio) throws SQLException {
        String sql = "UPDATE municipio SET nombre = ?, id_departamento = ? WHERE id_municipio = ?";
        Connection cn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, municipio.getNombre());
            ps.setInt(2, municipio.getIdDepartamento());
            ps.setInt(3, municipio.getIdMunicipio());
            return ps.executeUpdate() > 0;
        }
    }

    public Municipio findById(int id) throws SQLException {
        String sql = "SELECT id_municipio, nombre, id_departamento FROM municipio WHERE id_municipio = ?";
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

    public List<Municipio> findAll() throws SQLException {
        List<Municipio> lista = new ArrayList<>();
        String sql = "SELECT id_municipio, nombre, id_departamento FROM municipio ORDER BY id_municipio";
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
        String sql = "DELETE FROM municipio WHERE id_municipio = ?";
        Connection cn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Municipio mapear(ResultSet rs) throws SQLException {
        Municipio municipio = new Municipio();
        municipio.setIdMunicipio(rs.getInt("id_municipio"));
        municipio.setNombre(rs.getString("nombre"));
        municipio.setIdDepartamento(rs.getInt("id_departamento"));
        return municipio;
    }
}
