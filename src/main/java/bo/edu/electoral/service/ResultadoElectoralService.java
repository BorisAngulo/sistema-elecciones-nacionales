package bo.edu.electoral.service;

import bo.edu.electoral.dao.ActaDAO;
import bo.edu.electoral.dao.DepartamentoDAO;
import bo.edu.electoral.dao.DetalleVotoDAO;
import bo.edu.electoral.dao.MesaDAO;
import bo.edu.electoral.dao.MunicipioDAO;
import bo.edu.electoral.dao.PartidoPoliticoDAO;
import bo.edu.electoral.dao.RecintoDAO;
import bo.edu.electoral.model.Acta;
import bo.edu.electoral.model.Departamento;
import bo.edu.electoral.model.DetalleVoto;
import bo.edu.electoral.model.Mesa;
import bo.edu.electoral.model.Municipio;
import bo.edu.electoral.model.PartidoPolitico;
import bo.edu.electoral.model.Recinto;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Calcula los resultados oficiales registrados en las actas y sus detalles.
 */
public class ResultadoElectoralService {

    private final ActaDAO actaDAO = new ActaDAO();
    private final DepartamentoDAO departamentoDAO = new DepartamentoDAO();
    private final DetalleVotoDAO detalleVotoDAO = new DetalleVotoDAO();
    private final MesaDAO mesaDAO = new MesaDAO();
    private final MunicipioDAO municipioDAO = new MunicipioDAO();
    private final PartidoPoliticoDAO partidoDAO = new PartidoPoliticoDAO();
    private final RecintoDAO recintoDAO = new RecintoDAO();

    public InformeElectoral computar() throws SQLException {
        List<PartidoPolitico> partidos = partidoDAO.findAll();
        List<Departamento> departamentos = departamentoDAO.findAll();

        Map<Integer, PartidoPolitico> partidosPorId = new LinkedHashMap<>();
        for (PartidoPolitico partido : partidos) {
            partidosPorId.put(partido.getIdPartido(), partido);
        }

        Map<Integer, Acta> actasPorId = indexarActas(actaDAO.findAll());
        Map<Integer, Mesa> mesasPorId = indexarMesas(mesaDAO.findAll());
        Map<Integer, Recinto> recintosPorId = indexarRecintos(recintoDAO.findAll());
        Map<Integer, Municipio> municipiosPorId = indexarMunicipios(municipioDAO.findAll());

        Map<Integer, Map<Integer, Long>> votosPorDepartamento = new LinkedHashMap<>();
        for (Departamento departamento : departamentos) {
            votosPorDepartamento.put(departamento.getIdDepartamento(), inicializarVotos(partidos));
        }
        Map<Integer, Long> votosNacionales = inicializarVotos(partidos);

        for (DetalleVoto detalle : detalleVotoDAO.findAll()) {
            if (detalle.getVotosValidos() < 0) {
                throw new IllegalStateException("El detalle " + detalle.getIdDetalle()
                        + " tiene una cantidad negativa de votos.");
            }

            PartidoPolitico partido = partidosPorId.get(detalle.getIdPartido());
            if (partido == null) {
                throw new IllegalStateException("No existe el partido " + detalle.getIdPartido()
                        + " del detalle " + detalle.getIdDetalle() + ".");
            }

            Acta acta = requerido(actasPorId.get(detalle.getIdActa()), "acta", detalle.getIdActa());
            Mesa mesa = requerido(mesasPorId.get(acta.getIdMesa()), "mesa", acta.getIdMesa());
            Recinto recinto = requerido(recintosPorId.get(mesa.getIdRecinto()), "recinto", mesa.getIdRecinto());
            Municipio municipio = requerido(
                    municipiosPorId.get(recinto.getIdMunicipio()), "municipio", recinto.getIdMunicipio());
            int idDepartamento = municipio.getIdDepartamento();
            Map<Integer, Long> votosDepartamento = votosPorDepartamento.get(idDepartamento);
            if (votosDepartamento == null) {
                throw new IllegalStateException("No existe el departamento " + idDepartamento
                        + " asociado al detalle " + detalle.getIdDetalle() + ".");
            }

            sumarVotos(votosNacionales, partido.getIdPartido(), detalle);
            sumarVotos(votosDepartamento, partido.getIdPartido(), detalle);
        }

        List<ResultadoDepartamento> resultadosDepartamento = new ArrayList<>();
        for (Departamento departamento : departamentos) {
            Map<Integer, Long> votos = votosPorDepartamento.get(departamento.getIdDepartamento());
            long totalValidos = totalVotos(votos);
            resultadosDepartamento.add(new ResultadoDepartamento(
                    departamento.getIdDepartamento(),
                    departamento.getNombre(),
                    totalValidos,
                    crearFilas(partidos, votos, totalValidos)));
        }
        resultadosDepartamento.sort(Comparator.comparing(ResultadoDepartamento::departamento));

        long totalNacional = totalVotos(votosNacionales);
        ResultadoNacional resultadoNacional = new ResultadoNacional(
                totalNacional,
                crearFilas(partidos, votosNacionales, totalNacional));

        return new InformeElectoral(resultadoNacional, resultadosDepartamento);
    }

