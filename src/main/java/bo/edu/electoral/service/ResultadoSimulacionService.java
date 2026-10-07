package bo.edu.electoral.service;

import bo.edu.electoral.dao.PapeletaEscrutinioDAO;
import bo.edu.electoral.dao.DepartamentoDAO;
import bo.edu.electoral.dao.PartidoPoliticoDAO;
import bo.edu.electoral.model.Departamento;
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
    private final DepartamentoDAO departamentoDAO = new DepartamentoDAO();
    private final PartidoPoliticoDAO partidoDAO = new PartidoPoliticoDAO();

    public ComputoTotal computar() throws SQLException {
        return computarEstadisticas().nacional();
    }

    public InformeEstadistico computarEstadisticas() throws SQLException {
        List<PartidoPolitico> partidos = partidoDAO.findAll();
        Map<Integer, Acumulador> acumulados = new LinkedHashMap<>();
        for (Departamento departamento : departamentoDAO.findAll()) {
            acumulados.put(departamento.getIdDepartamento(),
                    new Acumulador(departamento.getIdDepartamento(), departamento.getNombre()));
        }
        Acumulador nacional = new Acumulador(0, "Nacional");

        for (PapeletaEscrutinioDAO.ConteoDepartamento conteo : papeletaDAO.contarPorDepartamento()) {
            Acumulador departamento = acumulados.computeIfAbsent(conteo.idDepartamento(),
                    id -> new Acumulador(id, conteo.departamento()));
            departamento.agregar(conteo);
            nacional.agregar(conteo);
        }

        List<ResultadoDepartamento> resultadosDepartamento = new ArrayList<>();
        for (Acumulador acumulado : acumulados.values()) {
            resultadosDepartamento.add(new ResultadoDepartamento(
                    acumulado.idDepartamento,
                    acumulado.nombre,
                    convertir(acumulado, partidos)));
        }
        resultadosDepartamento.sort((a, b) -> a.departamento().compareToIgnoreCase(b.departamento()));
        return new InformeEstadistico(convertir(nacional, partidos), resultadosDepartamento);
    }

    private ComputoTotal convertir(Acumulador acumulado, List<PartidoPolitico> partidos) {
        List<FilaResultado> filas = new ArrayList<>();
        for (PartidoPolitico partido : partidos) {
            long votos = acumulado.votosPorPartido.getOrDefault(partido.getIdPartido(), 0L);
            filas.add(new FilaResultado(partido.getSigla(), partido.getCandidatoPresidente(),
                    Math.toIntExact(votos), porcentaje(votos, acumulado.validos)));
        }
        filas.sort((a, b) -> Integer.compare(b.votos(), a.votos()));

        long total = Math.addExact(Math.addExact(acumulado.validos, acumulado.blancos), acumulado.nulos);
        return new ComputoTotal(
                filas,
                Math.toIntExact(acumulado.validos),
                Math.toIntExact(acumulado.blancos),
                Math.toIntExact(acumulado.nulos),
                Math.toIntExact(total));
    }

    private double porcentaje(long parte, long total) {
        if (total == 0) {
            return 0;
        }
        return parte * 100.0 / total;
    }

    private static class Acumulador {
        private final int idDepartamento;
        private final String nombre;
        private final Map<Integer, Long> votosPorPartido = new LinkedHashMap<>();
        private long validos;
        private long blancos;
        private long nulos;

        private Acumulador(int idDepartamento, String nombre) {
            this.idDepartamento = idDepartamento;
            this.nombre = nombre;
        }

        private void agregar(PapeletaEscrutinioDAO.ConteoDepartamento conteo) {
            switch (conteo.tipoVoto()) {
                case PapeletaEscrutinio.TIPO_VALIDO -> {
                    validos = Math.addExact(validos, conteo.cantidad());
                    if (conteo.idPartido() != null) {
                        votosPorPartido.merge(conteo.idPartido(), conteo.cantidad(), Math::addExact);
                    }
                }
                case PapeletaEscrutinio.TIPO_BLANCO ->
                        blancos = Math.addExact(blancos, conteo.cantidad());
                case PapeletaEscrutinio.TIPO_NULO ->
                        nulos = Math.addExact(nulos, conteo.cantidad());
                default -> throw new IllegalStateException(
                        "Tipo de voto no reconocido: " + conteo.tipoVoto());
            }
        }
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
        public ComputoTotal {
            partidos = List.copyOf(partidos);
        }
    }

    public record ResultadoDepartamento(
            int idDepartamento,
            String departamento,
            ComputoTotal computo
    ) {
    }

    public record InformeEstadistico(
            ComputoTotal nacional,
            List<ResultadoDepartamento> departamentos
    ) {
        public InformeEstadistico {
            departamentos = List.copyOf(departamentos);
        }
    }
}
