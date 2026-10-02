package bo.edu.electoral.ui;

import bo.edu.electoral.config.DatabaseConnection;
import bo.edu.electoral.dao.*;
import bo.edu.electoral.service.*;
import bo.edu.electoral.stats.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;

/** Consola electoral: todos los cambios de votación pasan por el servicio. */
public final class MainConsola {
    private final Scanner entrada = new Scanner(System.in, StandardCharsets.UTF_8);
    private final ProcesoElectoralService proceso = new ProcesoElectoralService();
    private final ReporteElectoralDAO reportes = new ReporteElectoralDAO();
    public static void main(String[] args) {
        try { new MainConsola().iniciar(); }
        finally { DatabaseConnection.closeConnection(); }
    }
    private String texto(String mensaje) {
        System.out.print(mensaje);
        if (!entrada.hasNextLine()) throw new NoSuchElementException();
        return entrada.nextLine().trim();
    }
    private int entero(String mensaje) {
        while (true) {
            try { return Integer.parseInt(texto(mensaje)); }
            catch (NumberFormatException e) { System.out.println("Ingrese un entero válido."); }
        }
    }
    private void iniciar() {
        try {
            while (true) {
                System.out.println("""
                        
                        ===== SISTEMA ELECTORAL NACIONAL =====
                        1. Buscar ciudadano por CI
                        2. Marcar asistencia
                        3. Registrar una papeleta
                        4. Cerrar mesa y generar acta
                        5. Resultados nacionales y Ley 026
                        6. Resultados por departamento
                        7. Estadísticas de votos por partido
                        8. Preparación de datos / consultas
                        9. Listar mesas y partidos
                        0. Salir
                        """);
                int opcion = entero("Opción: ");
                try {
                    switch (opcion) {
                        case 0 -> { return; }
                        case 1 -> {
                            var ciudadano = new PadronCiudadanoDAO().findById(texto("CI: "));
                            System.out.println(ciudadano == null ? "CI no encontrado." :
                                    ciudadano + " | Mesa: " + ciudadano.getIdMesa() + " | Hora: " + ciudadano.getHoraSufragio());
                        }
                        case 2 -> {
                            proceso.marcarAsistencia(texto("CI: "));
                            System.out.println("Asistencia registrada.");
                        }
                        case 3 -> {
                            int mesa = entero("ID mesa: ");
                            String tipo = texto("Tipo (VALIDO/BLANCO/NULO): ").toUpperCase(Locale.ROOT);
                            Integer partido = tipo.equals("VALIDO") ? entero("ID partido: ") : null;
                            System.out.println("Papeleta registrada: " + proceso.registrarPapeleta(mesa,tipo,partido));
                        }
                        case 4 -> {
                            int mesa = entero("ID mesa a cerrar: ");
                            if (texto("El cierre bloqueará la mesa. Escriba CERRAR para confirmar: ").equals("CERRAR"))
                                System.out.println("Mesa computada. Acta: " + proceso.cerrarMesa(mesa));
                            else System.out.println("Cierre cancelado.");
                        }
                        case 5 -> resultados();
                        case 6 -> {
                            var filas = reportes.porDepartamento();
                            if (filas.isEmpty()) System.out.println("No hay votos válidos computados.");
                            for (var f : filas) System.out.printf("%s | %s | %d%n",f.departamento(),f.sigla(),f.votos());
                        }
                        case 7 -> estadisticas();
                        case 8 -> {
                            System.out.println("La edición de catálogos/padrón se permite antes de la primera asistencia. Actas, detalles y papeletas: solo consulta.");
                            new MantenimientoConsola(entrada).iniciar();
                        }
                        case 9 -> {
                            for (var m : new MesaDAO().findAll())
                                System.out.printf("Mesa ID %d | Número %d | Inscritos %d | %s%n",
                                        m.getIdMesa(),m.getNumeroMesa(),m.getCantidadInscritos(),m.getEstado());
                            for (var p : new PartidoPoliticoDAO().findAll())
                                System.out.printf("Partido ID %d | %s | %s%n",p.getIdPartido(),p.getSigla(),p.getNombreCompleto());
                        }
                        default -> System.out.println("Opción no válida.");
                    }
                } catch (SQLException e) {
                    System.out.println("No se pudo completar la operación: " + e.getMessage());
                } catch (IllegalArgumentException | ArithmeticException e) {
                    System.out.println("Datos no válidos: " + e.getMessage());
                }
            }
        } catch (NoSuchElementException e) { System.out.println("Fin de entrada."); }
    }
    private void resultados() throws SQLException {
        var resumen = reportes.nacional();
        System.out.println(resumen.completo() ? "Cómputo de todas las mesas no anuladas." : "CÓMPUTO PARCIAL: quedan mesas pendientes o no hay mesas computadas.");
        System.out.printf("Computadas: %d | Habilitadas: %d | Anuladas: %d%n",
                resumen.computadas(),resumen.habilitadas(),resumen.anuladas());
        System.out.printf("%-8s %-20s %12s %12s%n","ID","Partido","Votos","% válidos");
        Map<Integer,Long> votos = new LinkedHashMap<>();
        for (var p : resumen.partidos()) {
            System.out.printf("%-8d %-20s %12d %11.2f%%%n",p.idPartido(),p.sigla(),p.votos(),p.porcentaje());
            votos.put(p.idPartido(),p.votos());
        }
        System.out.printf("Válidos: %d | Blancos: %d | Nulos: %d | Votaron: %d%n",
                resumen.validos(),resumen.blancos(),resumen.nulos(),resumen.asistentes());
        var resultado = new MotorElectoralLey026().evaluar(votos);
        System.out.println("Aplicación académica de Ley 026 sobre el cómputo disponible:");
        switch (resultado.estado()) {
            case SIN_VOTOS_VALIDOS -> System.out.println("Sin votos válidos: no se determina ganador.");
            case PRIMERA_VUELTA -> {
                var ganador = resumen.partidos().stream().filter(p -> p.idPartido() == resultado.ganador()).findFirst().orElseThrow();
                System.out.println("Cumple primera vuelta: " + ganador.sigla() + (resumen.completo() ? "" : " (provisional)"));
            }
            case SEGUNDA_VUELTA -> {
                System.out.println("No se cumple primera vuelta; corresponde segunda vuelta.");
                var lista = resultado.clasificacion();
                if (lista.size() > 2 && lista.get(1).votos() == lista.get(2).votos())
                    System.out.println("Empate en la clasificación: no se resuelve por ID; requiere resolución electoral.");
                else System.out.println("Primeras candidaturas (IDs): " + lista.get(0).idPartido() + " y " + lista.get(1).idPartido());
            }
        }
    }
    private void estadisticas() throws SQLException {
        var resumen = reportes.nacional();
        double[] datos = new double[resumen.partidos().size()];
        if (datos.length == 0) { System.out.println("No hay partidos registrados."); return; }
        for (int i = 0; i < datos.length; i++) datos[i] = resumen.partidos().get(i).votos();
        System.out.println("Población: totales válidos por partido, incluidos partidos con cero votos; solo mesas computadas.");
        System.out.printf("Media %.4f | Mediana %.4f | Moda %s%n",MedidasPosicion.media(datos),
                MedidasPosicion.mediana(datos),Arrays.toString(MedidasPosicion.moda(datos)));
        System.out.printf("P25 %.4f | P75 %.4f | P90 %.4f%n",MedidasPosicion.percentil(datos,25),
                MedidasPosicion.percentil(datos,75),MedidasPosicion.percentil(datos,90));
        System.out.printf("Varianza poblacional %.4f | Desviación %.4f%n",
                MedidasDispersion.varianza(datos),MedidasDispersion.desviacionEstandar(datos));
        if (MedidasPosicion.media(datos) == 0) System.out.println("CV indefinido: media cero.");
        else System.out.printf("CV %.4f%%%n",MedidasDispersion.coeficienteVariacion(datos));
        System.out.println("Valor | Frecuencia absoluta | Relativa | Acumulada | Relativa acumulada");
        for (var f : DistribucionFrecuencia.calcular(datos))
            System.out.printf("%.0f | %d | %.4f | %d | %.4f%n",f.valor(),f.absoluta(),f.relativa(),f.acumulada(),f.relativaAcumulada());
        System.out.printf("Chebyshev k=2: al menos %.0f%% en %s%n",100*MedidasDispersion.chebyshev(2),
                Arrays.toString(MedidasDispersion.intervaloChebyshev(datos,2)));
    }
}

