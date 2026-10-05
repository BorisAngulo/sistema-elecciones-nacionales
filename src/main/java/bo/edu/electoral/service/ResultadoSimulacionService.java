package bo.edu.electoral.service;

import bo.edu.electoral.dao.PapeletaEscrutinioDAO;
import bo.edu.electoral.dao.PartidoPoliticoDAO;
import bo.edu.electoral.model.PapeletaEscrutinio;
import bo.edu.electoral.model.PartidoPolitico;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Totales a partir de las papeletas. No cierra ni bloquea mesas ni actas.
 */
public class ResultadoSimulacionService {

    private final PapeletaEscrutinioDAO papeletaDAO = new PapeletaEscrutinioDAO();
    private final PartidoPoliticoDAO partidoDAO = new PartidoPoliticoDAO();

    public ComputoTotal computar() throws SQLException {
        List<PartidoPolitico> partidos = partidoDAO.findAll();
        List<PapeletaEscrutinio> papeletas = papeletaDAO.findAll();

        Map<Integer, Integer> votosPorPartido = new LinkedHashMap<>();
        for (PartidoPolitico partido : partidos) {
            votosPorPartido.put(partido.getIdPartido(), 0);
        }

        int blancos = 0;
        int nulos = 0;
        int validos = 0;
        for (PapeletaEscrutinio papeleta : papeletas) {
            if (PapeletaEscrutinio.TIPO_BLANCO.equals(papeleta.getTipoVoto())) {
                blancos++;
            } else if (PapeletaEscrutinio.TIPO_NULO.equals(papeleta.getTipoVoto())) {
                nulos++;
            } else if (PapeletaEscrutinio.TIPO_VALIDO.equals(papeleta.getTipoVoto())) {
                validos++;
                Integer idPartido = papeleta.getIdPartido();
                if (idPartido != null) {
                    votosPorPartido.merge(idPartido, 1, Integer::sum);
                }
            }
        }

        List<FilaResultado> filas = new ArrayList<>();
        for (PartidoPolitico partido : partidos) {
            int votos = votosPorPartido.getOrDefault(partido.getIdPartido(), 0);
            filas.add(new FilaResultado(partido.getSigla(), partido.getCandidatoPresidente(), votos,
                    porcentaje(votos, validos)));
        }
        filas.sort((a, b) -> Integer.compare(b.votos(), a.votos()));

        int total = validos + blancos + nulos;
        return new ComputoTotal(filas, validos, blancos, nulos, total);
    }

    private double porcentaje(int parte, int total) {
        if (total == 0) {
            return 0;
        }
        return parte * 100.0 / total;
    }

    public record FilaResultado(String sigla, String candidato, int votos, double porcentajeSobreValidos) {
    }

    public record ComputoTotal(
            List<FilaResultado> partidos,
            int votosValidos,
            int votosBlancos,
            int votosNulos,
            int totalPapeletas
    ) {
    }
}
