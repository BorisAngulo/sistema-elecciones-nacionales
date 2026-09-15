package bo.edu.electoral.model;

/**
 * Modelo de la tabla detalle_voto_partido.
 * Un registro por partido dentro de un acta.
 */
public class DetalleVoto {

    private int idDetalle;
    private int idActa;
    private int idPartido;
    private int votosValidos;
    private Acta acta;
    private PartidoPolitico partido;

    public DetalleVoto() {
    }

    public DetalleVoto(int idDetalle, int idActa, int idPartido, int votosValidos) {
        this.idDetalle = idDetalle;
        this.idActa = idActa;
        this.idPartido = idPartido;
        this.votosValidos = votosValidos;
    }

    public int getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(int idDetalle) {
        this.idDetalle = idDetalle;
    }

    public int getIdActa() {
        return idActa;
    }

    public void setIdActa(int idActa) {
        this.idActa = idActa;
    }

    public int getIdPartido() {
        return idPartido;
    }

    public void setIdPartido(int idPartido) {
        this.idPartido = idPartido;
    }

    public int getVotosValidos() {
        return votosValidos;
    }

    public void setVotosValidos(int votosValidos) {
        this.votosValidos = votosValidos;
    }

    public Acta getActa() {
        return acta;
    }

    public void setActa(Acta acta) {
        this.acta = acta;
        if (acta != null) {
            this.idActa = acta.getIdActa();
        }
    }

    public PartidoPolitico getPartido() {
        return partido;
    }

    public void setPartido(PartidoPolitico partido) {
        this.partido = partido;
        if (partido != null) {
            this.idPartido = partido.getIdPartido();
        }
    }

    @Override
    public String toString() {
        String etiquetaPartido = partido != null ? partido.getSigla() : String.valueOf(idPartido);
        return etiquetaPartido + ": " + votosValidos + " votos";
    }
}
