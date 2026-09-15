package bo.edu.electoral.model;

/**
 * Modelo de la tabla partido_politico.
 */
public class PartidoPolitico {

    private int idPartido;
    private String sigla;
    private String nombreCompleto;
    private String candidatoPresidente;

    public PartidoPolitico() {
    }

    public PartidoPolitico(int idPartido, String sigla, String nombreCompleto, String candidatoPresidente) {
        this.idPartido = idPartido;
        this.sigla = sigla;
        this.nombreCompleto = nombreCompleto;
        this.candidatoPresidente = candidatoPresidente;
    }

    public int getIdPartido() {
        return idPartido;
    }

    public void setIdPartido(int idPartido) {
        this.idPartido = idPartido;
    }

    public String getSigla() {
        return sigla;
    }

    public void setSigla(String sigla) {
        this.sigla = sigla;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getCandidatoPresidente() {
        return candidatoPresidente;
    }

    public void setCandidatoPresidente(String candidatoPresidente) {
        this.candidatoPresidente = candidatoPresidente;
    }

    @Override
    public String toString() {
        return sigla + " - " + candidatoPresidente;
    }
}
