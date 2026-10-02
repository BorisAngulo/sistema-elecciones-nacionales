package bo.edu.electoral.service;

/** Reglas puras, independientes de la interfaz y de JDBC. */
public final class ValidadorActaService {
    public void validar(String estado, boolean existeActa, long inscritos, long asistieron,
                        long blancos, long nulos, long validos, long papeletas) {
        if (!"HABILITADA".equals(estado)) throw new IllegalArgumentException("Mesa no habilitada");
        if (existeActa) throw new IllegalArgumentException("La mesa ya tiene acta");
        if (inscritos < 0 || asistieron < 0 || blancos < 0 || nulos < 0 || validos < 0 || papeletas < 0)
            throw new IllegalArgumentException("Los conteos no pueden ser negativos");
        long total = Math.addExact(Math.addExact(blancos, nulos), validos);
        if (total > inscritos || asistieron > inscritos)
            throw new IllegalArgumentException("Se supera la cantidad de inscritos");
        if (total != papeletas || papeletas != asistieron)
            throw new IllegalArgumentException("Votos, papeletas y asistencia deben coincidir");
    }
}

