package bo.edu.electoral.ui;

import bo.edu.electoral.config.DatabaseConnection;
import bo.edu.electoral.dao.ActaDAO;
import bo.edu.electoral.dao.DepartamentoDAO;
import bo.edu.electoral.dao.DetalleVotoDAO;
import bo.edu.electoral.dao.MesaDAO;
import bo.edu.electoral.dao.MunicipioDAO;
import bo.edu.electoral.dao.PadronCiudadanoDAO;
import bo.edu.electoral.dao.PapeletaEscrutinioDAO;
import bo.edu.electoral.dao.PartidoPoliticoDAO;
import bo.edu.electoral.dao.RecintoDAO;
import bo.edu.electoral.model.Acta;
import bo.edu.electoral.model.Departamento;
import bo.edu.electoral.model.DetalleVoto;
import bo.edu.electoral.model.Mesa;
import bo.edu.electoral.model.Municipio;
import bo.edu.electoral.model.PadronCiudadano;
import bo.edu.electoral.model.PapeletaEscrutinio;
import bo.edu.electoral.model.PartidoPolitico;
import bo.edu.electoral.model.Recinto;
import bo.edu.electoral.service.CierreMesaService;
import bo.edu.electoral.service.ResultadoElectoralService;
import bo.edu.electoral.service.ResultadoFinalService;
import bo.edu.electoral.service.ResultadoSimulacionService;
import bo.edu.electoral.service.SimuladorVotacionService;
import bo.edu.electoral.service.VotacionException;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Menú de consola para mantenimiento de datos, simulación y resultados electorales.
 */
public class MainConsola {

