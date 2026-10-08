package bo.edu.electoral.ui;

import bo.edu.electoral.config.DatabaseConnection;
import bo.edu.electoral.dao.*;
import bo.edu.electoral.service.*;
import bo.edu.electoral.stats.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;

/** Consola electoral: todos los cambios de votación pasan por el servicio. */
public final class 3
        MainConsola {
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
                try {
                    System.out.println("\n===== SISTEMA ELECTORAL CONTROLADO =====");
                    System.out.println("ESTADO: " + proceso.estado());
                    System.out.println("""
                        1. Crear ciudadano (antes de abrir)
                        2. Buscar ciudadano por CI
                        3. Listar ciudadanos
                        4. Crear partido político (antes de abrir)
                        5. Listar partidos políticos
                        6. ABRIR VOTACIONES
                        7. Marcar asistencia por CI
                        8. Registrar voto / papeleta
                        9. CERRAR VOTACIONES y generar actas
                        10. Resultados nacionales y Ley 026
                        11. Tabla de frecuencias absolutas y relativas
                        12. Estadísticas
                        13. Listar mesas
                        14. Resultados por departamento
                        0. Salir
                        """);
                    int opcion=entero("Opción: ");
                    switch(opcion) {
                        case 0 -> { return; }
                        case 1 -> {
                            proceso.crearCiudadano(texto("CI: "),texto("Nombres: "),texto("Apellidos: "),entero("ID mesa (inicial: 1): "));
                            System.out.println("Ciudadano creado. Verifique con la opción 2 o 3.");
                        }
                        case 2 -> {
                            var c=new PadronCiudadanoDAO().findById(texto("CI: ").toUpperCase(Locale.ROOT));
                            System.out.println(c==null ? "CI no encontrado." : c + " | Mesa: " + c.getIdMesa() + " | Asistencia: " + (c.isHaVotado()?"REGISTRADA":"PENDIENTE"));
                        }
                        case 3 -> {
                            var lista=new PadronCiudadanoDAO().findAll();
                            if(lista.isEmpty()) System.out.println("Todavía no hay ciudadanos. Use la opción 1.");
                            for(var c:lista) System.out.println(c + " | Mesa: " + c.getIdMesa());
                        }
                        case 4 -> System.out.println("Partido creado con ID " + proceso.crearPartido(texto("Sigla: "),texto("Nombre completo: "),texto("Candidato presidente: ")));
                        case 5 -> listarPartidos();
                        case 6 -> {
                            if(texto("Se bloqueará el registro de ciudadanos y partidos. Escriba ABRIR: ").equals("ABRIR")) {
                                proceso.abrirVotaciones(); System.out.println("VOTACIONES ABIERTAS. Use 7 para asistencia y 8 para papeletas.");
                            } else System.out.println("Apertura cancelada.");
                        }
                        case 7 -> { proceso.marcarAsistencia(texto("CI: ")); System.out.println("Asistencia registrada. Registre la papeleta con la opción 8."); }
                        case 8 -> {
                            int mesa=entero("ID mesa: "); listarPartidos();
                            String tipo=texto("Tipo (VALIDO/BLANCO/NULO): ").toUpperCase(Locale.ROOT);
                            Integer partido=tipo.equals("VALIDO")?entero("ID partido: "):null;
                            proceso.registrarPapeleta(mesa,tipo,partido);
                            System.out.println("Papeleta registrada. No se almacena el CI en el voto.");
                        }
                        case 9 -> {
                            if(texto("El cierre es definitivo. Escriba CERRAR: ").equals("CERRAR")) {
                                int cantidad=proceso.cerrarVotaciones();
                                System.out.println("VOTACIONES CERRADAS. Actas generadas: " + cantidad + ". Resultados: 10 y 11.");
                            } else System.out.println("Cierre cancelado.");
                        }
                        case 10 -> { if(resultadosDisponibles()) resultados(); }
                        case 11 -> { if(resultadosDisponibles()) System.out.print(TablaFrecuenciasPartidos.formatear(reportes.nacional())); }
                        case 12 -> { if(resultadosDisponibles()) estadisticas(); }
                        case 13 -> { for(var m:new MesaDAO().findAll()) System.out.printf("Mesa ID %d | Inscritos %d | %s%n",m.getIdMesa(),m.getCantidadInscritos(),m.getEstado()); }
                        case 14 -> { if(resultadosDisponibles()) for(var f:reportes.porDepartamento()) System.out.printf("%s | %s | %d%n",f.departamento(),f.sigla(),f.votos()); }
                        default -> System.out.println("Opción no válida.");
                    }
                } catch(SQLException e) {
                    System.out.println("No se completó la operación: " + e.getMessage());
                    if(texto("Enter para continuar, o 0 para salir: ").equals("0")) return;
                } catch(IllegalArgumentException | ArithmeticException e) { System.out.println(e.getMessage()); }
            }
        } catch(NoSuchElementException e) { System.out.println("Fin de entrada."); }
    }
    private void listarPartidos() throws SQLException {
        var lista=new PartidoPoliticoDAO().findAll();
        if(lista.isEmpty()) System.out.println("No hay partidos. Use la opción 4.");
        for(var p:lista) System.out.printf("ID %d | %s | %s | Candidato: %s%n",p.getIdPartido(),p.getSigla(),p.getNombreCompleto(),p.getCandidatoPresidente());
    }
    private boolean resultadosDisponibles() throws SQLException {
        if(!proceso.estado().equals("CERRADA")) {
            System.out.println("Los resultados se publican después del cierre. Use 9 al terminar la votación."); return false;
        }
        return true;
    }
    private void resultados() throws SQLException {
        var resumen = reportes.nacional();
        System.out.println(resumen.completo() ? "Cómputo de todas las mesas no anuladas." : "CÓMPUTO PARCIAL: quedan mesas pendientes o no hay mesas computadas.");
        System.out.printf("Computadas: %d | Habilitadas: %d | Anuladas: %d%n",
                resumen.computadas(),resumen.habilitadas(),resumen.anuladas());
        System.out.print(TablaFrecuenciasPartidos.formatear(resumen));
        Map<Integer,Long> votos = new LinkedHashMap<>();
        for (var p : resumen.partidos()) {
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
        System.out.print(TablaFrecuenciasPartidos.formatear(resumen));
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
        System.out.println("Distribución de los totales: aquí la frecuencia cuenta partidos con igual cantidad de votos.");
        System.out.println("Valor | Frecuencia absoluta | Relativa | Acumulada | Relativa acumulada");
        for (var f : DistribucionFrecuencia.calcular(datos))
            System.out.printf("%.0f | %d | %.4f | %d | %.4f%n",f.valor(),f.absoluta(),f.relativa(),f.acumulada(),f.relativaAcumulada());
        System.out.printf("Chebyshev k=2: al menos %.0f%% en %s%n",100*MedidasDispersion.chebyshev(2),
                Arrays.toString(MedidasDispersion.intervaloChebyshev(datos,2)));
    }
}
