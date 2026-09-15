package bo.edu.electoral.model;

/**
 * Modelo de la tabla municipio.
 */
public class Municipio {

    private int idMunicipio;
    private String nombre;
    private int idDepartamento;
    private Departamento departamento;

    public Municipio() {
    }

    public Municipio(int idMunicipio, String nombre, int idDepartamento) {
        this.idMunicipio = idMunicipio;
        this.nombre = nombre;
        this.idDepartamento = idDepartamento;
    }

    public int getIdMunicipio() {
        return idMunicipio;
    }

    public void setIdMunicipio(int idMunicipio) {
        this.idMunicipio = idMunicipio;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getIdDepartamento() {
        return idDepartamento;
    }

    public void setIdDepartamento(int idDepartamento) {
        this.idDepartamento = idDepartamento;
    }

    public Departamento getDepartamento() {
        return departamento;
    }

    public void setDepartamento(Departamento departamento) {
        this.departamento = departamento;
        if (departamento != null) {
            this.idDepartamento = departamento.getIdDepartamento();
        }
    }

    @Override
    public String toString() {
        return nombre;
    }
}
