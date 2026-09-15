package bo.edu.electoral.model;

/**
 * Modelo de la tabla recinto.
 */
public class Recinto {

    private int idRecinto;
    private String nombre;
    private int idMunicipio;
    private Municipio municipio;

    public Recinto() {
    }

    public Recinto(int idRecinto, String nombre, int idMunicipio) {
        this.idRecinto = idRecinto;
        this.nombre = nombre;
        this.idMunicipio = idMunicipio;
    }

    public int getIdRecinto() {
        return idRecinto;
    }

    public void setIdRecinto(int idRecinto) {
        this.idRecinto = idRecinto;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getIdMunicipio() {
        return idMunicipio;
    }

    public void setIdMunicipio(int idMunicipio) {
        this.idMunicipio = idMunicipio;
    }

    public Municipio getMunicipio() {
        return municipio;
    }

    public void setMunicipio(Municipio municipio) {
        this.municipio = municipio;
        if (municipio != null) {
            this.idMunicipio = municipio.getIdMunicipio();
        }
    }

    @Override
    public String toString() {
        return nombre;
    }
}
