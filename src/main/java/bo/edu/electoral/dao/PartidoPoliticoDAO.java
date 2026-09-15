package bo.edu.electoral.dao;

import bo.edu.electoral.config.DatabaseConnection;
import bo.edu.electoral.model.PartidoPolitico;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PartidoPoliticoDAO {

    public int insert(PartidoPolitico partido) throws SQLException {
        String sql = "INSERT INTO partido_politico (sigla, nombre_completo, candidato_presidente) "
                + "VALUES (?, ?, ?) RETURNING id_partido";
        Connection cn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, partido.getSigla());
            ps.setString(2, partido.getNombreCompleto());
            ps.setString(3, partido.getCandidatoPresidente());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    partido.setIdPartido(id);
                    return id;
                }
            }
        }
        throw new SQLException("No se pudo insertar el partido");
    }

    public boolean update(PartidoPolitico partido) throws SQLException {
        String sql = "UPDATE partido_politico SET sigla = ?, nombre_completo = ?, candidato_presidente = ? "
                + "WHERE id_partido = ?";
        Connection cn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, partido.getSigla());
            ps.setString(2, partido.getNombreCompleto());
            ps.setString(3, partido.getCandidatoPresidente());
            ps.setInt(4, partido.getIdPartido());
            return ps.executeUpdate() > 0;
        }
    }

    public PartidoPolitico findById(int id) throws SQLException {
        String sql = "SELECT id_partido, sigla, nombre_completo, candidato_presidente "
                + "FROM partido_politico WHERE id_partido = ?";
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

    public List<PartidoPolitico> findAll() throws SQLException {
        List<PartidoPolitico> lista = new ArrayList<>();
        String sql = "SELECT id_partido, sigla, nombre_completo, candidato_presidente "
                + "FROM partido_politico ORDER BY id_partido";
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
        String sql = "DELETE FROM partido_politico WHERE id_partido = ?";
        Connection cn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private PartidoPolitico mapear(ResultSet rs) throws SQLException {
        PartidoPolitico partido = new PartidoPolitico();
        partido.setIdPartido(rs.getInt("id_partido"));
        partido.setSigla(rs.getString("sigla"));
        partido.setNombreCompleto(rs.getString("nombre_completo"));
        partido.setCandidatoPresidente(rs.getString("candidato_presidente"));
        return partido;
    }
}
