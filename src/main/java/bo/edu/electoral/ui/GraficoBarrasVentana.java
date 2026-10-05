package bo.edu.electoral.ui;

import bo.edu.electoral.service.ResultadoSimulacionService.ComputoTotal;
import bo.edu.electoral.service.ResultadoSimulacionService.FilaResultado;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

/**
 * Ventana Swing con un gráfico de barras de los resultados totales.
 */
public class GraficoBarrasVentana extends JFrame {

    private static final Color[] COLORES = {
            new Color(37, 99, 235),
            new Color(220, 38, 38),
            new Color(22, 163, 74),
            new Color(217, 119, 6),
            new Color(124, 58, 237),
            new Color(13, 148, 136),
            new Color(225, 29, 72),
            new Color(75, 85, 99)
    };

    public GraficoBarrasVentana(ComputoTotal computo) {
        super("Resultados totales");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setContentPane(new PanelBarras(computo));
        pack();
        setLocationRelativeTo(null);
    }

    public static void mostrar(ComputoTotal computo) {
        SwingUtilities.invokeLater(() -> {
            GraficoBarrasVentana ventana = new GraficoBarrasVentana(computo);
            ventana.setVisible(true);
        });
    }

    private static class Barra {
        private final String etiqueta;
        private final int votos;
        private final Color color;

        private Barra(String etiqueta, int votos, Color color) {
            this.etiqueta = etiqueta;
            this.votos = votos;
            this.color = color;
        }
    }

    private static class PanelBarras extends JPanel {

        private final List<Barra> barras;
        private final int maximo;

        private PanelBarras(ComputoTotal computo) {
            setPreferredSize(new Dimension(820, 480));
            setBackground(Color.WHITE);
            barras = new ArrayList<>();
            int i = 0;
            for (FilaResultado fila : computo.partidos()) {
                barras.add(new Barra(fila.sigla(), fila.votos(), COLORES[i % COLORES.length]));
                i++;
            }
            barras.add(new Barra("Blancos", computo.votosBlancos(), new Color(156, 163, 175)));
            barras.add(new Barra("Nulos", computo.votosNulos(), new Color(55, 65, 81)));

            int mayor = 1;
            for (Barra barra : barras) {
                if (barra.votos > mayor) {
                    mayor = barra.votos;
                }
            }
            maximo = mayor;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int margenIzq = 50;
            int margenDer = 30;
            int margenSup = 60;
            int margenInf = 70;
            int anchoUtil = getWidth() - margenIzq - margenDer;
            int altoUtil = getHeight() - margenSup - margenInf;

            g2.setColor(new Color(17, 24, 39));
            g2.setFont(new Font("SansSerif", Font.BOLD, 18));
            g2.drawString("Resultados totales de la simulación", margenIzq, 32);

            g2.setColor(new Color(107, 114, 128));
            g2.drawLine(margenIzq, margenSup, margenIzq, margenSup + altoUtil);
            g2.drawLine(margenIzq, margenSup + altoUtil, margenIzq + anchoUtil, margenSup + altoUtil);

            int cantidad = barras.size();
            int hueco = 16;
            int anchoBarra = Math.max(24, (anchoUtil - hueco * (cantidad + 1)) / cantidad);

            for (int indice = 0; indice < cantidad; indice++) {
                Barra barra = barras.get(indice);
                int altoBarra = (int) Math.round(barra.votos * (double) altoUtil / maximo);
                int x = margenIzq + hueco + indice * (anchoBarra + hueco);
                int y = margenSup + altoUtil - altoBarra;

                g2.setColor(barra.color);
                g2.fillRect(x, y, anchoBarra, Math.max(altoBarra, 0));

                g2.setColor(new Color(17, 24, 39));
                g2.setFont(new Font("SansSerif", Font.BOLD, 12));
                String valor = String.valueOf(barra.votos);
                int anchoValor = g2.getFontMetrics().stringWidth(valor);
                g2.drawString(valor, x + (anchoBarra - anchoValor) / 2, y - 6);

                g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
                int anchoEtiqueta = g2.getFontMetrics().stringWidth(barra.etiqueta);
                g2.drawString(barra.etiqueta, x + (anchoBarra - anchoEtiqueta) / 2, margenSup + altoUtil + 22);
            }

            g2.dispose();
        }
    }
}
