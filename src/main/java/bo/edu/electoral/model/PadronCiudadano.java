package bo.edu.electoral.model;

import java.sql.Timestamp;

/**
 * Modelo de la tabla padron_ciudadano.
 * Control de asistencia por mesa. No almacena el voto (voto secreto).
 */
public class PadronCiudadano {

    private String ci;
    private String nombres;
    private String apellidos;
    private int idMesa;
    private boolean haVotado;
    private Timestamp horaSufragio;
    private Mesa mesa;

    public PadronCiudadano() {
        this.haVotado = false;
    }

    public PadronCiudadano(String ci, String nombres, String apellidos, int idMesa) {
        this.ci = ci;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.idMesa = idMesa;
        this.haVotado = false;
    }

    public String getCi() {
        return ci;
    }

    public void setCi(String ci) {
        this.ci = ci;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public int getIdMesa() {
        return idMesa;
    }

    public void setIdMesa(int idMesa) {
        this.idMesa = idMesa;
    }

    public boolean isHaVotado() {
        return haVotado;
    }

    public void setHaVotado(boolean haVotado) {
        this.haVotado = haVotado;
    }

    public Timestamp getHoraSufragio() {
        return horaSufragio;
    }

    public void setHoraSufragio(Timestamp horaSufragio) {
        this.horaSufragio = horaSufragio;
    }

    public Mesa getMesa() {
        return mesa;
    }

    public void setMesa(Mesa mesa) {
        this.mesa = mesa;
        if (mesa != null) {
            this.idMesa = mesa.getIdMesa();
        }
    }

    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }

    @Override
    public String toString() {
        return ci + " - " + getNombreCompleto() + (haVotado ? " (votó)" : " (pendiente)");
    }
}
