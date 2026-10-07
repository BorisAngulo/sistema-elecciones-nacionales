package bo.edu.electoral.service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Aplica los criterios de la Ley 026 al cómputo nacional de votos válidos.
 */
public class ResultadoFinalService {

    private static final BigDecimal CINCO = BigDecimal.valueOf(5);
    private static final BigDecimal DIEZ = BigDecimal.TEN;
    private static final BigDecimal DOS = BigDecimal.valueOf(2);

    private final ResultadoElectoralService resultadoElectoralService;

    public ResultadoFinalService() {
        this(new ResultadoElectoralService());
    }

    public ResultadoFinalService(ResultadoElectoralService resultadoElectoralService) {
        this.resultadoElectoralService = Objects.requireNonNull(resultadoElectoralService);
    }

    public ResultadoFinal computar() throws SQLException {
        return evaluar(resultadoElectoralService.computar());
    }

    public ResultadoFinal evaluar(ResultadoElectoralService.InformeElectoral informe) {
        Objects.requireNonNull(informe);
        long votosValidos = informe.nacional().votosValidos();
        List<ResultadoElectoralService.FilaPartido> ranking = informe.nacional().partidos().stream()
                .sorted(Comparator.comparingLong(ResultadoElectoralService.FilaPartido::votos).reversed()
                        .thenComparing(ResultadoElectoralService.FilaPartido::sigla))
                .toList();

        if (votosValidos == 0) {
            return new ResultadoFinal(
                    informe, EstadoResultado.SIN_VOTOS_VALIDOS, Optional.empty(), Optional.empty(), List.of());
        }

        if (ranking.isEmpty()) {
            return new ResultadoFinal(
                    informe, EstadoResultado.INDETERMINADO, Optional.empty(), Optional.empty(), List.of());
        }

        ResultadoElectoralService.FilaPartido primero = ranking.get(0);
        if (obtuvoMasDel50PorCiento(primero.votos(), votosValidos)) {
            return new ResultadoFinal(
                    informe, EstadoResultado.GANADOR_PRIMERA_VUELTA, Optional.of(primero),
                    Optional.of(CriterioPrimeraVuelta.MAS_DEL_50_POR_CIENTO), List.of());
        }

        if (ranking.size() < 2) {
            return new ResultadoFinal(
                    informe, EstadoResultado.INDETERMINADO, Optional.empty(), Optional.empty(), List.of());
        }

        ResultadoElectoralService.FilaPartido segundo = ranking.get(1);
        if (obtuvoAlMenos40PorCiento(primero.votos(), votosValidos)
                && tieneVentajaDeAlMenos10Puntos(primero.votos(), segundo.votos(), votosValidos)) {
            return new ResultadoFinal(
                    informe, EstadoResultado.GANADOR_PRIMERA_VUELTA, Optional.of(primero),
                    Optional.of(CriterioPrimeraVuelta.AL_MENOS_40_Y_10_PUNTOS_DE_VENTAJA), List.of());
        }

        if (ranking.size() > 2 && segundo.votos() == ranking.get(2).votos()) {
            return new ResultadoFinal(
                    informe, EstadoResultado.EMPATE_POR_SEGUNDO_LUGAR, Optional.empty(), Optional.empty(),
                    List.of(segundo, ranking.get(2)));
        }

        return new ResultadoFinal(
                informe, EstadoResultado.SEGUNDA_VUELTA, Optional.empty(), Optional.empty(),
                List.of(primero, segundo));
    }

    private boolean obtuvoMasDel50PorCiento(long votos, long total) {
        return votos > total / 2;
    }

    private boolean obtuvoAlMenos40PorCiento(long votos, long total) {
        return BigDecimal.valueOf(votos).multiply(CINCO)
                .compareTo(BigDecimal.valueOf(total).multiply(DOS)) >= 0;
    }

    private boolean tieneVentajaDeAlMenos10Puntos(long primero, long segundo, long total) {
        long diferencia = primero - segundo;
        return BigDecimal.valueOf(diferencia).multiply(DIEZ).compareTo(BigDecimal.valueOf(total)) >= 0;
    }

    public enum EstadoResultado {
        SIN_VOTOS_VALIDOS,
        GANADOR_PRIMERA_VUELTA,
        SEGUNDA_VUELTA,
        EMPATE_POR_SEGUNDO_LUGAR,
        INDETERMINADO
    }

    public enum CriterioPrimeraVuelta {
        MAS_DEL_50_POR_CIENTO,
        AL_MENOS_40_Y_10_PUNTOS_DE_VENTAJA
    }

    public record ResultadoFinal(
            ResultadoElectoralService.InformeElectoral informe,
            EstadoResultado estado,
            Optional<ResultadoElectoralService.FilaPartido> ganador,
            Optional<CriterioPrimeraVuelta> criterioPrimeraVuelta,
            List<ResultadoElectoralService.FilaPartido> candidatosSegundaVuelta
    ) {
        public ResultadoFinal {
            Objects.requireNonNull(informe);
            Objects.requireNonNull(estado);
            ganador = ganador == null ? Optional.empty() : ganador;
            criterioPrimeraVuelta = criterioPrimeraVuelta == null ? Optional.empty() : criterioPrimeraVuelta;
            candidatosSegundaVuelta = List.copyOf(candidatosSegundaVuelta);
        }
    }
}
