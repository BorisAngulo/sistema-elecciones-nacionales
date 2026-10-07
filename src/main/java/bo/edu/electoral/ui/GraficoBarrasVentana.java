package bo.edu.electoral.ui;

import bo.edu.electoral.service.ResultadoSimulacionService.ComputoTotal;
import bo.edu.electoral.service.ResultadoSimulacionService.FilaResultado;
import bo.edu.electoral.service.ResultadoSimulacionService.InformeEstadistico;
import bo.edu.electoral.service.ResultadoSimulacionService.ResultadoDepartamento;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Ventana de resultados con filtros por departamento y totales de votos.
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

    private final JPanel panelGrafico = new JPanel(new BorderLayout());

    public GraficoBarrasVentana(InformeEstadistico informe) {
        super("Estadísticas electorales por departamento");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        List<OpcionDepartamento> opciones = new ArrayList<>();
        opciones.add(new OpcionDepartamento("Nacional", informe.nacional()));
        for (ResultadoDepartamento departamento : informe.departamentos()) {
            opciones.add(new OpcionDepartamento(departamento.departamento(), departamento.computo()));
        }

        JComboBox<OpcionDepartamento> filtro = new JComboBox<>(opciones.toArray(OpcionDepartamento[]::new));
        filtro.addActionListener(event -> {
            OpcionDepartamento seleccion = (OpcionDepartamento) filtro.getSelectedItem();
            if (seleccion != null) {
                mostrarGrafico(seleccion);
            }
        });

        JPanel controles = new JPanel(new BorderLayout(12, 0));
        controles.add(new JLabel("Filtrar resultados por departamento:"), BorderLayout.WEST);
        controles.add(filtro, BorderLayout.CENTER);

        JPanel contenido = new JPanel(new BorderLayout(12, 12));
        contenido.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 16, 12, 16));
        contenido.add(controles, BorderLayout.NORTH);
        contenido.add(panelGrafico, BorderLayout.CENTER);
        setContentPane(contenido);
        setSize(980, 640);
        setLocationRelativeTo(null);

        mostrarGrafico(opciones.get(0));
    }

    public static void mostrar(InformeEstadistico informe) {
        SwingUtilities.invokeLater(() -> {
            GraficoBarrasVentana ventana = new GraficoBarrasVentana(informe);
            ventana.setVisible(true);
        });
    }

    private void mostrarGrafico(OpcionDepartamento seleccion) {
        panelGrafico.removeAll();
        panelGrafico.add(new PanelBarras(seleccion.nombre, seleccion.computo), BorderLayout.CENTER);
        panelGrafico.revalidate();
        panelGrafico.repaint();
    }

    private record OpcionDepartamento(String nombre, ComputoTotal computo) {
        @Override
        public String toString() {
            return nombre;
        }
    }

    private static class Barra {
        private final String etiqueta;
        private final int votos;
        private final double porcentaje;
        private final Color color;

        private Barra(String etiqueta, int votos, double porcentaje, Color color) {
            this.etiqueta = etiqueta;
            this.votos = votos;
            this.porcentaje = porcentaje;
            this.color = color;
        }
    }

    private static class PanelBarras extends JPanel {

        private final List<Barra> barras;
        private final int maximo;
        private final String titulo;
        private final ComputoTotal computo;

        private PanelBarras(String titulo, ComputoTotal computo) {
            this.titulo = titulo;
            this.computo = computo;
            setPreferredSize(new Dimension(920, 540));
            setBackground(Color.WHITE);
            barras = new ArrayList<>();
            int i = 0;
            for (FilaResultado fila : computo.partidos()) {
                barras.add(new Barra(fila.sigla(), fila.votos(), fila.porcentajeSobreValidos(),
                        COLORES[i++ % COLORES.length]));
            }
            double porcentajeBlancos = porcentaje(computo.votosBlancos(), computo.totalPapeletas());
            double porcentajeNulos = porcentaje(computo.votosNulos(), computo.totalPapeletas());
            barras.add(new Barra("Blancos", computo.votosBlancos(), porcentajeBlancos,
                    new Color(156, 163, 175)));
            barras.add(new Barra("Nulos", computo.votosNulos(), porcentajeNulos,
                    new Color(55, 65, 81)));

            int mayor = 1;
            for (Barra barra : barras) {
                mayor = Math.max(mayor, barra.votos);
            }
            maximo = mayor;
        }

        private static double porcentaje(int parte, int total) {
            return total == 0 ? 0 : parte * 100.0 / total;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int margenIzq = 55;
            int margenDer = 30;
            int margenSup = 62;
            int margenInf = 145;
            int anchoUtil = getWidth() - margenIzq - margenDer;
            int altoUtil = getHeight() - margenSup - margenInf;

            g2.setColor(new Color(17, 24, 39));
            g2.setFont(new Font("SansSerif", Font.BOLD, 18));
            g2.drawString("Resultados electorales - " + titulo, margenIzq, 32);

            g2.setColor(new Color(107, 114, 128));
            g2.drawLine(margenIzq, margenSup, margenIzq, margenSup + altoUtil);
            g2.drawLine(margenIzq, margenSup + altoUtil, margenIzq + anchoUtil, margenSup + altoUtil);

            int cantidad = barras.size();
            int hueco = Math.max(8, Math.min(18, anchoUtil / (cantidad * 5)));
            int anchoBarra = Math.max(24, (anchoUtil - hueco * (cantidad + 1)) / cantidad);

            for (int indice = 0; indice < cantidad; indice++) {
                Barra barra = barras.get(indice);
                int altoBarra = (int) Math.round(barra.votos * (double) altoUtil / maximo);
                int x = margenIzq + hueco + indice * (anchoBarra + hueco);
                int y = margenSup + altoUtil - altoBarra;

                g2.setColor(barra.color);
                g2.fillRect(x, y, anchoBarra, Math.max(altoBarra, 0));

                g2.setColor(new Color(17, 24, 39));
                g2.setFont(new Font("SansSerif", Font.BOLD, 11));
                dibujarCentrado(g2, NumberFormat.getIntegerInstance(Locale.forLanguageTag("es-BO"))
                        .format(barra.votos), x + anchoBarra / 2, y - 18);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
                dibujarCentrado(g2, String.format(Locale.ROOT, "%.1f%%", barra.porcentaje),
                        x + anchoBarra / 2, y - 4);
                dibujarCentrado(g2, barra.etiqueta, x + anchoBarra / 2, margenSup + altoUtil + 22);
            }

            int pie = getHeight() - 83;
            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2.setColor(new Color(17, 24, 39));
            g2.drawString("Totales", margenIzq, pie);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
            String totales = "Votos válidos: " + computo.votosValidos()
                    + "     Blancos: " + computo.votosBlancos()
                    + "     Nulos: " + computo.votosNulos()
                    + "     Total de papeletas: " + computo.totalPapeletas();
            g2.drawString(totales, margenIzq, pie + 24);
            g2.setColor(new Color(107, 114, 128));
            g2.drawString("Porcentaje de partidos sobre votos válidos; blancos y nulos sobre el total de papeletas.",
                    margenIzq, pie + 46);

            g2.dispose();
        }

        private void dibujarCentrado(Graphics2D g2, String texto, int centroX, int y) {
            int anchoTexto = g2.getFontMetrics().stringWidth(texto);
            g2.drawString(texto, centroX - anchoTexto / 2, y);
        }
    }
}
