package bo.edu.electoral.dao;

import bo.edu.electoral.config.DatabaseConnection;
import bo.edu.electoral.model.DetalleVoto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DetalleVotoDAO {

    public int insert(DetalleVoto detalle) throws SQLException {
        String sql = "INSERT INTO detalle_voto_partido (id_acta, id_partido, votos_validos) "
                + "VALUES (?, ?, ?) RETURNING id_detalle";
        Connection cn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, detalle.getIdActa());
            ps.setInt(2, detalle.getIdPartido());
            ps.setInt(3, detalle.getVotosValidos());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    detalle.setIdDetalle(id);
                    return id;
                }
            }
        }
        throw new SQLException("No se pudo insertar el detalle de voto");
    }

    public boolean update(DetalleVoto detalle) throws SQLException {
        String sql = "UPDATE detalle_voto_partido SET id_acta = ?, id_partido = ?, votos_validos = ? "
                + "WHERE id_detalle = ?";
        Connection cn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, detalle.getIdActa());
            ps.setInt(2, detalle.getIdPartido());
            ps.setInt(3, detalle.getVotosValidos());
            ps.setInt(4, detalle.getIdDetalle());
            return ps.executeUpdate() > 0;
        }
    }

    public DetalleVoto findById(int id) throws SQLException {
        String sql = "SELECT id_detalle, id_acta, id_partido, votos_validos "
                + "FROM detalle_voto_partido WHERE id_detalle = ?";
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

    public List<DetalleVoto> findAll() throws SQLException {
        List<DetalleVoto> lista = new ArrayList<>();
        String sql = "SELECT id_detalle, id_acta, id_partido, votos_validos "
                + "FROM detalle_voto_partido ORDER BY id_detalle";
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
        String sql = "DELETE FROM detalle_voto_partido WHERE id_detalle = ?";
        Connection cn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private DetalleVoto mapear(ResultSet rs) throws SQLException {
        DetalleVoto detalle = new DetalleVoto();
        detalle.setIdDetalle(rs.getInt("id_detalle"));
        detalle.setIdActa(rs.getInt("id_acta"));
        detalle.setIdPartido(rs.getInt("id_partido"));
        detalle.setVotosValidos(rs.getInt("votos_validos"));
        return detalle;
    }
}
