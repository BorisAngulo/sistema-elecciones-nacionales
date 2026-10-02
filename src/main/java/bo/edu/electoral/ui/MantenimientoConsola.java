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


import java.sql.SQLException;



import java.util.Scanner;
import java.util.List;

/**
 * Menú de consola para crear, listar, editar y eliminar registros.
 * Preparación previa y consultas; la votación se realiza desde MainConsola.
 */
public class MantenimientoConsola {

    private final Scanner scanner;
    public MantenimientoConsola(Scanner scanner) { this.scanner = scanner; }
    private final DepartamentoDAO departamentoDAO = new DepartamentoDAO();
    private final MunicipioDAO municipioDAO = new MunicipioDAO();
    private final RecintoDAO recintoDAO = new RecintoDAO();
    private final MesaDAO mesaDAO = new MesaDAO();
    private final PartidoPoliticoDAO partidoDAO = new PartidoPoliticoDAO();
    private final ActaDAO actaDAO = new ActaDAO();
    private final DetalleVotoDAO detalleVotoDAO = new DetalleVotoDAO();
    private final PadronCiudadanoDAO padronDAO = new PadronCiudadanoDAO();
    private final PapeletaEscrutinioDAO papeletaDAO = new PapeletaEscrutinioDAO();

    public void iniciar() {
        boolean continuar = true;
        while (continuar) {
            System.out.println();
            System.out.println("===== SISTEMA ELECTORAL - MANTENIMIENTO DE DATOS =====");
            System.out.println("1. Departamentos");
            System.out.println("2. Municipios");
            System.out.println("3. Recintos");
            System.out.println("4. Mesas");
            System.out.println("5. Partidos políticos");
            System.out.println("6. Padrón ciudadano");
            System.out.println("7. Papeletas de escrutinio");
            System.out.println("8. Actas");
            System.out.println("9. Detalle de votos");
            System.out.println("0. Volver al menú electoral");
            int opcion = leerEntero("Opción: ");
            switch (opcion) {
                case 1 -> menuDepartamentos();
                case 2 -> menuMunicipios();
                case 3 -> menuRecintos();
                case 4 -> menuMesas();
                case 5 -> menuPartidos();
                case 6 -> menuPadron();
                case 7 -> papeletaDAOConsulta();
                case 8 -> actaDAOConsulta();
                case 9 -> detalleDAOConsulta();
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
                if (opcion >= 3 && opcion <= 5) validarPreparacion();
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
                if (opcion >= 3 && opcion <= 5) validarPreparacion();
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
                if (opcion >= 3 && opcion <= 5) validarPreparacion();
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
                if (opcion >= 3 && opcion <= 5) validarPreparacion();
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
                        m.setEstado(Mesa.ESTADO_HABILITADA);
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
                        m.setEstado(Mesa.ESTADO_HABILITADA);
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
                if (opcion >= 3 && opcion <= 5) validarPreparacion();
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
                if (opcion >= 3 && opcion <= 5) validarPreparacion();
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
                        c.setHaVotado(false);
                        c.setHoraSufragio(null);
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
    private void validarPreparacion() throws SQLException {
        try (var p = DatabaseConnection.getConnection().prepareStatement(
                "SELECT EXISTS(SELECT 1 FROM padron_ciudadano WHERE ha_votado) OR EXISTS(SELECT 1 FROM papeleta_escrutinio) OR EXISTS(SELECT 1 FROM acta)"); var r = p.executeQuery()) {
            r.next();
            if (r.getBoolean(1)) throw new SQLException("La preparación está cerrada: ya empezó la votación. Solo se permite consultar.");
        }
    }
    private void papeletaDAOConsulta() {
        try { for (var p : papeletaDAO.findAll()) imprimirPapeleta(p); } catch (SQLException e) { mostrarError(e); }
    }
    private void actaDAOConsulta() {
        try { for (var a : actaDAO.findAll()) imprimirActa(a); } catch (SQLException e) { mostrarError(e); }
    }
    private void detalleDAOConsulta() {
        try { for (var d : detalleVotoDAO.findAll()) imprimirDetalle(d); } catch (SQLException e) { mostrarError(e); }
    }}



