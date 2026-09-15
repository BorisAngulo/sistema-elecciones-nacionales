package bo.edu.electoral.model;

/**
 * Modelo de la tabla mesa.
 * Estados posibles: HABILITADA, COMPUTADA, ANULADA.
 */
public class Mesa {

    public static final String ESTADO_HABILITADA = "HABILITADA";
    public static final String ESTADO_COMPUTADA = "COMPUTADA";
    public static final String ESTADO_ANULADA = "ANULADA";

    private int idMesa;
    private int numeroMesa;
    private int idRecinto;
    private int cantidadInscritos;
    private String estado;
    private Recinto recinto;

    public Mesa() {
        this.cantidadInscritos = 240;
        this.estado = ESTADO_HABILITADA;
    }

    public Mesa(int idMesa, int numeroMesa, int idRecinto, int cantidadInscritos, String estado) {
        this.idMesa = idMesa;
        this.numeroMesa = numeroMesa;
        this.idRecinto = idRecinto;
        this.cantidadInscritos = cantidadInscritos;
        this.estado = estado;
    }

    public int getIdMesa() {
        return idMesa;
    }

    public void setIdMesa(int idMesa) {
        this.idMesa = idMesa;
    }

    public int getNumeroMesa() {
        return numeroMesa;
    }

    public void setNumeroMesa(int numeroMesa) {
        this.numeroMesa = numeroMesa;
    }

    public int getIdRecinto() {
        return idRecinto;
    }

    public void setIdRecinto(int idRecinto) {
        this.idRecinto = idRecinto;
    }

    public int getCantidadInscritos() {
        return cantidadInscritos;
    }

    public void setCantidadInscritos(int cantidadInscritos) {
        this.cantidadInscritos = cantidadInscritos;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Recinto getRecinto() {
        return recinto;
    }

    public void setRecinto(Recinto recinto) {
        this.recinto = recinto;
        if (recinto != null) {
            this.idRecinto = recinto.getIdRecinto();
        }
    }

    @Override
    public String toString() {
        return "Mesa " + numeroMesa + " (" + estado + ")";
    }
}
