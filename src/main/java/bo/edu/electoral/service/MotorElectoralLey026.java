package bo.edu.electoral.service;

import java.math.BigInteger;
import java.util.*;

/** Art. 52.II Ley 026: >50%, o >=40% con >=10 puntos de ventaja.
 * Se comparan enteros para no redondear los límites legales. */
public final class MotorElectoralLey026 {
    public enum Estado { SIN_VOTOS_VALIDOS, PRIMERA_VUELTA, SEGUNDA_VUELTA }
    public record Candidatura(int idPartido, long votos) {}
    public record Resultado(Estado estado, Integer ganador, List<Candidatura> clasificacion,
                            long totalValidos) {}
    public Resultado evaluar(Map<Integer, Long> votos) {
        Objects.requireNonNull(votos, "Faltan votos");
        List<Candidatura> orden = new ArrayList<>();
        long total = 0;
        for (var e : votos.entrySet()) {
            if (e.getKey() == null || e.getKey() <= 0 || e.getValue() == null || e.getValue() < 0)
                throw new IllegalArgumentException("Partido o votos inválidos");
            total = Math.addExact(total, e.getValue());
            orden.add(new Candidatura(e.getKey(), e.getValue()));
        }
        orden.sort(Comparator.comparingLong(Candidatura::votos).reversed()
                .thenComparingInt(Candidatura::idPartido));
        List<Candidatura> lista = List.copyOf(orden);
        if (total == 0) return new Resultado(Estado.SIN_VOTOS_VALIDOS, null, lista, 0);
        long primero = orden.get(0).votos();
        long segundo = orden.size() > 1 ? orden.get(1).votos() : 0;
        BigInteger t = BigInteger.valueOf(total);
        boolean gana = BigInteger.valueOf(primero).multiply(BigInteger.TWO).compareTo(t) > 0
                || (BigInteger.valueOf(primero).multiply(BigInteger.valueOf(100))
                    .compareTo(t.multiply(BigInteger.valueOf(40))) >= 0
                && BigInteger.valueOf(primero - segundo).multiply(BigInteger.TEN).compareTo(t) >= 0);
        return new Resultado(gana ? Estado.PRIMERA_VUELTA : Estado.SEGUNDA_VUELTA,
                gana ? orden.get(0).idPartido() : null, lista, total);
    }
}

