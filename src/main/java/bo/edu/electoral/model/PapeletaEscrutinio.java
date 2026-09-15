package bo.edu.electoral.model;

import java.sql.Timestamp;

/**
 * Modelo de la tabla papeleta_escrutinio.
 * Registro unitario de papeletas. No tiene CI: garantiza el voto secreto.
 * tipoVoto: VALIDO, BLANCO, NULO. idPartido es null si es blanco o nulo.
 */
public class PapeletaEscrutinio {

    public static final String TIPO_VALIDO = "VALIDO";
    public static final String TIPO_BLANCO = "BLANCO";
    public static final String TIPO_NULO = "NULO";

    private int idPapeleta;
    private int idMesa;
    private int ordenExtraccion;
    private String tipoVoto;
    private Integer idPartido;
    private Timestamp fechaRegistro;
    private Mesa mesa;
    private PartidoPolitico partido;

    public PapeletaEscrutinio() {
    }

    public PapeletaEscrutinio(int idPapeleta, int idMesa, int ordenExtraccion, String tipoVoto, Integer idPartido) {
        this.idPapeleta = idPapeleta;
        this.idMesa = idMesa;
        this.ordenExtraccion = ordenExtraccion;
        this.tipoVoto = tipoVoto;
        this.idPartido = idPartido;
    }

    public int getIdPapeleta() {
        return idPapeleta;
    }

    public void setIdPapeleta(int idPapeleta) {
        this.idPapeleta = idPapeleta;
    }

    public int getIdMesa() {
        return idMesa;
    }

    public void setIdMesa(int idMesa) {
        this.idMesa = idMesa;
    }

    public int getOrdenExtraccion() {
        return ordenExtraccion;
    }

    public void setOrdenExtraccion(int ordenExtraccion) {
        this.ordenExtraccion = ordenExtraccion;
    }

    public String getTipoVoto() {
        return tipoVoto;
    }

    public void setTipoVoto(String tipoVoto) {
        this.tipoVoto = tipoVoto;
    }

    public Integer getIdPartido() {
        return idPartido;
    }

    public void setIdPartido(Integer idPartido) {
        this.idPartido = idPartido;
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
        String detalle;
        if (TIPO_VALIDO.equals(tipoVoto) && partido != null) {
            detalle = partido.getSigla();
        } else if (TIPO_VALIDO.equals(tipoVoto) && idPartido != null) {
            detalle = "partido " + idPartido;
        } else {
            detalle = tipoVoto;
        }
        return "Papeleta #" + ordenExtraccion + " - " + detalle;
    }
}