    public ResultadoDepartamento obtenerResultadoPorDepartamento(int idDepartamento) throws SQLException {
        return computar().departamentos().stream()
                .filter(resultado -> resultado.idDepartamento() == idDepartamento)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe el departamento con id " + idDepartamento + "."));
    }

    private Map<Integer, Long> inicializarVotos(List<PartidoPolitico> partidos) {
        Map<Integer, Long> votos = new LinkedHashMap<>();
        for (PartidoPolitico partido : partidos) {
            votos.put(partido.getIdPartido(), 0L);
        }
        return votos;
    }

    private Map<Integer, Acta> indexarActas(List<Acta> actas) {
        Map<Integer, Acta> resultado = new LinkedHashMap<>();
        for (Acta acta : actas) {
            resultado.put(acta.getIdActa(), acta);
        }
        return resultado;
    }

    private Map<Integer, Mesa> indexarMesas(List<Mesa> mesas) {
        Map<Integer, Mesa> resultado = new LinkedHashMap<>();
        for (Mesa mesa : mesas) {
            resultado.put(mesa.getIdMesa(), mesa);
        }
        return resultado;
    }

    private Map<Integer, Recinto> indexarRecintos(List<Recinto> recintos) {
        Map<Integer, Recinto> resultado = new LinkedHashMap<>();
        for (Recinto recinto : recintos) {
            resultado.put(recinto.getIdRecinto(), recinto);
        }
        return resultado;
    }

    private Map<Integer, Municipio> indexarMunicipios(List<Municipio> municipios) {
        Map<Integer, Municipio> resultado = new LinkedHashMap<>();
        for (Municipio municipio : municipios) {
            resultado.put(municipio.getIdMunicipio(), municipio);
        }
        return resultado;
    }

    private <T> T requerido(T valor, String entidad, int id) {
        if (valor == null) {
            throw new IllegalStateException("No existe " + entidad + " con id " + id + ".");
        }
        return valor;
    }

    private void sumarVotos(Map<Integer, Long> votos, int idPartido, DetalleVoto detalle) {
        votos.put(idPartido, Math.addExact(votos.get(idPartido), detalle.getVotosValidos()));
    }

    private long totalVotos(Map<Integer, Long> votos) {
        long total = 0;
        for (long cantidad : votos.values()) {
            total = Math.addExact(total, cantidad);
        }
        return total;
    }

    private List<FilaPartido> crearFilas(
            List<PartidoPolitico> partidos,
            Map<Integer, Long> votos,
            long totalValidos
    ) {
        List<FilaPartido> filas = new ArrayList<>();
        for (PartidoPolitico partido : partidos) {
            long cantidad = votos.get(partido.getIdPartido());
            double porcentaje = totalValidos == 0 ? 0.0 : cantidad * 100.0 / totalValidos;
            filas.add(new FilaPartido(
                    partido.getSigla(),
                    partido.getNombreCompleto(),
                    partido.getCandidatoPresidente(),
                    cantidad,
                    porcentaje));
        }
        filas.sort(Comparator.comparingLong(FilaPartido::votos).reversed()
                .thenComparing(FilaPartido::sigla));
        return List.copyOf(filas);
    }

    public record FilaPartido(
            String sigla,
            String nombreCompleto,
            String candidato,
            long votos,
            double porcentajeSobreValidos
    ) {
    }

    public record ResultadoNacional(long votosValidos, List<FilaPartido> partidos) {
        public ResultadoNacional {
            partidos = List.copyOf(partidos);
        }
    }

    public record ResultadoDepartamento(
            int idDepartamento,
            String departamento,
            long votosValidos,
            List<FilaPartido> partidos
    ) {
        public ResultadoDepartamento {
            partidos = List.copyOf(partidos);
        }
    }

    public record InformeElectoral(
            ResultadoNacional nacional,
            List<ResultadoDepartamento> departamentos
    ) {
        public InformeElectoral {
            departamentos = List.copyOf(departamentos);
        }
    }
}