    private final Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);
    private final DepartamentoDAO departamentoDAO = new DepartamentoDAO();
    private final MunicipioDAO municipioDAO = new MunicipioDAO();
    private final RecintoDAO recintoDAO = new RecintoDAO();
    private final MesaDAO mesaDAO = new MesaDAO();
    private final PartidoPoliticoDAO partidoDAO = new PartidoPoliticoDAO();
    private final ActaDAO actaDAO = new ActaDAO();
    private final DetalleVotoDAO detalleVotoDAO = new DetalleVotoDAO();
    private final PadronCiudadanoDAO padronDAO = new PadronCiudadanoDAO();
    private final PapeletaEscrutinioDAO papeletaDAO = new PapeletaEscrutinioDAO();
    private final SimuladorVotacionService votacionService = new SimuladorVotacionService();
    private final ResultadoSimulacionService resultadoService = new ResultadoSimulacionService();
    private final ResultadoElectoralService resultadoElectoralService = new ResultadoElectoralService();
    private final ResultadoFinalService resultadoFinalService = new ResultadoFinalService();
    private final CierreMesaService cierreMesaService = new CierreMesaService();

    public static void main(String[] args) {
        try {
            DatabaseConnection.getConnection();
            System.out.println("Conectado a PostgreSQL.");
            new MainConsola().iniciar();
        } catch (SQLException e) {
            System.err.println("No se pudo conectar: " + e.getMessage());
        } finally {
            DatabaseConnection.closeConnection();
        }
    }

    private void iniciar() {
        boolean continuar = true;
        while (continuar) {
            System.out.println();
            System.out.println("===== SISTEMA ELECTORAL  =====");
            System.out.println();
            System.out.println("PREPARAR ELECCION---");
            System.out.println("1. Departamentos");
            System.out.println("2. Municipios");
            System.out.println("3. Recintos");
            System.out.println("4. Mesas");
            System.out.println("5. Partidos políticos");
            System.out.println("6. Padrón ciudadano");
            System.out.println();
            System.out.println("SEGUIMIENTO DE ELECCION ---");
            System.out.println("7. Simular votación");
            System.out.println("8. Resultados de la votación (gráficos)");
            System.out.println();
            System.out.println("CIERRE Y RESULTADOS OFICIALES ---");
            System.out.println("9. Cerrar mesa e iniciar escrutinio oficial");
            System.out.println("10. Resultados oficiales por partido y departamento");
            System.out.println("11. Resultado final y conclusión electoral (Ley 026)");
            System.out.println();
            System.out.println("AUDITORIA  ---");
            System.out.println("12. Papeletas de escrutinio");
            System.out.println("13. Actas y detalle de votos");
            System.out.println("0. Salir");
            int opcion = leerEntero("Opción: ");
            switch (opcion) {
                case 1 -> menuDepartamentos();
                case 2 -> menuMunicipios();
                case 3 -> menuRecintos();
                case 4 -> menuMesas();
                case 5 -> menuPartidos();
                case 6 -> menuPadron();
                case 7 -> simularVotacion();
                case 8 -> mostrarResultadosTotales();
                case 9 -> cerrarMesa();
                case 10 -> mostrarResultadosOficiales();
                case 11 -> mostrarResultadosFinales();
                case 12 -> menuPapeletas();
                case 13, 14 -> mostrarActasYDetalleConsolidado();
                case 0 -> continuar = false;
                default -> System.out.println("Opción no válida.");
            }
        }
        System.out.println("Hasta luego.");
    }

    private void menuDepartamentos() {
        while (true) {
            System.out.println();
            System.out.println("--- Departamentos ---");
            imprimirCrud();
            int opcion = leerEntero("Opción: ");
            try {
                switch (opcion) {
                    case 1 -> {
                        for (Departamento d : departamentoDAO.findAll()) {
                            System.out.println(d.getIdDepartamento() + " | " + d.getNombre());
                        }
                    }
                    case 2 -> {
                        Departamento d = departamentoDAO.findById(leerEntero("ID: "));
                        System.out.println(d == null ? "No encontrado." : d.getIdDepartamento() + " | " + d.getNombre());
                    }
                    case 3 -> {
                        Departamento d = new Departamento();
                        d.setNombre(leerTexto("Nombre: "));
                        int id = departamentoDAO.insert(d);
                        System.out.println("Creado con ID " + id);
                    }
                    case 4 -> {
                        Departamento d = departamentoDAO.findById(leerEntero("ID a editar: "));
                        if (d == null) {
                            System.out.println("No encontrado.");
                            break;
                        }
                        d.setNombre(leerTextoConDefecto("Nombre", d.getNombre()));
                        System.out.println(departamentoDAO.update(d) ? "Actualizado." : "No se actualizó.");
                    }
                    case 5 -> System.out.println(
                            departamentoDAO.delete(leerEntero("ID a eliminar: ")) ? "Eliminado." : "No encontrado.");
                    case 0 -> {
                        return;
                    }
                    default -> System.out.println("Opción no válida.");
                }
            } catch (SQLException e) {
                mostrarError(e);
            }
        }
    }

    private void menuMunicipios() {
        while (true) {
            System.out.println();
            System.out.println("--- Municipios ---");
            imprimirCrud();
            int opcion = leerEntero("Opción: ");
            try {
                switch (opcion) {
                    case 1 -> {
                        for (Municipio m : municipioDAO.findAll()) {
                            System.out.println(m.getIdMunicipio() + " | " + m.getNombre()
                                    + " | depto " + m.getIdDepartamento());
                        }
                    }
                    case 2 -> {
                        Municipio m = municipioDAO.findById(leerEntero("ID: "));
                        System.out.println(m == null ? "No encontrado."
                                : m.getIdMunicipio() + " | " + m.getNombre() + " | depto " + m.getIdDepartamento());
                    }
                    case 3 -> {
                        Municipio m = new Municipio();
                        m.setNombre(leerTexto("Nombre: "));
                        m.setIdDepartamento(leerEntero("ID departamento: "));
                        System.out.println("Creado con ID " + municipioDAO.insert(m));
                    }
                    case 4 -> {
                        Municipio m = municipioDAO.findById(leerEntero("ID a editar: "));
                        if (m == null) {
                            System.out.println("No encontrado.");
                            break;
                        }
                        m.setNombre(leerTextoConDefecto("Nombre", m.getNombre()));
                        m.setIdDepartamento(leerEnteroConDefecto("ID departamento", m.getIdDepartamento()));
                        System.out.println(municipioDAO.update(m) ? "Actualizado." : "No se actualizó.");
                    }
                    case 5 -> System.out.println(
                            municipioDAO.delete(leerEntero("ID a eliminar: ")) ? "Eliminado." : "No encontrado.");
                    case 0 -> {
                        return;
                    }
                    default -> System.out.println("Opción no válida.");
                }
            } catch (SQLException e) {
                mostrarError(e);
            }
        }
    }

    private void menuRecintos() {
        while (true) {
            System.out.println();
            System.out.println("--- Recintos ---");
            imprimirCrud();
            int opcion = leerEntero("Opción: ");
            try {
                switch (opcion) {
                    case 1 -> {
                        for (Recinto r : recintoDAO.findAll()) {
                            System.out.println(r.getIdRecinto() + " | " + r.getNombre()
                                    + " | municipio " + r.getIdMunicipio());
                        }
                    }
                    case 2 -> {
                        Recinto r = recintoDAO.findById(leerEntero("ID: "));
                        System.out.println(r == null ? "No encontrado."
                                : r.getIdRecinto() + " | " + r.getNombre() + " | municipio " + r.getIdMunicipio());
                    }
                    case 3 -> {
                        Recinto r = new Recinto();
                        r.setNombre(leerTexto("Nombre: "));
                        r.setIdMunicipio(leerEntero("ID municipio: "));
                        System.out.println("Creado con ID " + recintoDAO.insert(r));
                    }
                    case 4 -> {
                        Recinto r = recintoDAO.findById(leerEntero("ID a editar: "));
                        if (r == null) {
                            System.out.println("No encontrado.");
                            break;
                        }
                        r.setNombre(leerTextoConDefecto("Nombre", r.getNombre()));
                        r.setIdMunicipio(leerEnteroConDefecto("ID municipio", r.getIdMunicipio()));
                        System.out.println(recintoDAO.update(r) ? "Actualizado." : "No se actualizó.");
                    }
                    case 5 -> System.out.println(
                            recintoDAO.delete(leerEntero("ID a eliminar: ")) ? "Eliminado." : "No encontrado.");
                    case 0 -> {
                        return;
                    }
                    default -> System.out.println("Opción no válida.");
                }
            } catch (SQLException e) {
                mostrarError(e);
            }
        }
    }

    private void menuMesas() {
        while (true) {
            System.out.println();
            System.out.println("--- Mesas ---");
            imprimirCrud();
            int opcion = leerEntero("Opción: ");
            try {
                switch (opcion) {
                    case 1 -> {
                        for (Mesa m : mesaDAO.findAll()) {
                            imprimirMesa(m);
                        }
                    }
                    case 2 -> {
                        Mesa m = mesaDAO.findById(leerEntero("ID: "));
                        if (m == null) {
                            System.out.println("No encontrada.");
                        } else {
                            imprimirMesa(m);
                        }
                    }
                    case 3 -> {
                        Mesa m = new Mesa();
                        m.setNumeroMesa(leerEntero("Número de mesa: "));
                        m.setIdRecinto(leerEntero("ID recinto: "));
                        m.setCantidadInscritos(leerEnteroConDefecto("Inscritos", 240));
                        m.setEstado(leerTextoConDefecto("Estado (HABILITADA/COMPUTADA/ANULADA)", Mesa.ESTADO_HABILITADA));
                        System.out.println("Creada con ID " + mesaDAO.insert(m));
                    }
                    case 4 -> {
                        Mesa m = mesaDAO.findById(leerEntero("ID a editar: "));
                        if (m == null) {
                            System.out.println("No encontrada.");
                            break;
                        }
                        m.setNumeroMesa(leerEnteroConDefecto("Número de mesa", m.getNumeroMesa()));
                        m.setIdRecinto(leerEnteroConDefecto("ID recinto", m.getIdRecinto()));
                        m.setCantidadInscritos(leerEnteroConDefecto("Inscritos", m.getCantidadInscritos()));
                        m.setEstado(leerTextoConDefecto("Estado", m.getEstado()));
                        System.out.println(mesaDAO.update(m) ? "Actualizada." : "No se actualizó.");
                    }
                    case 5 -> System.out.println(
                            mesaDAO.delete(leerEntero("ID a eliminar: ")) ? "Eliminada." : "No encontrada.");
                    case 0 -> {
                        return;
                    }
                    default -> System.out.println("Opción no válida.");
                }
            } catch (SQLException e) {
                mostrarError(e);
            }
        }
    }

    private void menuPartidos() {
        while (true) {
            System.out.println();
            System.out.println("--- Partidos políticos ---");
            imprimirCrud();
            int opcion = leerEntero("Opción: ");
            try {
                switch (opcion) {
                    case 1 -> {
                        for (PartidoPolitico p : partidoDAO.findAll()) {
                            imprimirPartido(p);
                        }
                    }
                    case 2 -> {
                        PartidoPolitico p = partidoDAO.findById(leerEntero("ID: "));
                        if (p == null) {
                            System.out.println("No encontrado.");
                        } else {
                            imprimirPartido(p);
                        }
                    }
                    case 3 -> {
                        PartidoPolitico p = new PartidoPolitico();
                        p.setSigla(leerTexto("Sigla: "));
                        p.setNombreCompleto(leerTexto("Nombre completo: "));
                        p.setCandidatoPresidente(leerTexto("Candidato presidente: "));
                        System.out.println("Creado con ID " + partidoDAO.insert(p));
                    }
                    case 4 -> {
                        PartidoPolitico p = partidoDAO.findById(leerEntero("ID a editar: "));
                        if (p == null) {
                            System.out.println("No encontrado.");
                            break;
                        }
                        p.setSigla(leerTextoConDefecto("Sigla", p.getSigla()));
                        p.setNombreCompleto(leerTextoConDefecto("Nombre completo", p.getNombreCompleto()));
                        p.setCandidatoPresidente(leerTextoConDefecto("Candidato", p.getCandidatoPresidente()));
                        System.out.println(partidoDAO.update(p) ? "Actualizado." : "No se actualizó.");
                    }
                    case 5 -> System.out.println(
                            partidoDAO.delete(leerEntero("ID a eliminar: ")) ? "Eliminado." : "No encontrado.");
                    case 0 -> {
                        return;
                    }
                    default -> System.out.println("Opción no válida.");
                }
            } catch (SQLException e) {
                mostrarError(e);
            }
        }
    }

    private void menuPadron() {
        while (true) {
            System.out.println();
            System.out.println("--- Padrón ciudadano ---");
            imprimirCrud();
            int opcion = leerEntero("Opción: ");
            try {
                switch (opcion) {
                    case 1 -> {
                        List<PadronCiudadano> lista = padronDAO.findAll();
                        if (lista.isEmpty()) {
                            System.out.println("Sin registros.");
                        }
                        for (PadronCiudadano c : lista) {
                            imprimirCiudadano(c);
                        }
                    }
                    case 2 -> {
                        PadronCiudadano c = padronDAO.findById(leerTexto("CI: "));
                        if (c == null) {
                            System.out.println("No encontrado.");
                        } else {
                            imprimirCiudadano(c);
                        }
                    }
                    case 3 -> {
                        PadronCiudadano c = new PadronCiudadano();
                        c.setCi(leerTexto("CI: "));
                        c.setNombres(leerTexto("Nombres: "));
                        c.setApellidos(leerTexto("Apellidos: "));
                        c.setIdMesa(leerEntero("ID mesa: "));
                        padronDAO.insert(c);
                        System.out.println("Creado.");
                    }
                    case 4 -> {
                        PadronCiudadano c = padronDAO.findById(leerTexto("CI a editar: "));
                        if (c == null) {
                            System.out.println("No encontrado.");
                            break;
                        }
                        c.setNombres(leerTextoConDefecto("Nombres", c.getNombres()));
                        c.setApellidos(leerTextoConDefecto("Apellidos", c.getApellidos()));
                        c.setIdMesa(leerEnteroConDefecto("ID mesa", c.getIdMesa()));
                        String voto = leerTextoConDefecto("¿Ya votó? (s/n)", c.isHaVotado() ? "s" : "n");
                        boolean haVotado = voto.equalsIgnoreCase("s") || voto.equalsIgnoreCase("si");
                        c.setHaVotado(haVotado);
                        c.setHoraSufragio(haVotado ? Timestamp.valueOf(LocalDateTime.now()) : null);
                        System.out.println(padronDAO.update(c) ? "Actualizado." : "No se actualizó.");
                    }
                    case 5 -> System.out.println(
                            padronDAO.delete(leerTexto("CI a eliminar: ")) ? "Eliminado." : "No encontrado.");
                    case 0 -> {
                        return;
                    }
                    default -> System.out.println("Opción no válida.");
                }
            } catch (SQLException e) {
                mostrarError(e);
            }
        }
    }

    private void menuPapeletas() {
        while (true) {
            System.out.println();
            System.out.println("--- Papeletas de escrutinio ---");
            imprimirCrud();
            int opcion = leerEntero("Opción: ");
            try {
                switch (opcion) {
                    case 1 -> {
                        for (PapeletaEscrutinio p : papeletaDAO.findAll()) {
                            imprimirPapeleta(p);
                        }
                    }
                    case 2 -> {
                        PapeletaEscrutinio p = papeletaDAO.findById(leerEntero("ID: "));
                        if (p == null) {
                            System.out.println("No encontrada.");
                        } else {
                            imprimirPapeleta(p);
                        }
                    }
                    case 3 -> {
                        PapeletaEscrutinio p = new PapeletaEscrutinio();
                        p.setIdMesa(leerEntero("ID mesa: "));
                        p.setOrdenExtraccion(leerEntero("Orden de extracción: "));
                        p.setTipoVoto(leerTexto("Tipo (VALIDO/BLANCO/NULO): ").toUpperCase());
                        if (PapeletaEscrutinio.TIPO_VALIDO.equals(p.getTipoVoto())) {
                            p.setIdPartido(leerEntero("ID partido: "));
                        } else {
                            p.setIdPartido(null);
                        }
                        System.out.println("Creada con ID " + papeletaDAO.insert(p));
                    }
                    case 4 -> {
                        PapeletaEscrutinio p = papeletaDAO.findById(leerEntero("ID a editar: "));
                        if (p == null) {
                            System.out.println("No encontrada.");
                            break;
                        }
                        p.setIdMesa(leerEnteroConDefecto("ID mesa", p.getIdMesa()));
                        p.setOrdenExtraccion(leerEnteroConDefecto("Orden", p.getOrdenExtraccion()));
                        p.setTipoVoto(leerTextoConDefecto("Tipo", p.getTipoVoto()).toUpperCase());
                        if (PapeletaEscrutinio.TIPO_VALIDO.equals(p.getTipoVoto())) {
                            int actual = p.getIdPartido() == null ? 0 : p.getIdPartido();
                            p.setIdPartido(leerEnteroConDefecto("ID partido", actual));
                        } else {
                            p.setIdPartido(null);
                        }
                        System.out.println(papeletaDAO.update(p) ? "Actualizada." : "No se actualizó.");
                    }
                    case 5 -> System.out.println(
                            papeletaDAO.delete(leerEntero("ID a eliminar: ")) ? "Eliminada." : "No encontrada.");
                    case 0 -> {
                        return;
                    }
                    default -> System.out.println("Opción no válida.");
                }
            } catch (SQLException e) {
                mostrarError(e);
            }
        }
    }

    private void menuActas() {
        while (true) {
            System.out.println();
            System.out.println("--- Actas ---");
            imprimirCrud();
            int opcion = leerEntero("Opción: ");
            try {
                switch (opcion) {
                    case 1 -> {
                        for (Acta a : actaDAO.findAll()) {
                            imprimirActa(a);
                        }
                    }
                    case 2 -> {
                        Acta a = actaDAO.findById(leerEntero("ID: "));
                        if (a == null) {
                            System.out.println("No encontrada.");
                        } else {
                            imprimirActa(a);
                        }
                    }
                    case 3 -> {
                        Acta a = new Acta();
                        a.setIdMesa(leerEntero("ID mesa: "));
                        a.setVotosBlancos(leerEntero("Votos blancos: "));
                        a.setVotosNulos(leerEntero("Votos nulos: "));
                        a.setTotalCiudadanosVotaron(leerEntero("Total ciudadanos que votaron: "));
                        System.out.println("Creada con ID " + actaDAO.insert(a));
                    }
                    case 4 -> {
                        Acta a = actaDAO.findById(leerEntero("ID a editar: "));
                        if (a == null) {
                            System.out.println("No encontrada.");
                            break;
                        }
                        a.setIdMesa(leerEnteroConDefecto("ID mesa", a.getIdMesa()));
                        a.setVotosBlancos(leerEnteroConDefecto("Blancos", a.getVotosBlancos()));
                        a.setVotosNulos(leerEnteroConDefecto("Nulos", a.getVotosNulos()));
                        a.setTotalCiudadanosVotaron(leerEnteroConDefecto("Votaron", a.getTotalCiudadanosVotaron()));
                        System.out.println(actaDAO.update(a) ? "Actualizada." : "No se actualizó.");
                    }
                    case 5 -> System.out.println(
                            actaDAO.delete(leerEntero("ID a eliminar: ")) ? "Eliminada." : "No encontrada.");
                    case 0 -> {
                        return;
                    }
                    default -> System.out.println("Opción no válida.");
                }
            } catch (SQLException e) {
                mostrarError(e);
            }
        }
    }

    private void menuDetalles() {
        while (true) {
            System.out.println();
            System.out.println("--- Detalle de votos ---");
            imprimirCrud();
            int opcion = leerEntero("Opción: ");
            try {
                switch (opcion) {
                    case 1 -> {
                        for (DetalleVoto d : detalleVotoDAO.findAll()) {
                            imprimirDetalle(d);
                        }
                    }
                    case 2 -> {
                        DetalleVoto d = detalleVotoDAO.findById(leerEntero("ID: "));
                        if (d == null) {
                            System.out.println("No encontrado.");
                        } else {
                            imprimirDetalle(d);
                        }
                    }
                    case 3 -> {
                        DetalleVoto d = new DetalleVoto();
                        d.setIdActa(leerEntero("ID acta: "));
                        d.setIdPartido(leerEntero("ID partido: "));
                        d.setVotosValidos(leerEntero("Votos válidos: "));
                        System.out.println("Creado con ID " + detalleVotoDAO.insert(d));
                    }
                    case 4 -> {
                        DetalleVoto d = detalleVotoDAO.findById(leerEntero("ID a editar: "));
                        if (d == null) {
                            System.out.println("No encontrado.");
                            break;
                        }
                        d.setIdActa(leerEnteroConDefecto("ID acta", d.getIdActa()));
                        d.setIdPartido(leerEnteroConDefecto("ID partido", d.getIdPartido()));
                        d.setVotosValidos(leerEnteroConDefecto("Votos válidos", d.getVotosValidos()));
                        System.out.println(detalleVotoDAO.update(d) ? "Actualizado." : "No se actualizó.");
                    }
                    case 5 -> System.out.println(
                            detalleVotoDAO.delete(leerEntero("ID a eliminar: ")) ? "Eliminado." : "No encontrado.");
                    case 0 -> {
                        return;
                    }
                    default -> System.out.println("Opción no válida.");
                }
            } catch (SQLException e) {
                mostrarError(e);
            }
        }
    }

    private void mostrarActasYDetalleConsolidado() {
        System.out.println();
        System.out.println("--- Actas y detalle de votos consolidado ---");
        try {
            List<Acta> actas = actaDAO.findAll();
            List<DetalleVoto> detalles = detalleVotoDAO.findAll();
            List<PartidoPolitico> partidos = partidoDAO.findAll();

            if (actas.isEmpty()) {
                System.out.println("No hay actas registradas.");
                return;
            }

            ActasDetalleVentana.mostrar(actas, detalles, partidos);
            System.out.println("Se abrió la ventana con la tabla consolidada.");

            Map<Integer, Map<Integer, Integer>> votosPorActaYPartido = new HashMap<>();
            for (DetalleVoto detalle : detalles) {
                votosPorActaYPartido.computeIfAbsent(detalle.getIdActa(), key -> new HashMap<>())
                        .merge(detalle.getIdPartido(), detalle.getVotosValidos(), Integer::sum);
            }

            StringBuilder encabezado = new StringBuilder();
            encabezado.append(String.format("%-9s %-7s %-12s %-12s %-10s %-10s",
                    "Acta", "Mesa", "Votaron", "Válidos", "Blancos", "Nulos"));
            for (PartidoPolitico partido : partidos) {
                encabezado.append(String.format(" %-8s", partido.getSigla()));
            }
            System.out.println(encabezado);
            System.out.println("-".repeat(Math.max(encabezado.length(), 40)));

            for (Acta acta : actas) {
                Map<Integer, Integer> votosPartidos = votosPorActaYPartido.getOrDefault(acta.getIdActa(), Map.of());
                int votosValidos = 0;
                for (Integer votos : votosPartidos.values()) {
                    votosValidos += votos;
                }

                StringBuilder fila = new StringBuilder();
                fila.append(String.format("%-9d %-7d %-12d %-12d %-10d %-10d",
                        acta.getIdActa(),
                        acta.getIdMesa(),
                        acta.getTotalCiudadanosVotaron(),
                        votosValidos,
                        acta.getVotosBlancos(),
                        acta.getVotosNulos()));

                for (PartidoPolitico partido : partidos) {
                    int votos = votosPartidos.getOrDefault(partido.getIdPartido(), 0);
                    fila.append(String.format(" %-8d", votos));
                }
                System.out.println(fila);
            }
        } catch (SQLException e) {
            mostrarError(e);
        }
    }

    private void simularVotacion() {
        System.out.println();
        System.out.println("--- Simular votación ---");
        try {
            String ci = leerTexto("CI del votante: ");
            PadronCiudadano ciudadano = votacionService.validarCi(ci);
            System.out.println("Votante válido: " + ciudadano.getNombreCompleto()
                    + " | mesa " + ciudadano.getIdMesa());

            List<PartidoPolitico> partidos = votacionService.listarPartidos();
            if (partidos.isEmpty()) {
                System.out.println("No hay partidos registrados. Cargue partidos antes de votar.");
                return;
            }
            System.out.println("Partidos:");
            for (PartidoPolitico partido : partidos) {
                System.out.println("  " + partido.getIdPartido() + " | " + partido.getSigla()
                        + " | " + partido.getCandidatoPresidente());
            }
            System.out.println("  B | voto BLANCO");
            System.out.println("  N | voto NULO");

            String eleccion = leerTexto("Elija ID de partido, B o N: ");
            String tipoVoto;
            Integer idPartido = null;
            if (eleccion.equalsIgnoreCase("B") || eleccion.equalsIgnoreCase("BLANCO")) {
                tipoVoto = PapeletaEscrutinio.TIPO_BLANCO;
            } else if (eleccion.equalsIgnoreCase("N") || eleccion.equalsIgnoreCase("NULO")) {
                tipoVoto = PapeletaEscrutinio.TIPO_NULO;
            } else {
                try {
                    idPartido = Integer.parseInt(eleccion);
                    tipoVoto = PapeletaEscrutinio.TIPO_VALIDO;
                } catch (NumberFormatException e) {
                    System.out.println("Opción no válida.");
                    return;
                }
            }

            PapeletaEscrutinio papeleta = votacionService.registrarVoto(ci, tipoVoto, idPartido);
            System.out.println("Voto registrado. Papeleta #" + papeleta.getOrdenExtraccion()
                    + " en mesa " + papeleta.getIdMesa() + " (" + papeleta.getTipoVoto() + ").");
            System.out.println("El CI no se guardó en la papeleta (voto secreto).");
        } catch (VotacionException e) {
            System.out.println(e.getMessage());
        } catch (SQLException e) {
            mostrarError(e);
        }
    }

    private void mostrarResultadosTotales() {
        System.out.println();
        System.out.println("--- Resultados totales ---");
        try {
            ResultadoSimulacionService.InformeEstadistico informe = resultadoService.computarEstadisticas();
            ResultadoSimulacionService.ComputoTotal computo = informe.nacional();
            if (computo.totalPapeletas() == 0) {
                System.out.println("Aún no hay votos registrados.");
                return;
            }
            System.out.printf("%-8s %-28s %8s %10s%n", "Sigla", "Candidato", "Votos", "% válidos");
            for (ResultadoSimulacionService.FilaResultado fila : computo.partidos()) {
                System.out.printf("%-8s %-28s %8d %9.2f%%%n",
                        fila.sigla(),
                        fila.candidato(),
                        fila.votos(),
                        fila.porcentajeSobreValidos());
            }
            System.out.println("--------------------------------------------------------------");
            System.out.println("Válidos: " + computo.votosValidos()
                    + " | Blancos: " + computo.votosBlancos()
                    + " | Nulos: " + computo.votosNulos()
                    + " | Total: " + computo.totalPapeletas());
            System.out.printf("Porcentajes sobre el total: válidos %.2f%% | blancos %.2f%% | nulos %.2f%%%n",
                    computo.votosValidos() * 100.0 / computo.totalPapeletas(),
                    computo.votosBlancos() * 100.0 / computo.totalPapeletas(),
                    computo.votosNulos() * 100.0 / computo.totalPapeletas());
            GraficoBarrasVentana.mostrar(informe);
            System.out.println("Se abrió el gráfico. Usa el filtro para consultar cada departamento.");
        } catch (SQLException e) {
            mostrarError(e);
        }
    }

    private void mostrarResultadosOficiales() {
        System.out.println();
        System.out.println("--- Resultados oficiales por partido y departamento ---");
        try {
            imprimirInformeElectoral(resultadoElectoralService.computar());
        } catch (SQLException e) {
            mostrarError(e);
        }
    }

    private void mostrarResultadosFinales() {
        System.out.println();
        System.out.println("--- Resultado final de la elección presidencial ---");
        try {
            ResultadoFinalService.ResultadoFinal resultado = resultadoFinalService.computar();
            imprimirInformeElectoral(resultado.informe());
            imprimirConclusionElectoral(resultado);
        } catch (SQLException e) {
            mostrarError(e);
        }
    }

    private void imprimirInformeElectoral(ResultadoElectoralService.InformeElectoral informe) {
        imprimirResultadosPartidos("RESULTADO NACIONAL", informe.nacional().votosValidos(),
                informe.nacional().partidos());
        for (ResultadoElectoralService.ResultadoDepartamento departamento : informe.departamentos()) {
            imprimirResultadosPartidos("DEPARTAMENTO: " + departamento.departamento(),
                    departamento.votosValidos(),
                    departamento.partidos());
        }
    }

    private void imprimirConclusionElectoral(ResultadoFinalService.ResultadoFinal resultado) {
        System.out.println();
        System.out.println("=== CONCLUSIÓN ELECTORAL (LEY 026) ===");
        switch (resultado.estado()) {
            case SIN_VOTOS_VALIDOS ->
                    System.out.println("No hay votos válidos computados; todavía no se puede determinar el resultado.");
            case GANADOR_PRIMERA_VUELTA -> {
                ResultadoElectoralService.FilaPartido ganador = resultado.ganador()
                        .orElseThrow(() -> new IllegalStateException("El resultado no incluye al ganador."));
                System.out.printf("Ganador en primera vuelta: %s (%s), %d votos (%.2f%%).%n",
                        ganador.candidato(), ganador.sigla(), ganador.votos(), ganador.porcentajeSobreValidos());
                switch (resultado.criterioPrimeraVuelta().orElseThrow(
                        () -> new IllegalStateException("El resultado no incluye el criterio aplicado."))) {
                    case MAS_DEL_50_POR_CIENTO ->
                            System.out.println("Cumple el requisito de obtener más del 50% de los votos válidos.");
                    case AL_MENOS_40_Y_10_PUNTOS_DE_VENTAJA ->
                            System.out.println("Cumple el requisito de obtener al menos 40% y aventajar por "
                                    + "10 puntos porcentuales o más al segundo.");
                }
            }
            case SEGUNDA_VUELTA -> {
                List<ResultadoElectoralService.FilaPartido> candidatos =
                        resultado.candidatosSegundaVuelta();
                ResultadoElectoralService.FilaPartido primero = candidatos.get(0);
                ResultadoElectoralService.FilaPartido segundo = candidatos.get(1);
                System.out.println("No se alcanzó un criterio para ganar en primera vuelta.");
                System.out.println("Corresponde una segunda vuelta entre:");
                imprimirCandidatoSegundaVuelta(primero);
                imprimirCandidatoSegundaVuelta(segundo);
            }
            case EMPATE_POR_SEGUNDO_LUGAR -> {
                System.out.println("No se alcanzó un criterio para ganar en primera vuelta y hay empate "
                        + "por el segundo lugar.");
                System.out.println("No se puede definir todavía la segunda vuelta; candidatos empatados:");
                for (ResultadoElectoralService.FilaPartido candidato : resultado.candidatosSegundaVuelta()) {
                    imprimirCandidatoSegundaVuelta(candidato);
                }
            }
            case INDETERMINADO ->
                    System.out.println("No hay candidaturas suficientes para determinar un ganador o "
                            + "una segunda vuelta.");
        }
    }

    private void imprimirCandidatoSegundaVuelta(ResultadoElectoralService.FilaPartido candidato) {
        System.out.printf("  %s (%s): %d votos (%.2f%%).%n",
                candidato.candidato(), candidato.sigla(), candidato.votos(),
                candidato.porcentajeSobreValidos());
    }

    private void cerrarMesa() {
        System.out.println();
        System.out.println("--- Cierre de mesas e inicio del escrutinio oficial ---");
        try {
            System.out.println("1. Cerrar una mesa");
            System.out.println("2. Cerrar todas las mesas habilitadas");
            System.out.println("3. Habilitar todas las mesas para ingreso de votaciones (demo)");
            System.out.println("0. Volver");
            int opcion = leerEntero("Opción: ");
            if (opcion == 0) {
                return;
            }
            if (opcion == 2) {
                cerrarTodasLasMesas();
                return;
            }
            if (opcion == 3) {
                habilitarTodasLasMesas();
                return;
            }
            if (opcion != 1) {
                System.out.println("Opción no válida.");
                return;
            }

            List<Mesa> mesas = mesaDAO.findAll();
            System.out.println("Mesas habilitadas:");
            boolean hayMesasHabilitadas = false;
            for (Mesa mesa : mesas) {
                if (Mesa.ESTADO_HABILITADA.equals(mesa.getEstado())) {
                    System.out.println("  ID " + mesa.getIdMesa() + " | mesa " + mesa.getNumeroMesa());
                    hayMesasHabilitadas = true;
                }
            }
            if (!hayMesasHabilitadas) {
                System.out.println("No hay mesas habilitadas para cerrar.");
                return;
            }

            int idMesa = leerEntero("ID de la mesa a cerrar: ");
            CierreMesaService.ResumenCierre resumen = cierreMesaService.cerrarMesa(idMesa);
            System.out.println("Mesa " + resumen.numeroMesa() + " cerrada y marcada como COMPUTADA.");
            System.out.println("Votos válidos: " + resumen.votosValidos()
                    + " | Blancos: " + resumen.votosBlancos()
                    + " | Nulos: " + resumen.votosNulos()
                    + " | Total: " + resumen.totalVotos());
            System.out.println("El acta y el detalle por partido quedaron registrados.");
        } catch (VotacionException e) {
            System.out.println(e.getMessage());
        } catch (SQLException e) {
            mostrarError(e);
        }
    }

    private void cerrarTodasLasMesas() throws SQLException {
        String confirmacion = leerTexto(
                "¿Confirma cerrar todas las mesas habilitadas que tengan papeletas? (S/N): ");
        if (!"S".equalsIgnoreCase(confirmacion.trim())) {
            System.out.println("Cierre cancelado.");
            return;
        }

        CierreMesaService.ResultadoCierreMasivo resultado = cierreMesaService.cerrarTodasMesas();
        System.out.println("Mesas cerradas: " + resultado.cerradas().size());
        for (CierreMesaService.ResumenCierre resumen : resultado.cerradas()) {
            System.out.println("  Mesa " + resumen.numeroMesa()
                    + " | válidos " + resumen.votosValidos()
                    + " | blancos " + resumen.votosBlancos()
                    + " | nulos " + resumen.votosNulos()
                    + " | total " + resumen.totalVotos());
        }
        if (!resultado.noCerradas().isEmpty()) {
            System.out.println("Mesas habilitadas que no se cerraron:");
            for (String detalle : resultado.noCerradas()) {
                System.out.println("  " + detalle);
            }
        }
    }

    private void habilitarTodasLasMesas() throws SQLException {
        String confirmacion = leerTexto(
                "¿Confirma habilitar todas las mesas para el ingreso de votaciones? (S/N): ");
        if (!"S".equalsIgnoreCase(confirmacion.trim())) {
            System.out.println("Habilitación cancelada.");
            return;
        }

        int habilitadas = 0;
        for (Mesa mesa : mesaDAO.findAll()) {
            if (!Mesa.ESTADO_HABILITADA.equals(mesa.getEstado())) {
                mesa.setEstado(Mesa.ESTADO_HABILITADA);
                if (mesaDAO.update(mesa)) {
                    habilitadas++;
                }
            } else {
                habilitadas++;
            }
        }

        System.out.println("Mesas habilitadas: " + habilitadas);
        System.out.println("Las mesas quedaron listas para el ingreso de votaciones.");
    }

    private void imprimirResultadosPartidos(
            String titulo,
            long votosValidos,
            List<ResultadoElectoralService.FilaPartido> partidos
    ) {
        System.out.println();
            System.out.println("=".repeat(95));
        System.out.println(titulo);
        System.out.println("Votos válidos: " + votosValidos);
        if (votosValidos == 0) {
            System.out.println("Sin votos válidos registrados.");
            return;
        }

        System.out.printf("%-4s %-8s %-28s %-28s %12s %10s%n",
                "#", "Sigla", "Partido", "Candidato", "Votos", "% válidos");
            System.out.println("-".repeat(95));
        int posicion = 1;
        for (ResultadoElectoralService.FilaPartido partido : partidos) {
                System.out.printf("%-4d %-8s %-28s %-28s %12d %10.2f%%%n",
                    posicion++,
                    partido.sigla(),
                    partido.nombreCompleto(),
                    partido.candidato(),
                    partido.votos(),
                    partido.porcentajeSobreValidos());
        }
    }

    private void imprimirCrud() {
        System.out.println("1. Listar todos");
        System.out.println("2. Buscar");
        System.out.println("3. Crear");
        System.out.println("4. Editar");
        System.out.println("5. Eliminar");
        System.out.println("0. Volver");
    }

    private void imprimirMesa(Mesa m) {
        System.out.println(m.getIdMesa() + " | mesa " + m.getNumeroMesa()
                + " | recinto " + m.getIdRecinto()
                + " | inscritos " + m.getCantidadInscritos()
                + " | " + m.getEstado());
    }

    private void imprimirPartido(PartidoPolitico p) {
        System.out.println(p.getIdPartido() + " | " + p.getSigla()
                + " | " + p.getNombreCompleto()
                + " | " + p.getCandidatoPresidente());
    }

    private void imprimirCiudadano(PadronCiudadano c) {
        System.out.println(c.getCi() + " | " + c.getNombreCompleto()
                + " | mesa " + c.getIdMesa()
                + " | " + (c.isHaVotado() ? "votó" : "pendiente"));
    }

    private void imprimirPapeleta(PapeletaEscrutinio p) {
        System.out.println(p.getIdPapeleta() + " | mesa " + p.getIdMesa()
                + " | #" + p.getOrdenExtraccion()
                + " | " + p.getTipoVoto()
                + " | partido " + p.getIdPartido());
    }

    private void imprimirActa(Acta a) {
        System.out.println(a.getIdActa() + " | mesa " + a.getIdMesa()
                + " | blancos " + a.getVotosBlancos()
                + " | nulos " + a.getVotosNulos()
                + " | votaron " + a.getTotalCiudadanosVotaron());
    }

    private void imprimirDetalle(DetalleVoto d) {
        System.out.println(d.getIdDetalle() + " | acta " + d.getIdActa()
                + " | partido " + d.getIdPartido()
                + " | votos " + d.getVotosValidos());
    }

    private void mostrarError(SQLException e) {
        System.out.println("Error de base de datos: " + e.getMessage());
    }

    private String leerTexto(String mensaje) {
        System.out.print(mensaje);
        return scanner.nextLine().trim();
    }

    private String leerTextoConDefecto(String etiqueta, String actual) {
        String valor = leerTexto(etiqueta + " [" + actual + "]: ");
        return valor.isEmpty() ? actual : valor;
    }

    private int leerEntero(String mensaje) {
        while (true) {
            String valor = leerTexto(mensaje);
            try {
                return Integer.parseInt(valor);
            } catch (NumberFormatException e) {
                System.out.println("Ingresa un número entero.");
            }
        }
    }

    private int leerEnteroConDefecto(String etiqueta, int actual) {
        String valor = leerTexto(etiqueta + " [" + actual + "]: ");
        if (valor.isEmpty()) {
            return actual;
        }
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            System.out.println("Valor inválido, se mantiene " + actual);
            return actual;
        }
    }
}
