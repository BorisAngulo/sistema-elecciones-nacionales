package bo.edu.electoral.stats;

import java.util.Arrays;

public final class MedidasPosicion {
    private MedidasPosicion() {}
    static double[] ordenar(double[] datos) {
        if (datos == null || datos.length == 0) throw new IllegalArgumentException("Se requieren datos");
        double[] copia = datos.clone();
        for (double x : copia) if (!Double.isFinite(x))
            throw new IllegalArgumentException("Los datos deben ser finitos");
        Arrays.sort(copia);
        return copia;
    }
    public static double media(double[] datos) {
        ordenar(datos);
        double media = 0;
        for (double x : datos) media += x / datos.length;
        return media;
    }
    public static double mediana(double[] datos) { return percentil(datos, 50); }
    /** Interpolación lineal: posición (n-1)*p/100, incluidos p=0 y p=100. */
    public static double percentil(double[] datos, double p) {
        if (!Double.isFinite(p) || p < 0 || p > 100) throw new IllegalArgumentException("Percentil entre 0 y 100");
        double[] a = ordenar(datos);
        double pos = (a.length - 1) * (p / 100);
        int i = (int) pos;
        double fraccion = pos - i;
        return a[i] * (1 - fraccion) + a[Math.min(i + 1, a.length - 1)] * fraccion;
    }
    /** Devuelve todas las modas; arreglo vacío si todos los valores aparecen una vez. */
    public static double[] moda(double[] datos) {
        var f = DistribucionFrecuencia.calcular(datos);
        int max = 1, n = 0;
        for (var fila : f) max = Math.max(max, fila.absoluta());
        if (max == 1) return new double[0];
        for (var fila : f) if (fila.absoluta() == max) n++;
        double[] resultado = new double[n];
        int i = 0;
        for (var fila : f) if (fila.absoluta() == max) resultado[i++] = fila.valor();
        return resultado;
    }
}

