package bo.edu.electoral.stats;

public final class MedidasDispersion {
    private MedidasDispersion() {}
    public static double varianza(double[] datos) { return varianza(datos, false); }
    /** Poblacional: divisor n; muestral: divisor n-1. */
    public static double varianza(double[] datos, boolean muestral) {
        double media = MedidasPosicion.media(datos);
        if (muestral && datos.length < 2) throw new IllegalArgumentException("La muestra necesita dos datos");
        double suma = 0;
        for (double x : datos) { double d = x - media; suma += d * d; }
        return suma / (datos.length - (muestral ? 1 : 0));
    }
    public static double desviacionEstandar(double[] datos) { return Math.sqrt(varianza(datos)); }
    /** Porcentaje respecto del valor absoluto de la media. */
    public static double coeficienteVariacion(double[] datos) {
        double media = MedidasPosicion.media(datos);
        if (media == 0) throw new IllegalArgumentException("CV indefinido con media cero");
        return desviacionEstandar(datos) / Math.abs(media) * 100;
    }
    /** Proporción mínima garantizada dentro de k desviaciones, k > 1. */
    public static double chebyshev(double k) {
        if (!Double.isFinite(k) || k <= 1) throw new IllegalArgumentException("k debe ser finito y mayor que 1");
        return 1 - 1 / (k * k);
    }
    public static double[] intervaloChebyshev(double[] datos, double k) {
        chebyshev(k);
        double media = MedidasPosicion.media(datos), radio = k * desviacionEstandar(datos);
        return new double[]{media - radio, media + radio};
    }
}

