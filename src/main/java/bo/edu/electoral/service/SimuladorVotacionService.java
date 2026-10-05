package bo.edu.electoral.service;

import bo.edu.electoral.config.DatabaseConnection;
import bo.edu.electoral.dao.MesaDAO;
import bo.edu.electoral.dao.PadronCiudadanoDAO;
import bo.edu.electoral.dao.PapeletaEscrutinioDAO;
import bo.edu.electoral.dao.PartidoPoliticoDAO;
import bo.edu.electoral.model.Mesa;
import bo.edu.electoral.model.PadronCiudadano;
import bo.edu.electoral.model.PapeletaEscrutinio;
import bo.edu.electoral.model.PartidoPolitico;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Simula el sufragio de un votante usando los DAOs.
 * El CI se valida en el padrón; la papeleta no guarda CI (voto secreto).
 */
public class SimuladorVotacionService {

    private final PadronCiudadanoDAO padronDAO = new PadronCiudadanoDAO();
    private final MesaDAO mesaDAO = new MesaDAO();
    private final PartidoPoliticoDAO partidoDAO = new PartidoPoliticoDAO();
    private final PapeletaEscrutinioDAO papeletaDAO = new PapeletaEscrutinioDAO();

    public PadronCiudadano validarCi(String ci) throws SQLException, VotacionException {
        if (ci == null || ci.isBlank()) {
            throw new VotacionException("Debe registrar un CI.");
        }

        PadronCiudadano ciudadano = padronDAO.findById(ci.trim());
        if (ciudadano == null) {
            throw new VotacionException("El CI " + ci + " no está en el padrón de esta elección.");
        }
        if (ciudadano.isHaVotado()) {
            throw new VotacionException("El CI " + ci + " ya votó. No se permite un segundo sufragio.");
        }

        Mesa mesa = mesaDAO.findById(ciudadano.getIdMesa());
        if (mesa == null) {
            throw new VotacionException("La mesa " + ciudadano.getIdMesa() + " asignada al ciudadano no existe.");
        }
        if (!Mesa.ESTADO_HABILITADA.equals(mesa.getEstado())) {
            throw new VotacionException(
                    "La mesa " + mesa.getNumeroMesa() + " está " + mesa.getEstado() + ". No se puede votar.");
        }

        ciudadano.setMesa(mesa);
        return ciudadano;
    }

    public List<PartidoPolitico> listarPartidos() throws SQLException {
        return partidoDAO.findAll();
    }

    /**
     * Registra la papeleta (sin CI) y marca asistencia en el padrón.
     *
     * @param tipoVoto VALIDO, BLANCO o NULO
     * @param idPartido obligatorio solo si el tipo es VALIDO
     */
    public PapeletaEscrutinio registrarVoto(String ci, String tipoVoto, Integer idPartido)
            throws SQLException, VotacionException {
        PadronCiudadano ciudadano = validarCi(ci);
        String tipo = normalizarTipo(tipoVoto);

        if (PapeletaEscrutinio.TIPO_VALIDO.equals(tipo)) {
            if (idPartido == null) {
                throw new VotacionException("Un voto válido debe indicar el partido.");
            }
            PartidoPolitico partido = partidoDAO.findById(idPartido);
            if (partido == null) {
                throw new VotacionException("No existe el partido con ID " + idPartido + ".");
            }
        } else {
            idPartido = null;
        }

        PapeletaEscrutinio papeleta = new PapeletaEscrutinio();
        papeleta.setIdMesa(ciudadano.getIdMesa());
        papeleta.setOrdenExtraccion(papeletaDAO.siguienteOrden(ciudadano.getIdMesa()));
        papeleta.setTipoVoto(tipo);
        papeleta.setIdPartido(idPartido);

        Connection cn = DatabaseConnection.getConnection();
        boolean autoCommit = cn.getAutoCommit();
        try {
            cn.setAutoCommit(false);
            papeletaDAO.insert(papeleta);
            ciudadano.setHaVotado(true);
            ciudadano.setHoraSufragio(Timestamp.valueOf(LocalDateTime.now()));
            padronDAO.update(ciudadano);
            cn.commit();
        } catch (SQLException e) {
            cn.rollback();
            throw e;
        } finally {
            cn.setAutoCommit(autoCommit);
        }
        return papeleta;
    }

    private String normalizarTipo(String tipoVoto) throws VotacionException {
        if (tipoVoto == null || tipoVoto.isBlank()) {
            throw new VotacionException("Debe elegir VALIDO, BLANCO o NULO.");
        }
        String tipo = tipoVoto.trim().toUpperCase();
        if (!PapeletaEscrutinio.TIPO_VALIDO.equals(tipo)
                && !PapeletaEscrutinio.TIPO_BLANCO.equals(tipo)
                && !PapeletaEscrutinio.TIPO_NULO.equals(tipo)) {
            throw new VotacionException("Tipo de voto no válido. Use VALIDO, BLANCO o NULO.");
        }
        return tipo;
    }
}
