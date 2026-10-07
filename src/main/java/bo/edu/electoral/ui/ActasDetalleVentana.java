package bo.edu.electoral.ui;

import bo.edu.electoral.model.Acta;
import bo.edu.electoral.model.DetalleVoto;
import bo.edu.electoral.model.PartidoPolitico;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.JTableHeader;
import javax.swing.table.DefaultTableModel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActasDetalleVentana extends JFrame {

    private static final Color FONDO = new Color(31, 41, 55);
    private static final Color FONDO_CABECERA = new Color(17, 24, 39);
    private static final Color TEXTO = Color.WHITE;
    private static final Color TITULO = new Color(250, 204, 21);
    private static final Color CUADRICULA = new Color(75, 85, 99);

    public ActasDetalleVentana(List<Acta> actas, List<DetalleVoto> detalles, List<PartidoPolitico> partidos) {
        super("Actas y detalle de votos consolidado");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        DefaultTableModel model = construirModelo(actas, detalles, partidos);
        JTable tabla = new JTable(model);
        tabla.setFillsViewportHeight(true);
        tabla.setRowHeight(32);
        tabla.setBackground(FONDO);
        tabla.setForeground(TEXTO);
        tabla.setSelectionBackground(new Color(55, 65, 81));
        tabla.setSelectionForeground(TEXTO);
        tabla.setGridColor(CUADRICULA);
        tabla.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JTableHeader cabecera = tabla.getTableHeader();
        cabecera.setBackground(FONDO_CABECERA);
        cabecera.setForeground(TITULO);
        cabecera.setFont(new Font("SansSerif", Font.BOLD, 14));
        cabecera.setPreferredSize(new Dimension(cabecera.getPreferredSize().width, 38));
        ajustarAnchosColumnas(tabla);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.getViewport().setBackground(FONDO);
        scroll.setPreferredSize(new Dimension(1450, 760));
        setContentPane(scroll);
        setSize(1500, 820);
        setLocationRelativeTo(null);
    }

    public static void mostrar(List<Acta> actas, List<DetalleVoto> detalles, List<PartidoPolitico> partidos) {
        SwingUtilities.invokeLater(() -> {
            ActasDetalleVentana ventana = new ActasDetalleVentana(actas, detalles, partidos);
            ventana.setVisible(true);
        });
    }

    private void ajustarAnchosColumnas(JTable tabla) {
        FontMetrics metricas = tabla.getFontMetrics(tabla.getFont());
        FontMetrics metricasCabecera = tabla.getTableHeader()
                .getFontMetrics(tabla.getTableHeader().getFont());

        for (int columna = 0; columna < tabla.getColumnCount(); columna++) {
            int ancho = metricasCabecera.stringWidth(
                    tabla.getColumnModel().getColumn(columna).getHeaderValue().toString()) + 36;
            for (int fila = 0; fila < tabla.getRowCount(); fila++) {
                Object valor = tabla.getValueAt(fila, columna);
                if (valor != null) {
                    ancho = Math.max(ancho, metricas.stringWidth(valor.toString()) + 28);
                }
            }
            tabla.getColumnModel().getColumn(columna).setPreferredWidth(Math.max(90, ancho));
        }
    }

    private DefaultTableModel construirModelo(List<Acta> actas, List<DetalleVoto> detalles, List<PartidoPolitico> partidos) {
        Map<Integer, Map<Integer, Integer>> votosPorActaYPartido = new HashMap<>();
        for (DetalleVoto detalle : detalles) {
            votosPorActaYPartido
                    .computeIfAbsent(detalle.getIdActa(), key -> new HashMap<>())
                    .merge(detalle.getIdPartido(), detalle.getVotosValidos(), Integer::sum);
        }

        String[] columnas = new String[6 + partidos.size()];
        columnas[0] = "Acta";
        columnas[1] = "Mesa";
        columnas[2] = "Votaron";
        columnas[3] = "Válidos";
        columnas[4] = "Blancos";
        columnas[5] = "Nulos";
        for (int i = 0; i < partidos.size(); i++) {
            columnas[6 + i] = partidos.get(i).getSigla();
        }

        Object[][] filas = new Object[actas.size()][columnas.length];
        int filaIndex = 0;
        for (Acta acta : actas) {
            Map<Integer, Integer> votosPartidos = votosPorActaYPartido.getOrDefault(acta.getIdActa(), Map.of());
            int votosValidos = 0;
            for (Integer votos : votosPartidos.values()) {
                votosValidos += votos;
            }

            filas[filaIndex][0] = acta.getIdActa();
            filas[filaIndex][1] = acta.getIdMesa();
            filas[filaIndex][2] = acta.getTotalCiudadanosVotaron();
            filas[filaIndex][3] = votosValidos;
            filas[filaIndex][4] = acta.getVotosBlancos();
            filas[filaIndex][5] = acta.getVotosNulos();

            for (int i = 0; i < partidos.size(); i++) {
                PartidoPolitico partido = partidos.get(i);
                filas[filaIndex][6 + i] = votosPartidos.getOrDefault(partido.getIdPartido(), 0);
            }
            filaIndex++;
        }

        return new DefaultTableModel(filas, columnas) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }
}
