package bo.edu.electoral.model;

import java.sql.Timestamp;

/**
 * Modelo de la tabla acta.
 * Relación 1 a 1 con mesa (id_mesa es UNIQUE).
 */
public class Acta {

    private int idActa;
    private int idMesa;
    private int votosBlancos;
    private int votosNulos;
    private int totalCiudadanosVotaron;
    private Timestamp fechaRegistro;
    private Mesa mesa;

    public Acta() {
    }

    public Acta(int idActa, int idMesa, int votosBlancos, int votosNulos, int totalCiudadanosVotaron) {
        this.idActa = idActa;
        this.idMesa = idMesa;
        this.votosBlancos = votosBlancos;
        this.votosNulos = votosNulos;
        this.totalCiudadanosVotaron = totalCiudadanosVotaron;
    }

    public int getIdActa() {
        return idActa;
    }

    public void setIdActa(int idActa) {
        this.idActa = idActa;
    }

    public int getIdMesa() {
        return idMesa;
    }

    public void setIdMesa(int idMesa) {
        this.idMesa = idMesa;
    }

    public int getVotosBlancos() {
        return votosBlancos;
    }

    public void setVotosBlancos(int votosBlancos) {
        this.votosBlancos = votosBlancos;
    }

    public int getVotosNulos() {
        return votosNulos;
    }

    public void setVotosNulos(int votosNulos) {
        this.votosNulos = votosNulos;
    }

    public int getTotalCiudadanosVotaron() {
        return totalCiudadanosVotaron;
    }

    public void setTotalCiudadanosVotaron(int totalCiudadanosVotaron) {
        this.totalCiudadanosVotaron = totalCiudadanosVotaron;
    }

    public Timestamp getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Timestamp fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
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

    @Override
    public String toString() {
        return "Acta mesa " + idMesa
                + " | blancos=" + votosBlancos
                + " | nulos=" + votosNulos
                + " | votaron=" + totalCiudadanosVotaron;
    }
}
