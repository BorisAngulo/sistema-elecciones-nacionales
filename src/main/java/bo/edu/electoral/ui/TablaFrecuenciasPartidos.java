package bo.edu.electoral.ui;

import bo.edu.electoral.dao.ReporteElectoralDAO.Resumen;
import java.util.Formatter;
import java.util.Locale;

/** Frecuencias de votos por categoría; los partidos no tienen orden acumulativo. */
public final class TablaFrecuenciasPartidos {
    private TablaFrecuenciasPartidos() {}

    public static String formatear(Resumen resumen) {
        StringBuilder salida = new StringBuilder();
        try (Formatter f = new Formatter(salida, Locale.ROOT)) {
            f.format("%nTABLA DE FRECUENCIAS POR PARTIDO%n");
            f.format("%s | Mesas computadas: %d | Pendientes: %d | Anuladas: %d%n",
                    resumen.completo() ? "Cómputo completo" : "Cómputo parcial",
                    resumen.computadas(), resumen.habilitadas(), resumen.anuladas());
            f.format("Solo actas de mesas COMPUTADAS. Los votos de mesas abiertas aún no aparecen.%n");
            f.format("%-6s | %-20s | %14s | %14s | %10s%n",
                    "ID", "Partido", "Absoluta (fi)", "Relativa (hi)", "Porcentaje");
            f.format("%s%n", "-".repeat(78));
            for (var partido : resumen.partidos()) {
                fila(f, Integer.toString(partido.idPartido()), partido.sigla(), partido.votos(), resumen.validos());
            }
            fila(f, "", "TOTAL VÁLIDOS", resumen.validos(), resumen.validos());
            f.format("fi = votos del partido; hi = fi / total de votos válidos; porcentaje = hi × 100.%n");
            if (resumen.validos() == 0)
                f.format("Sin votos válidos computados: la frecuencia relativa y el porcentaje no están definidos (—).%n");
            long total = Math.addExact(resumen.validos(), Math.addExact(resumen.blancos(), resumen.nulos()));
            f.format("%nRESUMEN DE PAPELETAS (porcentajes sobre todas las papeletas computadas)%n");
            fila(f, "", "VÁLIDOS", resumen.validos(), total);
            fila(f, "", "BLANCOS", resumen.blancos(), total);
            fila(f, "", "NULOS", resumen.nulos(), total);
            fila(f, "", "TOTAL PAPELETAS", total, total);
            f.format("Los porcentajes se redondean a dos decimales.%n");
        }
        return salida.toString();
    }

    private static void fila(Formatter f, String id, String nombre, long votos, long total) {
        String relativa = total == 0 ? "—" : String.format(Locale.ROOT, "%.4f", (double) votos / total);
        String porcentaje = total == 0 ? "—" : String.format(Locale.ROOT, "%.2f%%", votos * 100.0 / total);
        f.format("%-6s | %-20s | %14d | %14s | %10s%n", id, nombre, votos, relativa, porcentaje);
    }
}
