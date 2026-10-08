package bo.edu.electoral;

import bo.edu.electoral.dao.ReporteElectoralDAO.Resumen;
import bo.edu.electoral.dao.ReporteElectoralDAO.TotalPartido;
import bo.edu.electoral.ui.TablaFrecuenciasPartidos;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TablaFrecuenciasPartidosTest {
    @Test void separaDenominadorValidoDeTotalPapeletas() {
        var resumen = new Resumen(List.of(new TotalPartido(1,"A",2,0),
                new TotalPartido(2,"B",1,0), new TotalPartido(3,"C",0,0)),3,1,1,5,1,0,0);
        String tabla = TablaFrecuenciasPartidos.formatear(resumen);
        assertTrue(tabla.contains("0.6667"));
        assertTrue(tabla.contains("66.67%"));
        assertTrue(tabla.contains("33.33%"));
        assertTrue(tabla.contains("60.00%"));
        assertTrue(tabla.contains("20.00%"));
        assertTrue(tabla.contains("0.0000"));
        assertTrue(tabla.contains("100.00%"));
    }

    @Test void sinVotosNoInventaPorcentajes() {
        String tabla = TablaFrecuenciasPartidos.formatear(new Resumen(
                List.of(new TotalPartido(1,"A",0,0)),0,0,0,0,0,1,0));
        assertTrue(tabla.contains("Cómputo parcial"));
        assertTrue(tabla.contains("no están definidos"));
        assertFalse(tabla.contains("NaN"));
        assertFalse(tabla.contains("100.00%"));
    }
}
