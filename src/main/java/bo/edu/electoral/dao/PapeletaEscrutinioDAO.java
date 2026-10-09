package bo.edu.electoral.dao;

import bo.edu.electoral.config.DatabaseConnection;
import bo.edu.electoral.model.PapeletaEscrutinio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class PapeletaEscrutinioDAO {

    public int insert(PapeletaEscrutinio papeleta) throws SQLException {
        String sql = "INSERT INTO papeleta_escrutinio (id_mesa, orden_extraccion, tipo_voto, id_partido) "
                + "VALUES (?, ?, ?, ?) RETURNING id_papeleta";
        Connection cn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, papeleta.getIdMesa());
            ps.setInt(2, papeleta.getOrdenExtraccion());
            ps.setString(3, papeleta.getTipoVoto());
            setEnteroONulo(ps, 4, papeleta.getIdPartido());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    papeleta.setIdPapeleta(id);
                    return id;
                }
            }
        }
        throw new SQLException("No se pudo insertar la papeleta");
    }

    public boolean update(PapeletaEscrutinio papeleta) throws SQLException {
        String sql = "UPDATE papeleta_escrutinio SET id_mesa = ?, orden_extraccion = ?, tipo_voto = ?, "
                + "id_partido = ? WHERE id_papeleta = ?";
        Connection cn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, papeleta.getIdMesa());
            ps.setInt(2, papeleta.getOrdenExtraccion());
            ps.setString(3, papeleta.getTipoVoto());
            setEnteroONulo(ps, 4, papeleta.getIdPartido());
            ps.setInt(5, papeleta.getIdPapeleta());
            return ps.executeUpdate() > 0;
        }
    }

    public PapeletaEscrutinio findById(int id) throws SQLException {
        String sql = "SELECT id_papeleta, id_mesa, orden_extraccion, tipo_voto, id_partido, fecha_registro "
                + "FROM papeleta_escrutinio WHERE id_papeleta = ?";
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

    public List<PapeletaEscrutinio> findAll() throws SQLException {
        List<PapeletaEscrutinio> lista = new ArrayList<>();
        String sql = "SELECT id_papeleta, id_mesa, orden_extraccion, tipo_voto, id_partido, fecha_registro "
                + "FROM papeleta_escrutinio ORDER BY id_mesa, orden_extraccion";
        Connection cn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    // Método para obtener la siguiente orden de extracción de la papeleta
    public int siguienteOrden(int idMesa) throws SQLException {
        String sql = "SELECT COALESCE(MAX(orden_extraccion), 0) + 1 FROM papeleta_escrutinio WHERE id_mesa = ?";
        Connection cn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idMesa);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 1;
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM papeleta_escrutinio WHERE id_papeleta = ?";
        Connection cn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private void setEnteroONulo(PreparedStatement ps, int indice, Integer valor) throws SQLException {
        if (valor == null) {
            ps.setNull(indice, Types.INTEGER);
        } else {
            ps.setInt(indice, valor);
        }
    }

    private PapeletaEscrutinio mapear(ResultSet rs) throws SQLException {
        PapeletaEscrutinio papeleta = new PapeletaEscrutinio();
        papeleta.setIdPapeleta(rs.getInt("id_papeleta"));
        papeleta.setIdMesa(rs.getInt("id_mesa"));
        papeleta.setOrdenExtraccion(rs.getInt("orden_extraccion"));
        papeleta.setTipoVoto(rs.getString("tipo_voto"));
        int idPartido = rs.getInt("id_partido");
        papeleta.setIdPartido(rs.wasNull() ? null : idPartido);
        papeleta.setFechaRegistro(rs.getTimestamp("fecha_registro"));
        return papeleta;
    }
}
