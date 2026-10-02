package bo.edu.electoral.stats;

import java.util.Arrays;

public final class DistribucionFrecuencia {
    public record Frecuencia(double valor, int absoluta, double relativa, int acumulada,
                             double relativaAcumulada) {}
    private DistribucionFrecuencia() {}
    public static Frecuencia[] calcular(double[] datos) {
        double[] a = MedidasPosicion.ordenar(datos);
        Frecuencia[] filas = new Frecuencia[a.length];
        int n = 0, acumulada = 0;
        for (int i = 0; i < a.length;) {
            int j = i + 1;
            while (j < a.length && a[j] == a[i]) j++;
            int frecuencia = j - i;
            acumulada += frecuencia;
            filas[n++] = new Frecuencia(a[i], frecuencia, (double) frecuencia / a.length,
                    acumulada, (double) acumulada / a.length);
            i = j;
        }
        return Arrays.copyOf(filas, n);
    }
}

